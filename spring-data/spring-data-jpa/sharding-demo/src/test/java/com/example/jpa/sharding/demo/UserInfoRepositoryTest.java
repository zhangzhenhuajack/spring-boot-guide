package com.example.jpa.sharding.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.transaction.Transactional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
@Import(TestConfiguration.class)
@ComponentScan(value = "com.example.jpa")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserInfoRepositoryTest {

    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;

    @BeforeAll
    @Rollback(false)
    @Transactional
    public void init() {
//        UserInfo u1 = UserInfo.builder().id(1L).lastName("jack").build();
//        try {
//            userInfoRepository.save(u1);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("************************");
//        }
    }

    @Test
//    @Transactional
    @Rollback(false)
    public void testLife() {
//        userAddressRepository.findById(1l);
//        userAddressRepository.findById(2l);
//        UserInfo u1 = UserInfo.builder().id(1L).lastName("jack").build();
//        try {
//            userInfoRepository.save(u1);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("************************");
//        }
//
//        UserInfo u2 = UserInfo.builder().id(2L).lastName("jack").build();
//        try {
//            userInfoRepository.save(u2);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("************************");
//        }
//        //由于有cache机制，相同的对象查询只会触发一次查询SQL
//        userInfoRepository.findById(1L);
        //to do some thing
//        userInfoRepository.findById(2L);

//        System.out.println(u1);
//        System.out.println(u2);
        userInfoRepository.findByName("abc");

    }

}
