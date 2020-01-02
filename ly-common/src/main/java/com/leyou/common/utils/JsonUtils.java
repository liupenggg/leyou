package com.leyou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-04-24 17:20
 **/
public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);


    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }


    public static <T> T toBean(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }


    public static <E> List<E> toList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }


    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }


    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }


    // 测试代码
    //@Data
    //@AllArgsConstructor
    //@NoArgsConstructor
    //static class User{
    //    String name;
    //    Integer age;
    //}
    //public static void main(String[] args) {

    //    User user = new User("Jack",21);
        // toString
        //String json = toString(user);
        //System.out.println("json = " + json);// json = {"name":"Jack","age":21}

        // 反序列化
        //User user1 = toBean(json, User.class);
        //System.out.println("user1 = " + user1);// user1 = JsonUtils.User(name=Jack, age=21)

        // toList
        //json = "[20,-10,5,15]";
        //List<Integer> list = toList(json, Integer.class);// 元素的类型
        //System.out.println("list = " + list);// list = [20, -10, 5, 15]

        // toMap
        //language=JSON
        //String json = "{\"name\": \"Jack\", \"age\": \"21\"}";
        //Map<String, String> map = toMap(json, String.class, String.class);
        //System.out.println("map = " + map);// map = {name=Jack, age=21}

        // 演示List里面装Map
        //String json = "[{\"name\": \"Jack\", \"age\": \"21\"},{\"name\": \"Rose\", \"age\": \"18\"}]";//String json = "[20, -10, 5, 15]"

        // List里面装Map,Map里面装String,key是String，值也是String
        //List<Map<String,String>> maps = nativeRead(json, new TypeReference<List<Map<String,String>>>() {
        //});

        //for (Map<String,String>map:maps){
    //        System.out.println("map = " + map);
        //}
    //}
}
