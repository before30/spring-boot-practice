package cc.before30;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@Slf4j
public class SpringBootPracticeApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootPracticeApplication.class, args);
	}

	@Value("${service.version}")
	public String appVersion;

	@Value("${service.name}")
	public String appName;


	@Override
	public void run(String... args) throws Exception {
		log.info("{}", appName);
		log.info("{}, {}", appVersion, appName);
	}
}
