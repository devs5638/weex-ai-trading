package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.trading.demo.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllUsers_ShouldReturnUsersList() {
        // 调用获取所有用户接口
        ResponseEntity<List> response = restTemplate.getForEntity("/users", List.class);
        
        // 验证响应状态码
        assertEquals(200, response.getStatusCodeValue());
        
        // 验证返回的数据
        List users = response.getBody();
        assertNotNull(users);
        assertEquals(2, users.size(), "用户列表应该包含2个用户");
    }

    @Test
    void getUserByName_ShouldReturnSpecificUser() {
        // 测试查询张三
        ResponseEntity<User> zhangsanResponse = restTemplate.getForEntity("/users/张三", User.class);
        assertEquals(200, zhangsanResponse.getStatusCodeValue());
        User zhangsan = zhangsanResponse.getBody();
        assertNotNull(zhangsan);
        assertEquals("张三", zhangsan.getName());
        
        // 测试查询李四
        ResponseEntity<User> lisiResponse = restTemplate.getForEntity("/users/李四", User.class);
        assertEquals(200, lisiResponse.getStatusCodeValue());
        User lisi = lisiResponse.getBody();
        assertNotNull(lisi);
        assertEquals("李四", lisi.getName());
        
        // 测试查询不存在的用户
        ResponseEntity<User> notExistResponse = restTemplate.getForEntity("/users/王五", User.class);
        assertEquals(200, notExistResponse.getStatusCodeValue());
        assertNull(notExistResponse.getBody(), "不存在的用户应该返回null");
    }
}