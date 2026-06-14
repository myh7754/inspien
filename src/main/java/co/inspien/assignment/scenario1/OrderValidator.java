package co.inspien.assignment.scenario1;

import java.util.List;

public class OrderValidator {

    public void validate(List<OrderRecord> records) {
        if (records == null || records.isEmpty()) {
            throw new ValidationException("주문 레코드가 없습니다");
        }
        for (OrderRecord r : records) {
            requireNotBlank(r.userId(),   "USER_ID");
            requireNotBlank(r.name(),     "NAME");
            requireNotBlank(r.address(),  "ADDRESS");
            requireNotBlank(r.itemId(),   "ITEM_ID");
            requireNotBlank(r.itemName(), "ITEM_NAME");
            if (r.price() <= 0) {
                throw new ValidationException("PRICE는 1 이상이어야 합니다: " + r.price());
            }
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " 필드가 비어 있습니다");
        }
    }
}
