package co.inspien.assignment.scenario2.shipment;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("운송 건별 처리 (트랜잭션 단위)")
class ShipmentItemProcessorTest {

    private static final String KEY = "KEY999";
    private static final ShipmentRecord REC = new ShipmentRecord("A111", "ITEM1", "서울 금천구");

    @Mock ShipmentRepository repository;

    @Test
    @DisplayName("적재 → 상태전이 순서로 호출하고 SHIPMENT_ID를 반환한다")
    void processOne_insertsThenMarksSent() {
        ShipmentItemProcessor processor = new ShipmentItemProcessor(repository);
        when(repository.insertShipment(REC, KEY)).thenReturn("S001");

        String shipmentId = processor.processOne(REC, KEY);

        assertThat(shipmentId).isEqualTo("S001");
        InOrder order = inOrder(repository);
        order.verify(repository).insertShipment(REC, KEY);
        order.verify(repository).markSent("A111", KEY);
    }

    @Test
    @DisplayName("적재 실패 시 상태전이를 호출하지 않고 예외를 전파한다 (트랜잭션 롤백)")
    void processOne_insertFails_doesNotMarkSent() {
        ShipmentItemProcessor processor = new ShipmentItemProcessor(repository);
        when(repository.insertShipment(REC, KEY))
                .thenThrow(new InspienException(ErrorCode.SHIPMENT_PERSISTENCE_ERROR, "적재 실패"));

        assertThatThrownBy(() -> processor.processOne(REC, KEY))
                .isInstanceOf(InspienException.class);

        verify(repository, never()).markSent(anyString(), anyString());
    }
}
