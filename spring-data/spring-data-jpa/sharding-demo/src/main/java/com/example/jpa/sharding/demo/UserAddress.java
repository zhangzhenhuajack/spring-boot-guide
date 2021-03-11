package com.example.jpa.sharding.demo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class UserAddress {
    @Id
    private Long id;
    private String address;
    private Long userId;
}
