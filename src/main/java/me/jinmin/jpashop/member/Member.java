package me.jinmin.jpashop.member;

import lombok.Getter;
import lombok.Setter;
import me.jinmin.jpashop.domain.Address;
import me.jinmin.jpashop.order.Order;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
