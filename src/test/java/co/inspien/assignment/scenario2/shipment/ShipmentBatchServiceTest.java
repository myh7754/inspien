package co.inspien.assignment.scenario2.shipment;

import co.inspien.assignment.bootstrap.ApplicantContext;
import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("운송 배치 오케스트레이션")
class ShipmentBatchServiceTest {

    private static final String KEY = "KEY999";
    private static final ApplicantContext APPLICANT = new ApplicantContext(KEY, "문영훈");

    private static final ShipmentRecord REC1 = new ShipmentRecord("A111", "ITEM1", "서울 금천구");
    private static final ShipmentRecord REC2 = new ShipmentRecord("A222", "ITEM2", "서울 강남구");
    private static final ShipmentRecord REC3 = new ShipmentRecord("A333", "ITEM3", "부산 해운대구");

    @Mock ShipmentRepository repository;

    ShipmentBatchService service;

    @BeforeEach
    void setUp() {
        service = new ShipmentBatchService(repository, APPLICANT);
    }

    @Test
    @DisplayName("정상: 모든 미전송건을 적재하고 STATUS='Y'로 전이한다")
    void run_allSucceed_marksEverySent() {
        when(repository.findUnsent(KEY)).thenReturn(List.of(REC1, REC2, REC3));
        when(repository.insertShipment(any(), anyString())).thenReturn("S001");

        ShipmentBatchService.BatchResult result = service.run();

        assertThat(result).isEqualTo(new ShipmentBatchService.BatchResult(3, 3, 0));
        verify(repository).markSent("A111", KEY);
        verify(repository).markSent("A222", KEY);
        verify(repository).markSent("A333", KEY);
    }

    @Test
    @DisplayName("건별 격리: 한 건 적재 실패해도 나머지는 처리되고 실패 건은 STATUS 미전이")
    void run_oneFails_isolatesAndContinues() {
        when(repository.findUnsent(KEY)).thenReturn(List.of(REC1, REC2, REC3));
        when(repository.insertShipment(REC1, KEY)).thenReturn("S001");
        when(repository.insertShipment(REC2, KEY))
                .thenThrow(new InspienException(ErrorCode.SHIPMENT_PERSISTENCE_ERROR, "적재 실패"));
        when(repository.insertShipment(REC3, KEY)).thenReturn("S003");

        ShipmentBatchService.BatchResult result = service.run();

        assertThat(result).isEqualTo(new ShipmentBatchService.BatchResult(3, 2, 1));
        verify(repository).markSent("A111", KEY);
        verify(repository).markSent("A333", KEY);
        verify(repository, never()).markSent("A222", KEY); // 실패 건은 'N' 유지
    }

    @Test
    @DisplayName("미전송건이 없으면 아무 적재도 하지 않는다")
    void run_emptyTargets_doesNothing() {
        when(repository.findUnsent(KEY)).thenReturn(List.of());

        ShipmentBatchService.BatchResult result = service.run();

        assertThat(result).isEqualTo(new ShipmentBatchService.BatchResult(0, 0, 0));
        verify(repository, never()).insertShipment(any(), anyString());
        verify(repository, never()).markSent(anyString(), anyString());
    }
}
