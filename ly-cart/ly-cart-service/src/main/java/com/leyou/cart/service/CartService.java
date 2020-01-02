package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid";

    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo userInfo = UserInterceptor.getLoginUser();
        //key
        String key = KEY_PREFIX + userInfo.getId();
        //hashKey
        String hashKey = cart.getSkuId().toString();
        //记录num
        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);

        //判断当前购物车商品，是否存在
        if (operation.hasKey(hashKey)) {
            // 存在，修改数量
            cart = JsonUtils.toBean(operation.get(hashKey).toString(), Cart.class);// 覆盖之前的cart
            cart.setNum(cart.getNum() + num);
        }
        //写回redis
        operation.put(hashKey ,JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
        //获取登录用户的购物车
        BoundHashOperations<String, Object, Object> operation = getCarts();
        List<Cart> cartList = operation.values().stream()
                .map(o -> JsonUtils.toBean(o.toString(), Cart.class)).collect(Collectors.toList());
        return cartList;
    }

    public void updateGoodNum(String skuId, Integer num) {
        //获取登录用户的购物车
        BoundHashOperations<String, Object, Object> operation = getCarts();
        if (!operation.hasKey(skuId)) {
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        Cart cart = JsonUtils.toBean(operation.get(skuId).toString(), Cart.class);
        cart.setNum(num);
        //写回redis
        operation.put(skuId ,JsonUtils.toString(cart));
    }

    private BoundHashOperations<String, Object, Object> getCarts() {
        //获取登录用户
        UserInfo userInfo = UserInterceptor.getLoginUser();
        //key
        String key = KEY_PREFIX + userInfo.getId();
        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        return redisTemplate.boundHashOps(key);
    }

    public void deleteCartById(String SkuId) {
        //获取登录用户的购物车
        BoundHashOperations<String, Object, Object> operation = getCarts();
        //删除对应商品
        operation.delete(SkuId);
    }

    public void mergeCart(List<Cart> carts) {
        for (Cart cart : carts) {
            addCart(cart);
        }
    }
}