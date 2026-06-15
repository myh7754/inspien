package co.inspien.assignment;

import co.inspien.assignment.bootstrap.ProvisioningProperties;
import co.inspien.assignment.scenario1.FtpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ProvisioningProperties.class, FtpProperties.class})
public class InspienAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(InspienAssignmentApplication.class, args);
	}

}
