package me.jinmin.jpashop.order;

import lombok.Getter;
import lombok.Setter;
import me.jinmin.jpashop.item.Item;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue
    private Long id;

    private int orderPrice;
    private int count;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    //생성 메서드
    public static OrderItem createOrderItems(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    //비지니스 로직
    //재고 원복
    public void cancel() {
        getItem().addStock(count);
    }

    //조회
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
