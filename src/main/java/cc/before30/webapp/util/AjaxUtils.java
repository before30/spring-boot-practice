package cc.before30.webapp.util;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;

/**
 * User: before30 
 * Date: 2017. 5. 17.
 * Time: PM 12:55
 */
public class AjaxUtils {
	public static final String X_REQUESTED_WITH = "X-Requested-With";
	public static final String AJAXUPLOAD = "ajaxUpload";
	public static final String XMLHTTPREQUEST = "XMLHttpRequest";


	public static boolean isAjaxRequest(WebRequest webRequest) {
		String requestedWith = webRequest.getHeader(X_REQUESTED_WITH);
		return !Objects.isNull(requestedWith)? XMLHTTPREQUEST.equals(requestedWith) : false;
	}

	public static boolean isAjaxUploadRequest(WebRequest webRequest) {
		return !Objects.isNull(webRequest.getParameter(AJAXUPLOAD));
	}

	private AjaxUtils() {}
}
