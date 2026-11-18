package com.example.demo.user.service;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.repository.TodoCommandRepository;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 생성/수정/삭제 서비스.
 * 커맨드는 식별자만 반환하고, 응답 데이터는 UserQueryService로 조회한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserCommandRepository userCommandRepository;
    private final TodoCommandRepository todoCommandRepository;

    public Long createUser(UserCreateRequest request) {
        if (userCommandRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        User user = userCommandRepository.save(User.create(request.email(), request.name()));
        return user.getId();
    }

    public Long updateUser(Long userId, UserUpdateRequest request) {
        User user = findUser(userId);
        user.changeName(request.name());
        return user.getId();
    }

    /**
     * 사용자와 소유한 할 일을 함께 삭제한다.
     * 할 일을 하나씩 로딩해 삭제하는 Cascade REMOVE 대신 단일 DELETE 쿼리로 일괄 삭제하며,
     * FK 제약 조건 때문에 반드시 할 일을 먼저 삭제해야 한다.
     */
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        todoCommandRepository.deleteAllByUserId(userId);
        userCommandRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userCommandRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
