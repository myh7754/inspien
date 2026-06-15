package co.inspien.assignment.scenario1;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("주문 적재 리포지토리")
class OrderRepositoryTest {

    @Mock JdbcTemplate jdbc;

    // IdGenerator는 의존성 없는 순수 클래스라 실제 객체 사용
    private OrderRepository repository() {
        return new OrderRepository(jdbc, new IdGenerator());
    }

    private static final OrderRecord RECORD =
            new OrderRecord("USER1", "홍길동", "서울특별시 금천구", "ITEM1", "청바지", 21000);

    @Test
    @DisplayName("채번 재시도가 모두 소진되면 ORDER_PERSISTENCE_ERROR로 실패한다")
    void throwsPersistenceErrorWhenIdRetriesExhausted() {
        // 어떤 ID로 INSERT해도 항상 복합키 충돌이 나는 상황 모사
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DuplicateKeyException("dup"));

        assertThatThrownBy(() -> repository().saveAll(List.of(RECORD), "KEY999"))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_PERSISTENCE_ERROR);
    }
}
