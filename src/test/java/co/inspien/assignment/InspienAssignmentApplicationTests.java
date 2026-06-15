package co.inspien.assignment;

import co.inspien.assignment.bootstrap.ProvisioningResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 전체 컨텍스트 스모크 테스트.
 *
 * <p>{@code test} 프로파일로 실 제공 API 호출을 끄고(BootstrapConfig의 fetch 빈은 {@code @Profile("!test")}),
 * 캔드 {@link ProvisioningResponse}를 주입해 네트워크 없이 빈 그래프 조립만 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(InspienAssignmentApplicationTests.StubProvisioning.class)
class InspienAssignmentApplicationTests {

    @TestConfiguration(proxyBeanMethods = false)
    static class StubProvisioning {
        @Bean
        ProvisioningResponse provisioningResponse() {
            // applicantKey만 있으면 ApplicantContext 조립에 충분(나머지 접속정보는 부팅 시 미사용)
            return new ProvisioningResponse("KEY999", null, null, null, null);
        }
    }

    @Test
    void contextLoads() {
    }

}
