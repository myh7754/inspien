package co.inspien.assignment.scenario1;

import co.inspien.assignment.bootstrap.ProvisioningClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ORDER_TB 연결성 확인 통합테스트.
 *
 * 주의: APPLICANT 계정은 ORDER_TB에 INSERT/SELECT/UPDATE만 있고 DELETE 권한이 없다(공유 테이블).
 * 따라서 새 INSERT로 검증하면 지울 수 없는 테스트 데이터가 쌓인다.
 * INSERT 동작 자체는 이미 확인되었으므로, 여기서는 테이블에 더 쓰지 않고
 * 우리 APPLICANT_KEY로 조회가 되는지(연결성·복합키 스코프)만 검증한다.
 * 실제 적재+롤백 원자성 검증은 OrderService 트랜잭션 테스트에서 다룬다.
 *
 * 실행: ./gradlew integrationTest
 */
@Tag("integration")
@SpringBootTest
@DisplayName("주문 DB 연결성 통합")
class OrderRepositoryIT {

    private static final Logger log = LoggerFactory.getLogger(OrderRepositoryIT.class);

    @Autowired ProvisioningClient provisioningClient;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("내 APPLICANT_KEY로 ORDER_TB를 조회할 수 있다(연결성·복합키 스코프)")
    void can_query_order_tb_scoped_by_applicant_key() {
        String applicantKey = provisioningClient.fetch().applicantKey();

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ORDER_TB WHERE APPLICANT_KEY = ?",
                Integer.class, applicantKey);

        assertThat(count).isNotNull().isGreaterThanOrEqualTo(0);
        log.info("내 APPLICANT_KEY 주문 행 수: {}", count);

        // 조회되는 행이 있으면 구조가 기대대로인지(컬럼 존재) 확인
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT ORDER_ID, USER_ID, STATUS FROM ORDER_TB WHERE APPLICANT_KEY = ?",
                applicantKey);
        rows.forEach(row -> {
            assertThat(row).containsKeys("ORDER_ID", "USER_ID", "STATUS");
            log.info("  ORDER_ID={}, USER_ID={}, STATUS={}",
                    row.get("ORDER_ID"), row.get("USER_ID"), row.get("STATUS"));
        });
    }
}
