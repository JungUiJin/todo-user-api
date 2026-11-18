package com.example.demo.todo.repository;

import java.util.Optional;

import com.example.demo.todo.entity.Todo;
import org.springframework.data.repository.Repository;

/**
 * 할 일 조회 전용 저장소.
 * 단건 조회는 Spring Data 메서드 쿼리로, 동적 조건이 필요한 목록 조회는 QueryDSL({@link TodoQueryRepositoryCustom})로 처리한다.
 */
public interface TodoQueryRepository extends Repository<Todo, Long>, TodoQueryRepositoryCustom {

    Optional<Todo> findByIdAndUserId(Long id, Long userId);
}
