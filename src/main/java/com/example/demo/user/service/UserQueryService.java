package com.example.demo.user.service;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 조회 서비스.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserQueryRepository userQueryRepository;

    public UserResponse getUser(Long userId) {
        return userQueryRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public PageResponse<UserResponse> getUsers(Pageable pageable) {
        return PageResponse.of(userQueryRepository.findAll(pageable), UserResponse::from);
    }
}
