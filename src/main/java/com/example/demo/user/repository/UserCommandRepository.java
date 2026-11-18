package com.example.demo.user.repository;

import com.example.demo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 생성/수정/삭제용 저장소.
 */
public interface UserCommandRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
