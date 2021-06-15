package me.jinmin.jpashop.order;

import lombok.RequiredArgsConstructor;
import me.jinmin.jpashop.order.api.OrderFlatDto;
import me.jinmin.jpashop.order.api.OrderItemQueryDto;
import me.jinmin.jpashop.order.api.OrderQueryDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDto(){
        List<OrderQueryDto> results = findOrders();

        results.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return results;
    }

    //1:N(컬렉션 조회)
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new me.jinmin.jpashop.order.api.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) from OrderItem oi" +
                " join fetch oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    //1:N을 제외한 나머지 조회
    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new me.jinmin.jpashop.order.api.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization() {

        List<OrderQueryDto> results = findOrders();

        List<Long> orderIds = getOrderIds(results);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        results.forEach(o->{
            o.setOrderItems(orderItemMap.get(orderIds));
        });

        return results;
    }

    private List<Long> getOrderIds(List<OrderQueryDto> results) {
        List<Long> orderIds = results
                .stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new me.jinmin.jpashop.order.api.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        //key = id, value = id에 해당하는 OrderItemQueryDto
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems
                .stream()
                .collect(Collectors.groupingBy(o -> o.getOrderId()));
        return orderItemMap;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery("select new me.jinmin.jpashop.order.api.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d" +
                " join o.orderItems oi" +
                " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
