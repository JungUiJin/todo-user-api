package com.example.demo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import java.util.Optional;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.repository.TodoCommandRepository;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("UserCommandService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserCommandRepository userCommandRepository;

    @Mock
    private TodoCommandRepository todoCommandRepository;

    @InjectMocks
    private UserCommandService userCommandService;

    private static User createUser(Long id, String email, String name) {
        User user = User.create(email, name);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    @DisplayName("사용자 등록")
    class CreateUser {

        @Test
        @DisplayName("이메일이 중복되지 않으면 사용자를 저장하고 ID를 반환한다")
        void success() {
            // given
            UserCreateRequest request = new UserCreateRequest("user@example.com", "홍길동");
            given(userCommandRepository.existsByEmail("user@example.com")).willReturn(false);
            given(userCommandRepository.save(any(User.class)))
                    .willReturn(createUser(1L, "user@example.com", "홍길동"));

            // when
            Long userId = userCommandService.createUser(request);

            // then
            assertThat(userId).isEqualTo(1L);
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            then(userCommandRepository).should().save(captor.capture());
            assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
            assertThat(captor.getValue().getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("이미 등록된 이메일이면 DUPLICATE_EMAIL 예외가 발생하고 저장하지 않는다")
        void duplicateEmail() {
            // given
            UserCreateRequest request = new UserCreateRequest("user@example.com", "홍길동");
            given(userCommandRepository.existsByEmail("user@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userCommandService.createUser(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
            then(userCommandRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정")
    class UpdateUser {

        @Test
        @DisplayName("존재하는 사용자의 이름을 변경한다")
        void success() {
            // given
            User user = createUser(1L, "user@example.com", "홍길동");
            given(userCommandRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            Long userId = userCommandService.updateUser(1L, new UserUpdateRequest("김철수"));

            // then
            assertThat(userId).isEqualTo(1L);
            assertThat(user.getName()).isEqualTo("김철수");
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void userNotFound() {
            // given
            given(userCommandRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.updateUser(99L, new UserUpdateRequest("김철수")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("사용자의 할 일을 먼저 일괄 삭제한 뒤 사용자를 삭제한다")
        void success() {
            // given
            User user = createUser(1L, "user@example.com", "홍길동");
            given(userCommandRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            userCommandService.deleteUser(1L);

            // then
            InOrder inOrder = inOrder(todoCommandRepository, userCommandRepository);
            inOrder.verify(todoCommandRepository).deleteAllByUserId(1L);
            inOrder.verify(userCommandRepository).delete(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생하고 아무것도 삭제하지 않는다")
        void userNotFound() {
            // given
            given(userCommandRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.deleteUser(99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
            then(todoCommandRepository).should(never()).deleteAllByUserId(any());
            then(userCommandRepository).should(never()).delete(any());
        }
    }
}
