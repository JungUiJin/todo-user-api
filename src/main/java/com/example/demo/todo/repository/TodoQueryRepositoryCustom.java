package com.example.demo.todo.repository;

import com.example.demo.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL 기반 할 일 조회 저장소.
 */
public interface TodoQueryRepositoryCustom {

    /**
     * 사용자의 할 일 목록을 완료 여부로 동적 필터링하여 페이지 단위로 조회한다. (T-06)
     *
     * @param completed null이면 전체, true/false면 해당 완료 상태만 조회한다.
     * @param pageable  페이지 번호/크기와 정렬 조건. 허용되지 않은 정렬 속성이면 INVALID_INPUT 예외가 발생한다.
     */
    Page<Todo> findTodos(Long userId, Boolean completed, Pageable pageable);
}
