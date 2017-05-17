package cc.before30.webapp.services.lease.event;


import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;

public class SomethingLeaseExpiredEvent extends SomethingLeaseEvent {

	private static final long serialVersionUID = 1L;

	public SomethingLeaseExpiredEvent(RequestedSomething requestedSomething, Lease lease) {
		super(requestedSomething, lease);
	}
}
