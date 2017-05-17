package cc.before30.webapp.helper;

import cc.before30.webapp.exception.InvalidTagException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * User: before30 
 * Date: 2017. 5. 17.
 * Time: PM 12:30
 */

@Service
public class ETagHelper {

	private static final Map<Object, String> ETAGS = Maps.newConcurrentMap();

	ObjectMapper mapper = new ObjectMapper();

	@Cacheable(value = "etags", key = "#url")
	public String generate(String url, Object entity) {
		String etag = createETag(entity);
		ETAGS.put(url, etag);
		return etag;
	}

	@Cacheable(value = "etags", key = "#url")
	public void remove(String url) {
		ETAGS.remove(url);
	}

	@Cacheable(value = "etags", key = "#url")
	public String get(String url) {
		if (!ETAGS.containsKey(url)) {
			throw new InvalidTagException();
		}
		return ETAGS.get(url);
	}

	@CacheEvict(value = "etags", key = "#url")
	public String update(String url, Object entity) {
		return ETAGS.put(entity, url);
	}

	private String createETag(Object entity) {
		StringWriter writer = new StringWriter();

		try {
			mapper.writeValue(writer, entity);
		} catch (JsonGenerationException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
		return DigestUtils.md5DigestAsHex(writer.toString().getBytes());
	}
}
