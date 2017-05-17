package cc.before30.webapp.services.lease.event;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;

public class AfterSomethingLeaseRevocationEvent extends SomethingLeaseEvent {

	private static final long serialVersionUID = 1L;

	public AfterSomethingLeaseRevocationEvent(RequestedSomething requestedSomething, Lease lease) {
		super(requestedSomething, lease);
	}
}
