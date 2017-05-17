package cc.before30.webapp.services.lease;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

@CommonsLog
public class SomethingLeaseContainer extends SomethingLeaseEventPublisher implements
		InitializingBean, DisposableBean {

	private static final AtomicIntegerFieldUpdater<SomethingLeaseContainer> UPDATER = AtomicIntegerFieldUpdater
			.newUpdater(SomethingLeaseContainer.class, "status");

	private static final AtomicInteger poolId = new AtomicInteger();

	private static final int STATUS_INITIAL = 0;
	private static final int STATUS_STARTED = 1;
	private static final int STATUS_DESTROYED = 2;

	private final List<RequestedSomething> requestedSomethings = new CopyOnWriteArrayList<RequestedSomething>();

	private final Map<RequestedSomething, LeaseRenewalScheduler> renewals = new ConcurrentHashMap<RequestedSomething, LeaseRenewalScheduler>();

	private final VaultOperations operations;

	private int minRenewalSeconds = 10;

	private int expiryThresholdSeconds = 60;

	private TaskScheduler taskScheduler;

	private boolean manageTaskScheduler;

	private volatile boolean initialized;

	private volatile int status = STATUS_INITIAL;

	public SomethingLeaseContainer(VaultOperations operations) {

		Assert.notNull(operations, "VaultOperations must not be null");

		this.operations = operations;
	}

	public SomethingLeaseContainer(VaultOperations operations, TaskScheduler taskScheduler) {

		Assert.notNull(operations, "VaultOperations must not be null");
		Assert.notNull(taskScheduler, "TaskScheduler must not be null");

		this.operations = operations;
		setTaskScheduler(taskScheduler);
	}

	public void setExpiryThresholdSeconds(int expiryThresholdSeconds) {
		this.expiryThresholdSeconds = expiryThresholdSeconds;
	}

	public void setMinRenewalSeconds(int minRenewalSeconds) {
		this.minRenewalSeconds = minRenewalSeconds;
	}

	public int getMinRenewalSeconds() {
		return minRenewalSeconds;
	}

	public int getExpiryThresholdSeconds() {
		return expiryThresholdSeconds;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {

		Assert.notNull(taskScheduler, "TaskScheduler must not be null");
		this.taskScheduler = taskScheduler;
	}

	public RequestedSomething requestRenewableSecret(String path) {
		return addRequestedSecret(RequestedSomething.renewable(path));
	}

	public RequestedSomething requestRotatingSecret(String path) {
		return addRequestedSecret(RequestedSomething.rotating(path));
	}

	public RequestedSomething addRequestedSecret(RequestedSomething requestedSomething) {

		Assert.notNull(requestedSomething, "RequestedSomething must not be null");

		this.requestedSomethings.add(requestedSomething);

		if (initialized) {

			LeaseRenewalScheduler leaseRenewalScheduler = new LeaseRenewalScheduler(
					this.taskScheduler);
			this.renewals.put(requestedSomething, leaseRenewalScheduler);

			if (this.status == STATUS_STARTED) {
				start(requestedSomething, leaseRenewalScheduler);
			}
		}

		return requestedSomething;
	}

	public void start() {

		Assert.state(this.initialized, "Container is not initialized");
		Assert.state(this.status != STATUS_DESTROYED,
				"Container is destroyed and cannot be started");

		Map<RequestedSomething, LeaseRenewalScheduler> renewals = new HashMap<RequestedSomething, LeaseRenewalScheduler>(
				this.renewals);

		if (UPDATER.compareAndSet(this, STATUS_INITIAL, STATUS_STARTED)) {

			for (Entry<RequestedSomething, LeaseRenewalScheduler> entry : renewals
					.entrySet()) {
				start(entry.getKey(), entry.getValue());
			}
		}
	}

	private void start(RequestedSomething requestedSomething,
                       LeaseRenewalScheduler renewalScheduler) {

		VaultResponseSupport<Map<String, Object>> secrets = doGetSecrets(requestedSomething);

		if (secrets != null) {

			Lease lease = !StringUtils.hasText(secrets.getLeaseId()) ? Lease.none()
					: Lease.of(secrets.getLeaseId(), secrets.getLeaseDuration(),
							secrets.isRenewable());

			potentiallyScheduleLeaseRenewal(requestedSomething, lease, renewalScheduler);
			onSecretsObtained(requestedSomething, lease, secrets.getData());
		}
	}

	public void stop() {

		if (UPDATER.compareAndSet(this, STATUS_STARTED, STATUS_INITIAL)) {

			for (LeaseRenewalScheduler leaseRenewal : this.renewals.values()) {
				leaseRenewal.disableScheduleRenewal();
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!this.initialized) {

			super.afterPropertiesSet();

			this.initialized = true;

			if (this.taskScheduler == null) {

				ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
				scheduler.setDaemon(true);
				scheduler.setThreadNamePrefix(String.format("%s-%d-", getClass()
						.getSimpleName(), poolId.incrementAndGet()));
				scheduler.afterPropertiesSet();

				this.taskScheduler = scheduler;
				this.manageTaskScheduler = true;
			}

			for (RequestedSomething requestedSomething : requestedSomethings) {
				this.renewals.put(requestedSomething, new LeaseRenewalScheduler(
						this.taskScheduler));
			}
		}
	}

	@Override
	public void destroy() throws Exception {

		int status = this.status;

		if (status == STATUS_INITIAL || status == STATUS_STARTED) {

			if (UPDATER.compareAndSet(this, status, STATUS_DESTROYED)) {

				for (Entry<RequestedSomething, LeaseRenewalScheduler> entry : renewals
						.entrySet()) {

					Lease lease = entry.getValue().getLease();
					entry.getValue().disableScheduleRenewal();

					if (lease != null) {
						doRevokeLease(entry.getKey(), lease);
					}
				}

				if (manageTaskScheduler) {

					if (this.taskScheduler instanceof DisposableBean) {
						((DisposableBean) this.taskScheduler).destroy();
						this.taskScheduler = null;
					}
				}
			}
		}
	}

	void potentiallyScheduleLeaseRenewal(final RequestedSomething requestedSomething,
			final Lease lease, final LeaseRenewalScheduler leaseRenewal) {

		if (leaseRenewal.isLeaseRenewable(lease)) {

			if (log.isDebugEnabled()) {
				log.debug(String.format("Lease %s qualified for renewal",
						lease.getLeaseId()));
			}

			leaseRenewal.scheduleRenewal(new RenewLease() {

				@Override
				public Lease renewLease(Lease lease) {

					Lease newLease = doRenewLease(requestedSomething, lease);

					if (newLease == null) {
						return null;
					}

					potentiallyScheduleLeaseRenewal(requestedSomething, newLease,
							leaseRenewal);

					onAfterLeaseRenewed(requestedSomething, newLease);

					return newLease;
				}
			}, lease, getMinRenewalSeconds(), getExpiryThresholdSeconds());
		}
	}

	protected VaultResponseSupport<Map<String, Object>> doGetSecrets(
			org.springframework.vault.core.lease.domain.RequestedSomething requestedSomething) {

		try {
			return this.operations.read(requestedSomething.getPath());
		}
		catch (RuntimeException e) {

			onError(requestedSomething, Lease.none(), e);
			return null;
		}
	}

	protected Lease doRenewLease(org.springframework.vault.core.lease.domain.RequestedSomething requestedSomething, final Lease lease) {

		try {
			ResponseEntity<Map<String, Object>> entity = operations
					.doWithSession(new RestOperationsCallback<ResponseEntity<Map<String, Object>>>() {

						@Override
						@SuppressWarnings("unchecked")
						public ResponseEntity<Map<String, Object>> doWithRestOperations(
								RestOperations restOperations) {
							return (ResponseEntity) restOperations.exchange(
									"/sys/renew/{leaseId}", HttpMethod.PUT, null,
									Map.class, lease.getLeaseId());
						}
					});

			Map<String, Object> body = entity.getBody();
			String leaseId = (String) body.get("lease_id");
			Number leaseDuration = (Number) body.get("lease_duration");
			boolean renewable = (Boolean) body.get("renewable");

			if (leaseDuration == null || leaseDuration.intValue() < minRenewalSeconds) {
				onLeaseExpired(requestedSomething, lease);
				return null;
			}

			return Lease.of(leaseId, leaseDuration.longValue(), renewable);
		}
		catch (HttpStatusCodeException e) {

			if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				onLeaseExpired(requestedSomething, lease);
			}

			onError(requestedSomething,
					lease,
					new VaultException(String.format("Cannot renew lease: %s",
							VaultResponses.getError(e.getResponseBodyAsString()))));
		}
		catch (RuntimeException e) {
			onError(requestedSomething, lease, e);
		}

		return null;
	}

	protected void onLeaseExpired(org.springframework.vault.core.lease.domain.RequestedSomething requestedSomething, Lease lease) {

		super.onLeaseExpired(requestedSomething, lease);

		if (requestedSomething.getMode() == Mode.ROTATE) {
			start(requestedSomething, renewals.get(requestedSomething));
		}
	}

	/**
	 * Revoke the {@link Lease}.
	 *
	 * @param requestedSomething must not be {@literal null}.
	 * @param lease must not be {@literal null}.
	 */
	protected void doRevokeLease(org.springframework.vault.core.lease.domain.RequestedSomething requestedSomething, final Lease lease) {

		try {

			onBeforeLeaseRevocation(requestedSomething, lease);

			operations
					.doWithSession(new RestOperationsCallback<ResponseEntity<Map<String, Object>>>() {

						@Override
						@SuppressWarnings("unchecked")
						public ResponseEntity<Map<String, Object>> doWithRestOperations(
								RestOperations restOperations) {
							return (ResponseEntity) restOperations.exchange(
									"/sys/revoke/{leaseId}", HttpMethod.PUT, null,
									Map.class, lease.getLeaseId());
						}
					});

			onAfterLeaseRevocation(requestedSomething, lease);
		}
		catch (HttpStatusCodeException e) {
			onError(requestedSomething,
					lease,
					new VaultException(String.format("Cannot revoke lease: %s",
							VaultResponses.getError(e.getResponseBodyAsString()))));
		}
		catch (RuntimeException e) {
			onError(requestedSomething, lease, e);
		}
	}


	@CommonsLog
	static class LeaseRenewalScheduler {

		private final TaskScheduler taskScheduler;

		final AtomicReference<Lease> currentLeaseRef = new AtomicReference<Lease>();

		final Map<Lease, ScheduledFuture<?>> schedules = new ConcurrentHashMap<Lease, ScheduledFuture<?>>();

		/**
		 *
		 * @param taskScheduler must not be {@literal null}.
		 */
		LeaseRenewalScheduler(TaskScheduler taskScheduler) {
			this.taskScheduler = taskScheduler;
		}

		/**
		 * Schedule {@link Lease} renewal. Previously registered renewal tasks are
		 * canceled to prevent renewal of stale {@link Lease}s.
		 * @param renewLease strategy to renew a {@link Lease}.
		 * @param lease the current {@link Lease}.
		 * @param minRenewalSeconds minimum number of seconds before renewing a
		 * {@link Lease}. This is to prevent too many renewals in a very short timeframe.
		 * @param expiryThresholdSeconds number of seconds to renew before {@link Lease}.
		 * expires.
		 */
		void scheduleRenewal(final RenewLease renewLease, final Lease lease,
				final int minRenewalSeconds, final int expiryThresholdSeconds) {

			if (log.isDebugEnabled()) {
				log.debug(String.format(
						"Scheduling renewal for lease %s, lease duration %d",
						lease.getLeaseId(), lease.getLeaseDuration()));
			}

			Lease currentLease = this.currentLeaseRef.get();
			this.currentLeaseRef.set(lease);

			if (currentLease != null) {
				cancelSchedule(currentLease);
			}

			ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
					new Runnable() {

						@Override
						public void run() {

							try {

								schedules.remove(lease);

								if (currentLeaseRef.get() != lease) {
									log.debug("Current lease has changed. Skipping renewal");
									return;
								}

								if (log.isDebugEnabled()) {
									log.debug(String.format("Renewing lease %s",
											lease.getLeaseId()));
								}

								currentLeaseRef.compareAndSet(lease,
										renewLease.renewLease(lease));
							}
							catch (Exception e) {
								log.error(
										String.format("Cannot renew lease %s",
												lease.getLeaseId()), e);
							}
						}
					},
					new OneShotTrigger(getRenewalSeconds(lease, minRenewalSeconds,
							expiryThresholdSeconds)));

			schedules.put(lease, scheduledFuture);
		}

		private void cancelSchedule(Lease lease) {

			ScheduledFuture<?> scheduledFuture = schedules.get(lease);
			if (scheduledFuture != null) {

				if (log.isDebugEnabled()) {
					log.debug(String.format(
							"Canceling previously registered schedule for lease %s",
							lease.getLeaseId()));
				}

				scheduledFuture.cancel(false);
			}
		}

		/**
		 * Disables schedule for already scheduled renewals.
		 */
		void disableScheduleRenewal() {

			currentLeaseRef.set(null);
			Set<Lease> leases = new HashSet<Lease>(schedules.keySet());

			for (Lease lease : leases) {
				cancelSchedule(lease);
				schedules.remove(lease);
			}
		}

		private long getRenewalSeconds(Lease lease, int minRenewalSeconds,
				int expiryThresholdSeconds) {
			return Math.max(minRenewalSeconds, lease.getLeaseDuration()
					- expiryThresholdSeconds);
		}

		private boolean isLeaseRenewable(Lease lease) {
			return lease != null && lease.isRenewable();
		}

		public Lease getLease() {
			return currentLeaseRef.get();
		}
	}

	/**
	 * This one-shot trigger creates only one execution time to trigger an execution only
	 * once.
	 */
	static class OneShotTrigger implements Trigger {

		private static final AtomicIntegerFieldUpdater<OneShotTrigger> UPDATER = AtomicIntegerFieldUpdater
				.newUpdater(OneShotTrigger.class, "status");

		private static final int STATUS_ARMED = 0;
		private static final int STATUS_FIRED = 1;

		// see AtomicIntegerFieldUpdater UPDATER
		private volatile int status = 0;

		private final long seconds;

		OneShotTrigger(long seconds) {
			this.seconds = seconds;
		}

		@Override
		public Date nextExecutionTime(TriggerContext triggerContext) {

			if (UPDATER.compareAndSet(this, STATUS_ARMED, STATUS_FIRED)) {
				return new Date(System.currentTimeMillis()
						+ TimeUnit.SECONDS.toMillis(seconds));
			}

			return null;
		}
	}

	interface RenewLease {
		Lease renewLease(Lease lease) throws RuntimeException;
	}
}
