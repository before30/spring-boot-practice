package cc.before30.webapp.services.lease.event;

public abstract class LeaseListenerAdapter implements LeaseListener, LeaseErrorListener {

	@Override
	public void onLeaseEvent(SomethingLeaseEvent leaseEvent) {
		// empty listener method
	}

	@Override
	public void onLeaseError(SomethingLeaseEvent leaseEvent, Exception exception) {
		// empty listener method
	}
}
