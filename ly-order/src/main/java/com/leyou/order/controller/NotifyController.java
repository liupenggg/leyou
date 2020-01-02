package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {
    @Autowired
    private OrderService orderService;

    /*
    @GetMapping("{id}")
    public String hello(@PathVariable("id") Long id){
        return "元旦快乐！";
    }*/

    /**
     * 微信支付成功回调接口
     * @param result
     * @return
     */
    @PostMapping(value = "wxpay", produces = "application/xml")
    public Map<String, String> handleNotify(@RequestBody Map<String, String> result) {
        orderService.handleNotify(result);
        log.info("[微信支付回调] 接收微信支付回调，结果:{}",result);
        Map<String, String> msg = new HashMap<>();
        msg.put("return_code", "SUCCESS");
        msg.put("return_msg", "OK");
        return msg;
    }
}