package me.jinmin.jpashop.order.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.jinmin.jpashop.domain.Address;
import me.jinmin.jpashop.order.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    //v1 : 엔티티 직접 노출 X

    //v2 : dto 변환 (컬렉션)
    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> all = orderRepository.findAll(new OrderSearch());

        List<OrderDto> collect = all.stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(collect);
    }

    //v3 : dto 변환 + 페치 조인
    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> all = orderRepository.findAllWithItem();

        List<OrderDto> collect = all.stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(collect);
    }

    //v3.1 : dto 변환 + 페치 조인 + 페이징(단, 컬렉션 페치 조인에는 페이징 X)
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_1(@RequestParam(value = "offset", defaultValue = "0") int offset,
                             @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> all = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> collect = all.stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(collect);
    }

    //v4 : JPA에서 DTO 직접 조회
    @GetMapping("/api/v4/orders")
    public Result ordersV4() {
        return new Result(orderQueryRepository.findOrderQueryDto());
    }

    //v5 : JPA에서 DTO 직접 조회, 컬렉션 조회 최적화
    @GetMapping("/api/v5/orders")
    public Result ordersV5() {
        return new Result(orderQueryRepository.findAllByDto_optimization());

    }

    //v6 플랫데이터 최적화 (Order + OrderItem을 한 번에)
    @GetMapping("/api/v6/orders")
    public Result ordersV6(){
        return new Result(orderQueryRepository.findAllByDto_flat());
    }

    //v6.1
    @GetMapping("/api/v6_1/orders")
    public Result ordersV6_1() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        List<OrderQueryDto> results = flats
                .stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());

        return new Result(results);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
