package co.inspien.assignment;

import co.inspien.assignment.bootstrap.ProvisioningProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

// DB 접속정보는 제공 API 복호화(3단계) 후 확정되므로, 그 전까지 DataSource 자동설정을 제외해
// 앱이 정상 부팅되도록 한다. 실제 DB 연동 시 이 exclude를 제거한다.
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableConfigurationProperties(ProvisioningProperties.class)
public class InspienAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(InspienAssignmentApplication.class, args);
	}

}
