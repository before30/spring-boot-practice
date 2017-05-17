package cc.before30.webapp.services.lease.event;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;
import org.springframework.context.ApplicationEvent;

public abstract class SomethingLeaseEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final Lease lease;

	protected SomethingLeaseEvent(RequestedSomething requestedSomething, Lease lease) {
		super(requestedSomething);

		this.lease = lease;
	}

	@Override
	public RequestedSomething getSource() {
		return (RequestedSomething) super.getSource();
	}

	public Lease getLease() {
		return lease;
	}
}
