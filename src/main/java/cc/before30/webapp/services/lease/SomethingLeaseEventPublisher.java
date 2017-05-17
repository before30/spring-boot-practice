/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.before30.webapp.services.lease;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;
import cc.before30.webapp.services.lease.event.*;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class SomethingLeaseEventPublisher implements InitializingBean {

	private final Set<LeaseListener> leaseListeners = new CopyOnWriteArraySet<LeaseListener>();

	private final Set<LeaseErrorListener> leaseErrorListeners = new CopyOnWriteArraySet<LeaseErrorListener>();

	public void addLeaseListener(LeaseListener listener) {

		Assert.notNull(listener, "LeaseListener must not be null");

		this.leaseListeners.add(listener);
	}

	public void removeLeaseListener(LeaseListener listener) {
		this.leaseListeners.remove(listener);
	}

	public void addErrorListener(LeaseErrorListener listener) {

		Assert.notNull(listener, "LeaseListener must not be null");

		this.leaseErrorListeners.add(listener);
	}

	public void removeLeaseErrorListener(LeaseErrorListener listener) {
		this.leaseErrorListeners.remove(listener);
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (this.leaseErrorListeners.isEmpty()) {
			addErrorListener(LoggingErrorListener.INSTANCE);
		}
	}

	protected void onSecretsObtained(RequestedSomething requestedSomething, Lease lease,
									 Map<String, Object> body) {

		for (LeaseListener leaseListener : leaseListeners) {
			leaseListener.onLeaseEvent(new SomethingLeaseCreatedEvent(requestedSomething,
					lease, body));
		}
	}

	protected void onAfterLeaseRenewed(RequestedSomething requestedSomething, Lease lease) {

		for (LeaseListener leaseListener : leaseListeners) {
			leaseListener.onLeaseEvent(new AfterSomethingLeaseRenewedEvent(requestedSomething,
					lease));
		}
	}

	protected void onBeforeLeaseRevocation(RequestedSomething requestedSomething, Lease lease) {

		for (LeaseListener leaseListener : leaseListeners) {
			leaseListener.onLeaseEvent(new BeforeSomethingLeaseRevocationEvent(
                    requestedSomething, lease));
		}
	}

	protected void onAfterLeaseRevocation(RequestedSomething requestedSomething, Lease lease) {

		for (LeaseListener leaseListener : leaseListeners) {
			leaseListener.onLeaseEvent(new AfterSomethingLeaseRevocationEvent(
                    requestedSomething, lease));
		}
	}

	protected void onLeaseExpired(RequestedSomething requestedSomething, Lease lease) {

		for (LeaseListener leaseListener : leaseListeners) {
			leaseListener
					.onLeaseEvent(new SomethingLeaseExpiredEvent(requestedSomething, lease));
		}
	}

	protected void onError(RequestedSomething requestedSomething, Lease lease, Exception e) {

		for (LeaseErrorListener leaseErrorListener : leaseErrorListeners) {
			leaseErrorListener.onLeaseError(new SomethingLeaseErrorEvent(requestedSomething,
					lease, e), e);
		}
	}

	@CommonsLog
	public enum LoggingErrorListener implements LeaseErrorListener {

		INSTANCE;

		@Override
		public void onLeaseError(SomethingLeaseEvent leaseEvent, Exception exception) {
			log.warn(
					String.format("[%s] %s %s", leaseEvent.getSource(),
							leaseEvent.getLease(), exception.getMessage()), exception);
		}
	}
}
