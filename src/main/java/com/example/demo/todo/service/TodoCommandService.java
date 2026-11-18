package com.example.demo.todo.service;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.dto.TodoCompletionRequest;
import com.example.demo.todo.dto.TodoCreateRequest;
import com.example.demo.todo.dto.TodoUpdateRequest;
import com.example.demo.todo.entity.Todo;
import com.example.demo.todo.repository.TodoCommandRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 할 일 생성/수정/삭제 서비스.
 * 커맨드는 식별자만 반환하고, 응답 데이터는 TodoQueryService로 조회한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TodoCommandService {

    private final TodoCommandRepository todoCommandRepository;
    private final UserCommandRepository userCommandRepository;

    public Long createTodo(Long userId, TodoCreateRequest request) {
        User user = userCommandRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Todo todo = todoCommandRepository.save(
                Todo.create(user, request.title(), request.description(), request.dueDate()));
        return todo.getId();
    }

    public Long updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
        Todo todo = findTodo(userId, todoId);
        todo.update(request.title(), request.description(), request.dueDate());
        return todo.getId();
    }

    public Long changeCompletion(Long userId, Long todoId, TodoCompletionRequest request) {
        Todo todo = findTodo(userId, todoId);
        todo.changeCompletion(request.completed());
        return todo.getId();
    }

    public void deleteTodo(Long userId, Long todoId) {
        todoCommandRepository.delete(findTodo(userId, todoId));
    }

    // 사용자 존재 여부를 먼저 확인해 USER_NOT_FOUND와 TODO_NOT_FOUND를 구분한다
    private Todo findTodo(Long userId, Long todoId) {
        if (!userCommandRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return todoCommandRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    }
}
