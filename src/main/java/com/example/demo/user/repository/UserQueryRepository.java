package com.example.demo.user.repository;

import java.util.Optional;

import com.example.demo.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

/**
 * 사용자 조회 전용 저장소.
 * 저장/삭제 메서드가 노출되지 않도록 JpaRepository 대신 Repository를 상속하고 조회 메서드만 선언한다.
 */
public interface UserQueryRepository extends Repository<User, Long> {

    Optional<User> findById(Long id);

    Page<User> findAll(Pageable pageable);

    boolean existsById(Long id);
}
