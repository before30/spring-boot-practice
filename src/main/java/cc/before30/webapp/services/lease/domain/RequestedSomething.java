package cc.before30.webapp.services.lease.domain;

import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@Getter
@ToString
public class RequestedSomething {

	private final String path;
	private final Mode mode;

	private RequestedSomething(String path, Mode mode) {
		Assert.hasText(path, "Path must not be null or empty");
		Assert.isTrue(!path.startsWith("/"), "Path name must not start with a slash (/)");
		this.path = path;
		this.mode = mode;
	}

	public static RequestedSomething renewable(String path) {
		return new RequestedSomething(path, Mode.RENEW);
	}

	public static RequestedSomething rotating(String path) {
		return new RequestedSomething(path, Mode.ROTATE);
	}

	public static RequestedSomething from(Mode mode, String path) {

		Assert.notNull(mode, "Mode must not be null");

		return mode == Mode.ROTATE ? rotating(path) : renewable(path);
	}

	public enum Mode {

		/**
		 * Renew lease of the requested secret until secret expires its max lease time.
		 */
		RENEW,

		/**
		 * Renew lease of the requested secret. Obtains new secret along a new lease once
		 * the previous lease expires its max lease time.
		 */
		ROTATE;
	}
}
