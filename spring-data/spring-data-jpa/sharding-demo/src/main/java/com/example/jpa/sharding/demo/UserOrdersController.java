package com.example.jpa.sharding.demo;

import com.example.jpa.sharding.demo.core.UserOrders;
import com.example.jpa.sharding.demo.core.UserOrdersRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserOrdersController {
    private UserOrdersRepository userOrdersRepository;

    public UserOrdersController(UserOrdersRepository userOrdersRepository) {
        this.userOrdersRepository = userOrdersRepository;
    }
    /**
     * 新增用户
     * @param userOrders
     * @return
     */
    @PostMapping("user/order")
    public UserOrders create(@RequestBody UserOrders userOrders) {
        return userOrdersRepository.save(userOrders);
    }

    /**
     * 根据用户查询订单信息
     * @param uuid
     * @return
     */
    @GetMapping("user/{userUuid}/orders")
    public Page<UserOrders> query(@PathVariable(name = "userUuid") String uuid, Pageable pageable) {
        return userOrdersRepository.findAll(Example.of(UserOrders.builder().uuid(uuid).build()), pageable);
    }

}
