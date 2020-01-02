package com.leyou.cart.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface CartApi {
    /**
     * 根据skuId删除购物车商品
     *
     * @param skuId
     * @return
     */
    @DeleteMapping("{id}")
    Void deleteCartById(@PathVariable("id") String skuId);
}