package co.inspien.assignment;

import co.inspien.assignment.bootstrap.ProvisioningProperties;
import co.inspien.assignment.order.config.FtpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ProvisioningProperties.class, FtpProperties.class})
public class InspienAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(InspienAssignmentApplication.class, args);
	}

}
