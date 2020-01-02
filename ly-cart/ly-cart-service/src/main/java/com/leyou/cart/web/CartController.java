package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 新增购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList() {
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /**
     * 修改购物车，加1或减1
     * @param SkuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoodNum(@RequestParam("id")String SkuId,@RequestParam("num")Integer num) {
        cartService.updateGoodNum(SkuId, num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据skuId删除购物车商品
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCartById(@PathVariable("skuId")String skuId ) {
        cartService.deleteCartById(skuId);
        return ResponseEntity.ok().build();
    }

    /**
     * 合并购物车
     * @param carts
     * @return
     */
    @PostMapping("batch")
    public ResponseEntity<Void> mergeCart(@RequestBody List<Cart> carts) {
        cartService.mergeCart(carts);
        return ResponseEntity.ok().build();
    }
}