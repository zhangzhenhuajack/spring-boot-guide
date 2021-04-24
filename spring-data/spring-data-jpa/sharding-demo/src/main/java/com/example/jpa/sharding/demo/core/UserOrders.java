package com.example.jpa.sharding.demo.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_orders",indexes = {@Index(unique = true,columnList = "businessCode"),@Index(columnList = "uuid")})
@org.hibernate.annotations.Table(appliesTo = "user_orders", comment = "用户订单表")//为了给表添加注释
@EntityListeners(AuditingEntityListener.class)
public class UserOrders {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    //用户的UUID
    @Column(columnDefinition = "varchar(255) DEFAULT NULL COMMENT '用户的 UUID'")
    private String uuid;
    @Column(columnDefinition = "int(11) DEFAULT NULL COMMENT '数量'")
    private Long amount;
    @CreatedDate
    @Column(name = "created_at",columnDefinition = "datetime DEFAULT NULL COMMENT '创建时间'")
    private Instant createdAt;
    @Column(columnDefinition = "varchar(255) DEFAULT NULL COMMENT '事务字符串，必须唯一，可以为空'")
    private String businessCode;
}
