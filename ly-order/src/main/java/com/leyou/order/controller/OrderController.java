package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * http://api.leyou.com/api/order-service/order
     * @param orderDTO
     * @return
     */
    @PostMapping
    private ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        // 创建订单，返回订单id
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 创建支付链接
     * @param orderId
     * @return
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> creatPayUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.creatPayUrl(orderId));
    }

    /**
     * 前端获取订单状态
     * @param orderId
     * @return
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id")Long orderId){
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }

    @GetMapping("list")
    public ResponseEntity<List<Order>> queryOrdersByUserId(@RequestParam("page") Integer page, @RequestParam("rows") Integer rows ) {
        return ResponseEntity.ok(orderService.queryOrdersByUserId(page, rows));
    }
}