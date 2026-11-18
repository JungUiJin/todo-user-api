package com.example.demo.user.entity;

import com.example.demo.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티.
 * H2/MySQL 등에서 예약어인 user를 피하기 위해 테이블명은 users로 지정한다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    private User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public static User create(String email, String name) {
        return new User(email, name);
    }

    public void changeName(String name) {
        this.name = name;
    }
}
