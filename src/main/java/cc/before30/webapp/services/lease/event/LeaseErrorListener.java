package cc.before30.webapp.services.lease.event;

public interface LeaseErrorListener {

	void onLeaseError(SomethingLeaseEvent leaseEvent, Exception exception);
}
