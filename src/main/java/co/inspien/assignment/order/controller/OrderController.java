package co.inspien.assignment.order.controller;
import co.inspien.assignment.order.service.OrderService;
import co.inspien.assignment.order.dto.OrderResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 실시간 주문 수신 엔드포인트(FR-S1-09-a).
 * XML 본문을 받아 {@link OrderService}에 위임하고 결과 JSON을 반환한다.
 */
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/orders",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public OrderResponse create(@RequestBody String xml) {
        return OrderResponse.success(orderService.process(xml));
    }
}
