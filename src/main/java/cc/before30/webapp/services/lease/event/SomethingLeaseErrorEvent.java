package cc.before30.webapp.services.lease.event;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;

public class SomethingLeaseErrorEvent extends SomethingLeaseEvent {

	private static final long serialVersionUID = 1L;

	private final Throwable exception;

	public SomethingLeaseErrorEvent(RequestedSomething requestedSomething, Lease lease,
									Throwable exception) {
		super(requestedSomething, lease);
		this.exception = exception;
	}

	public Throwable getException() {
		return exception;
	}
}
