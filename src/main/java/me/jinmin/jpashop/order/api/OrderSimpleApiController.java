package me.jinmin.jpashop.order.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.jinmin.jpashop.domain.Address;
import me.jinmin.jpashop.order.Order;
import me.jinmin.jpashop.order.OrderRepository;
import me.jinmin.jpashop.order.OrderSearch;
import me.jinmin.jpashop.order.OrderStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    //v1 : 쓰지말자.
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        return orderRepository.findAll(new OrderSearch());
    }

    //v2 : DTO변환
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2(){
        List<Order> orders = orderRepository.findAll(new OrderSearch());

        List<SimpleOrderDto> collect = orders
                .stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(collect);
    }

    //v3 : DTO + 페치 조인
    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> collect = orders
                .stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());

        return new Result(collect);
    }

    //v4 : JPA에서 DTO 바로조회
    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4(){
        List<OrderSimpleQueryDto> orderDto = orderRepository.findOrderDto();

        return new Result(orderDto);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus status;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.status = order.getStatus();
            this.address = order.getDelivery().getAddress();
        }
    }
}
