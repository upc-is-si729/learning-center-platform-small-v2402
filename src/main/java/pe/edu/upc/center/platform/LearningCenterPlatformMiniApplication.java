package pe.edu.upc.center.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LearningCenterPlatformMiniApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningCenterPlatformMiniApplication.class, args);
	}

}
