package co.inspien.assignment.bootstrap;

import co.inspien.assignment.order.service.ReceiptFileBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;

/**
 * 부팅 배선 설정.
 *
 * <p>제공 API에서 받은 접속/식별 정보를 빈으로 노출한다(B안). 실제 fetch는
 * {@code @Profile("!test")}로 격리해, 네트워크 없는 단위 컨텍스트 로딩(test 프로파일)에서는
 * 테스트가 캔드 {@link ProvisioningResponse}를 주입하도록 한다.
 */
@Configuration
public class BootstrapConfig {

    /** 영수증 파일명 타임스탬프 생성용. 테스트는 고정 Clock으로 교체 가능. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** 부팅 시 1회 제공 API 호출. 같은 응답에서 applicantKey·(추후)FTP 접속정보를 파생한다. */
    @Bean
    @Profile("!test")
    ProvisioningResponse provisioningResponse(ProvisioningClient client) {
        return client.fetch();
    }

    @Bean
    ApplicantContext applicantContext(ProvisioningResponse response, ProvisioningProperties props) {
        return new ApplicantContext(response.applicantKey(), props.applicant().name());
    }

    @Bean
    ReceiptFileBuilder receiptFileBuilder(Clock clock, ApplicantContext applicant) {
        return new ReceiptFileBuilder(clock, applicant.name());
    }
}
