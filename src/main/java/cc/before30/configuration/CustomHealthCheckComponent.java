package cc.before30.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Created by before30 on 16/05/2017.
 */

@Component
@Slf4j
public class CustomHealthCheckComponent implements HealthIndicator {

	@Override
	public Health health() {
		log.info("health!!!");
		return Health.up().withDetail("hello", "custom").build();
	}
}
