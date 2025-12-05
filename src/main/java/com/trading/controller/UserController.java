package com.trading.controller;

import com.trading.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    
    // 模拟用户数据
    private final List<User> userList = new ArrayList<>();
    
    // 构造函数中初始化用户数据
    public UserController() {
        // 添加张三和李四的用户信息
        userList.add(new User("张三", 28, "北京市海淀区"));
        userList.add(new User("李四", 32, "上海市浦东新区"));
    }
    
    /**
     * 查询所有用户列表
     * @return 用户列表
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userList;
    }
    
    /**
     * 根据用户名查询用户详情
     * @param name 用户名
     * @return 用户对象，如果不存在返回null
     */
    @GetMapping("/users/{name}")
    public User getUserByName(@PathVariable String name) {
        Optional<User> user = userList.stream()
                .filter(u -> u.getName().equals(name))
                .findFirst();
        return user.orElse(null);
    }
}