package cc.before30.webapp.services.lease.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;

@EqualsAndHashCode
@ToString
public class Lease {

	private static final Lease NONE = new Lease(null, 0, false);
	private final String leaseId;
	private final long leaseDuration;
	private final boolean renewable;

	private Lease(String leaseId, long leaseDuration, boolean renewable) {
		this.leaseId = leaseId;
		this.leaseDuration = leaseDuration;
		this.renewable = renewable;
	}

	public static Lease of(String leaseId, long leaseDuration, boolean renewable) {
		Assert.hasText(leaseId, "LeaseId must not be empty");
		return new Lease(leaseId, leaseDuration, renewable);
	}

	public static Lease none() {
		return NONE;
	}

	public String getLeaseId() {
		return leaseId;
	}

	public long getLeaseDuration() {
		return leaseDuration;
	}

	public boolean isRenewable() {
		return renewable;
	}
}
