package com.example.jpa.sharding.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInfoRepository extends JpaRepository<UserInfo,Long> {
    List<UserInfo> findByName(String name);
}
