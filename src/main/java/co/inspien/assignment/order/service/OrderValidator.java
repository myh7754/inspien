package co.inspien.assignment.order.service;
import co.inspien.assignment.order.dto.OrderRecord;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderValidator {

    public void validate(List<OrderRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new InspienException(ErrorCode.VALIDATION_ERROR, "주문 레코드가 없습니다");
        }
        for (OrderRecord r : records) {
            requireNotBlank(r.userId(),   "USER_ID");
            requireNotBlank(r.name(),     "NAME");
            requireNotBlank(r.address(),  "ADDRESS");
            requireNotBlank(r.itemId(),   "ITEM_ID");
            requireNotBlank(r.itemName(), "ITEM_NAME");
            if (r.price() <= 0) {
                throw new InspienException(ErrorCode.VALIDATION_ERROR, "PRICE는 1 이상이어야 합니다: " + r.price());
            }
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InspienException(ErrorCode.VALIDATION_ERROR, fieldName + " 필드가 비어 있습니다");
        }
    }
}
