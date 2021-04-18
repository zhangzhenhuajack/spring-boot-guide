package com.example.jpa.sharding.demo.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress,Long> {
}
