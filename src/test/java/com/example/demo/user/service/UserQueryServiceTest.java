package com.example.demo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("UserQueryService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    private static User createUser(Long id, String email, String name) {
        User user = User.create(email, name);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    @DisplayName("사용자 단건 조회")
    class GetUser {

        @Test
        @DisplayName("존재하는 사용자를 UserResponse로 변환해 반환한다")
        void success() {
            // given
            given(userQueryRepository.findById(1L))
                    .willReturn(Optional.of(createUser(1L, "user@example.com", "홍길동")));

            // when
            UserResponse response = userQueryService.getUser(1L);

            // then
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("user@example.com");
            assertThat(response.name()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void userNotFound() {
            // given
            given(userQueryRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userQueryService.getUser(99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회")
    class GetUsers {

        @Test
        @DisplayName("페이지 정보와 함께 사용자 목록을 반환한다")
        void success() {
            // given
            Pageable pageable = PageRequest.of(0, 2);
            List<User> users = List.of(
                    createUser(1L, "a@example.com", "가"),
                    createUser(2L, "b@example.com", "나"));
            given(userQueryRepository.findAll(pageable)).willReturn(new PageImpl<>(users, pageable, 3));

            // when
            PageResponse<UserResponse> response = userQueryService.getUsers(pageable);

            // then
            assertThat(response.content()).extracting(UserResponse::email)
                    .containsExactly("a@example.com", "b@example.com");
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.totalElements()).isEqualTo(3);
            assertThat(response.totalPages()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
        }
    }
}
