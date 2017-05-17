package cc.before30.webapp.services.lease.event;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;

public class AfterSomethingLeaseRenewedEvent extends SomethingLeaseEvent {

	private static final long serialVersionUID = 1L;

	public AfterSomethingLeaseRenewedEvent(RequestedSomething requestedSomething, Lease lease) {
		super(requestedSomething, lease);
	}
}
