package me.jinmin.jpashop.order;

import lombok.RequiredArgsConstructor;
import me.jinmin.jpashop.item.Item;
import me.jinmin.jpashop.item.ItemService;
import me.jinmin.jpashop.member.Member;
import me.jinmin.jpashop.member.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    //주문
    @GetMapping("/order")
    public String createOrderForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam Long memberId,
                        @RequestParam Long itemId,
                        @RequestParam int count) {
        orderService.order(memberId, itemId, count);

        return "redirect:/orders";
    }

    //주문 목록
    @GetMapping("/orders")
    public String getOrderList(@ModelAttribute("orderSearch") OrderSearch orderSearch,
                               Model model) {
        List<Order> orders = orderService.searchOrders(orderSearch);
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    //주문 최소
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long orderId) {
        orderService.cancelOrder(orderId);

        return "redirect:/orders";
    }
}
