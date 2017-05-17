package cc.before30.webapp.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * User: before30 
 * Date: 2017. 5. 17.
 * Time: PM 12:51
 */
@Setter
@Getter
public class PolicyInvalidException extends RuntimeException {
	private static final long serialVersionUID = -8944332638840928196L;

	public static enum ErrorCode {
		TOO_MANY_DRIVERS(41201),
		TOO_MANY_VEHICLES(41202),
		NON_NULL_POLICY_NUM(41203),
		NON_NULL_QUOTE(41204);

		final int code;

		ErrorCode(int code) {
			this.code = code;
		}

		public int value() {
			return this.code;
		}
	}

	ErrorCode errorCode;

	public PolicyInvalidException(ErrorCode errorCode) {
		super(errorCode.toString() + ", code: " + errorCode.value());
		this.errorCode = errorCode;
	}
}
