package cc.before30.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * User: before30 
 * Date: 2017. 5. 17.
 * Time: PM 12:05
 */
@ConfigurationProperties(prefix = "service")
@Setter
@Getter
public class AppInfoProperties {
	private String name;
	private String version;
}
