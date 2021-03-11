package com.example.jpa.sharding.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    @Id
    private Long id;
    private String name;
    private String email;
    private String lastName;
}
