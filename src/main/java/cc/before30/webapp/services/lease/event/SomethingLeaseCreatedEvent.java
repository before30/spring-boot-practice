package cc.before30.webapp.services.lease.event;

import cc.before30.webapp.services.lease.domain.Lease;
import cc.before30.webapp.services.lease.domain.RequestedSomething;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SomethingLeaseCreatedEvent extends SomethingLeaseEvent {

	private static final long serialVersionUID = 1L;

	private final Map<String, Object> secrets;

	public SomethingLeaseCreatedEvent(RequestedSomething requestedSomething, Lease lease,
									  Map<String, Object> secrets) {

		super(requestedSomething, lease);
		this.secrets = Collections.unmodifiableMap(new HashMap<String, Object>(secrets));
	}

	public Map<String, Object> getSecrets() {
		return secrets;
	}
}
