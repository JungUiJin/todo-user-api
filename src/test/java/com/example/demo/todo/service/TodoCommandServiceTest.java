package com.example.demo.todo.service;

import static com.example.demo.todo.TodoFixture.todo;
import static com.example.demo.todo.TodoFixture.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.util.Optional;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.dto.TodoCompletionRequest;
import com.example.demo.todo.dto.TodoCreateRequest;
import com.example.demo.todo.dto.TodoUpdateRequest;
import com.example.demo.todo.entity.Todo;
import com.example.demo.todo.repository.TodoCommandRepository;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("TodoCommandService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class TodoCommandServiceTest {

    @Mock
    private TodoCommandRepository todoCommandRepository;

    @Mock
    private UserCommandRepository userCommandRepository;

    @InjectMocks
    private TodoCommandService todoCommandService;

    @Nested
    @DisplayName("할 일 등록")
    class CreateTodo {

        @Test
        @DisplayName("사용자가 존재하면 미완료 상태의 할 일을 저장하고 ID를 반환한다")
        void success() {
            // given
            User user = user(1L);
            TodoCreateRequest request = new TodoCreateRequest("PRD 작성", "4단계", LocalDate.of(2026, 10, 20));
            given(userCommandRepository.findById(1L)).willReturn(Optional.of(user));
            given(todoCommandRepository.save(any(Todo.class))).willReturn(todo(10L, user, "PRD 작성"));

            // when
            Long todoId = todoCommandService.createTodo(1L, request);

            // then
            assertThat(todoId).isEqualTo(10L);
            ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
            then(todoCommandRepository).should().save(captor.capture());
            Todo saved = captor.getValue();
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getTitle()).isEqualTo("PRD 작성");
            assertThat(saved.getDescription()).isEqualTo("4단계");
            assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
            assertThat(saved.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("사용자가 없으면 USER_NOT_FOUND 예외가 발생하고 저장하지 않는다")
        void userNotFound() {
            // given
            given(userCommandRepository.findById(99L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoCommandService.createTodo(99L, new TodoCreateRequest("제목", null, null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
            then(todoCommandRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("할 일 수정")
    class UpdateTodo {

        @Test
        @DisplayName("전달한 필드만 수정하고 null인 필드는 기존 값을 유지한다")
        void partialUpdate() {
            // given
            Todo todo = todo(10L, user(1L), "기존 제목");
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(todo));

            // when
            Long todoId = todoCommandService.updateTodo(1L, 10L, new TodoUpdateRequest("새 제목", null, null));

            // then
            assertThat(todoId).isEqualTo(10L);
            assertThat(todo.getTitle()).isEqualTo("새 제목");
            assertThat(todo.getDescription()).isEqualTo("설명");
            assertThat(todo.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
        }

        @Test
        @DisplayName("사용자가 없으면 USER_NOT_FOUND 예외가 발생한다")
        void userNotFound() {
            // given
            given(userCommandRepository.existsById(99L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> todoCommandService.updateTodo(99L, 10L, new TodoUpdateRequest("새 제목", null, null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
            then(todoCommandRepository).should(never()).findByIdAndUserId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("할 일이 없거나 다른 사용자의 할 일이면 TODO_NOT_FOUND 예외가 발생한다")
        void todoNotFound() {
            // given
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoCommandService.updateTodo(1L, 10L, new TodoUpdateRequest("새 제목", null, null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.TODO_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("할 일 완료 상태 변경")
    class ChangeCompletion {

        @Test
        @DisplayName("완료 상태를 true로 변경한다")
        void complete() {
            // given
            Todo todo = todo(10L, user(1L), "제목");
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(todo));

            // when
            todoCommandService.changeCompletion(1L, 10L, new TodoCompletionRequest(true));

            // then
            assertThat(todo.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("완료된 할 일을 다시 미완료로 변경한다")
        void uncomplete() {
            // given
            Todo todo = todo(10L, user(1L), "제목");
            todo.changeCompletion(true);
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(todo));

            // when
            todoCommandService.changeCompletion(1L, 10L, new TodoCompletionRequest(false));

            // then
            assertThat(todo.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("할 일이 없으면 TODO_NOT_FOUND 예외가 발생한다")
        void todoNotFound() {
            // given
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoCommandService.changeCompletion(1L, 10L, new TodoCompletionRequest(true)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.TODO_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("할 일 삭제")
    class DeleteTodo {

        @Test
        @DisplayName("사용자의 할 일을 삭제한다")
        void success() {
            // given
            Todo todo = todo(10L, user(1L), "제목");
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(todo));

            // when
            todoCommandService.deleteTodo(1L, 10L);

            // then
            then(todoCommandRepository).should().delete(todo);
        }

        @Test
        @DisplayName("할 일이 없으면 TODO_NOT_FOUND 예외가 발생하고 삭제하지 않는다")
        void todoNotFound() {
            // given
            given(userCommandRepository.existsById(1L)).willReturn(true);
            given(todoCommandRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoCommandService.deleteTodo(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.TODO_NOT_FOUND);
            then(todoCommandRepository).should(never()).delete(any());
        }
    }
}
