package com.leyou.service;

import com.leyou.client.UserClient;
import com.leyou.properties.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @create: 2019-07-19 18:50
 **/
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 根据用户名及密码判断用户是否登录，并且根据配置生成jwt的token返回给浏览器
     *
     * @param username
     * @param password
     * @return
     */
    public String accredit(String username, String password) {
        //1.根据用户名及密码查询用户
        User user = userClient.queryUser(username, password);

        //2.判断user
        if (user == null) {
            return null;
        }

        //3.jwtUtils生成jwt类型的token
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUserName(user.getUsername());
            String token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}