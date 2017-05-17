package cc.before30.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by before30 on 16/05/2017.
 */

@Component
@Slf4j
public class CustomHealthCheckComponent implements HealthIndicator {

	@Autowired AppInfoProperties appInfoProperties;

	@Override
	public Health health() {
		Map<String, String> map = new HashMap<>();
		map.put("name", appInfoProperties.getName());
		map.put("version", appInfoProperties.getVersion());

		return Health
			.up()
			.withDetail("app", map)
			.build();
	}
}
