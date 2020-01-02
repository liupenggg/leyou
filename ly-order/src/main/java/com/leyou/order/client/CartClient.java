package com.leyou.order.client;

import com.leyou.cart.api.CartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "cart-service")
public interface CartClient extends CartApi {
}