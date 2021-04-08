package com.example.jpa.example1;

import org.springframework.beans.BeanUtils;

public class UtilTest {
    public static void main(String[] args) {
        User user1 = new User();
        user1.setName("jack1");
        user1.setEmail("email1");
        user1.setAge(1);
        user1.setVersion(1);
        User user2 = new User();
        user2.setName("jack2");
        user2.setVersion(2);
    }
}
