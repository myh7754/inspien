package co.inspien.assignment.order.service;
import co.inspien.assignment.order.dto.OrderRecord;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("주문 검증기")
class OrderValidatorTest {

    private final OrderValidator validator = new OrderValidator();

    private static OrderRecord record(String userId, String name, String address,
                                      String itemId, String itemName, int price) {
        return new OrderRecord(userId, name, address, itemId, itemName, price);
    }

    @Test
    @DisplayName("모든 필드가 유효한 레코드 목록은 예외 없이 통과한다")
    void valid_records_pass_without_exception() {
        List<OrderRecord> records = List.of(
                record("USER01", "홍길동", "서울시 강남구", "ITEM01", "청바지", 10000)
        );
        assertThatNoException().isThrownBy(() -> validator.validate(records));
    }

    @Test
    @DisplayName("레코드가 하나도 없으면 VALIDATION_ERROR로 거부한다")
    void empty_list_throws_validation_exception() {
        assertThatThrownBy(() -> validator.validate(List.of()))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("USER_ID가 공백이면 VALIDATION_ERROR로 거부한다")
    void blank_userId_throws_validation_exception() {
        List<OrderRecord> records = List.of(
                record("", "홍길동", "서울", "ITEM01", "청바지", 10000)
        );
        assertThatThrownBy(() -> validator.validate(records))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("NAME이 공백이면 VALIDATION_ERROR로 거부한다")
    void blank_name_throws_validation_exception() {
        List<OrderRecord> records = List.of(
                record("USER01", "  ", "서울", "ITEM01", "청바지", 10000)
        );
        assertThatThrownBy(() -> validator.validate(records))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("ADDRESS가 공백이면 VALIDATION_ERROR로 거부한다")
    void blank_address_throws_validation_exception() {
        List<OrderRecord> records = List.of(
                record("USER01", "홍길동", "", "ITEM01", "청바지", 10000)
        );
        assertThatThrownBy(() -> validator.validate(records))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("ITEM_ID가 공백이면 VALIDATION_ERROR로 거부한다")
    void blank_itemId_throws_validation_exception() {
        List<OrderRecord> records = List.of(
                record("USER01", "홍길동", "서울", "", "청바지", 10000)
        );
        assertThatThrownBy(() -> validator.validate(records))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("ITEM_NAME이 공백이면 VALIDATION_ERROR로 거부한다")
    void blank_itemName_throws_validation_exception() {
        List<OrderRecord> records = List.of(
                record("USER01", "홍길동", "서울", "ITEM01", "", 10000)
        );
        assertThatThrownBy(() -> validator.validate(records))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("PRICE가 0 이하면 VALIDATION_ERROR로 거부한다")
    void non_positive_price_throws_validation_exception() {
        List<OrderRecord> records = List.of(
                record("USER01", "홍길동", "서울", "ITEM01", "청바지", 0)
        );
        assertThatThrownBy(() -> validator.validate(records))
                .isInstanceOf(InspienException.class)
                .extracting(e -> ((InspienException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}
