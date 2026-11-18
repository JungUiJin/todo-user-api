package com.example.demo.todo.service;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.entity.Todo;
import com.example.demo.todo.repository.TodoQueryRepository;
import com.example.demo.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 할 일 조회 서비스.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TodoQueryService {

    private final TodoQueryRepository todoQueryRepository;
    private final UserQueryRepository userQueryRepository;

    public TodoResponse getTodo(Long userId, Long todoId) {
        validateUserExists(userId);
        return todoQueryRepository.findByIdAndUserId(todoId, userId)
                .map(TodoResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    }

    /**
     * 사용자의 할 일 목록을 조회한다.
     *
     * @param completed null이면 전체, true/false면 해당 완료 상태만 조회한다.
     */
    public PageResponse<TodoResponse> getTodos(Long userId, Boolean completed, Pageable pageable) {
        validateUserExists(userId);
        Page<Todo> todos = todoQueryRepository.findTodos(userId, completed, pageable);
        return PageResponse.of(todos, TodoResponse::from);
    }

    private void validateUserExists(Long userId) {
        if (!userQueryRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
