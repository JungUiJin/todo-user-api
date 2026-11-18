package com.example.demo.todo.repository;

import java.util.Optional;

import com.example.demo.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

/**
 * 할 일 조회 전용 저장소.
 */
public interface TodoQueryRepository extends Repository<Todo, Long> {

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    Page<Todo> findAllByUserId(Long userId, Pageable pageable);

    Page<Todo> findAllByUserIdAndCompleted(Long userId, boolean completed, Pageable pageable);
}
