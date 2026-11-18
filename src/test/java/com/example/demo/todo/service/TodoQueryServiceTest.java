package com.example.demo.todo.service;

import static com.example.demo.todo.TodoFixture.todo;
import static com.example.demo.todo.TodoFixture.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.entity.Todo;
import com.example.demo.todo.repository.TodoQueryRepository;
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

@DisplayName("TodoQueryService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class TodoQueryServiceTest {

    @Mock
    private TodoQueryRepository todoQueryRepository;

    @Mock
    private UserQueryRepository userQueryRepository;

    @InjectMocks
    private TodoQueryService todoQueryService;

    @Nested
    @DisplayName("할 일 단건 조회")
    class GetTodo {

        @Test
        @DisplayName("사용자의 할 일을 TodoResponse로 변환해 반환한다")
        void success() {
            // given
            given(userQueryRepository.existsById(1L)).willReturn(true);
            given(todoQueryRepository.findByIdAndUserId(10L, 1L))
                    .willReturn(Optional.of(todo(10L, user(1L), "PRD 작성")));

            // when
            TodoResponse response = todoQueryService.getTodo(1L, 10L);

            // then
            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("PRD 작성");
            assertThat(response.completed()).isFalse();
        }

        @Test
        @DisplayName("사용자가 없으면 USER_NOT_FOUND 예외가 발생한다")
        void userNotFound() {
            // given
            given(userQueryRepository.existsById(99L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> todoQueryService.getTodo(99L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("할 일이 없거나 다른 사용자의 할 일이면 TODO_NOT_FOUND 예외가 발생한다")
        void todoNotFound() {
            // given
            given(userQueryRepository.existsById(1L)).willReturn(true);
            given(todoQueryRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> todoQueryService.getTodo(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.TODO_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("할 일 목록 조회")
    class GetTodos {

        private final Pageable pageable = PageRequest.of(0, 2);

        @Test
        @DisplayName("completed가 null이면 완료 여부와 관계없이 전체 목록을 조회한다")
        void withoutFilter() {
            // given
            User user = user(1L);
            Todo done = todo(11L, user, "완료한 일");
            done.changeCompletion(true);
            List<Todo> todos = List.of(todo(10L, user, "미완료 일"), done);
            given(userQueryRepository.existsById(1L)).willReturn(true);
            given(todoQueryRepository.findAllByUserId(1L, pageable)).willReturn(new PageImpl<>(todos, pageable, 5));

            // when
            PageResponse<TodoResponse> response = todoQueryService.getTodos(1L, null, pageable);

            // then
            assertThat(response.content()).extracting(TodoResponse::completed).containsExactly(false, true);
            assertThat(response.totalElements()).isEqualTo(5);
            assertThat(response.totalPages()).isEqualTo(3);
            assertThat(response.hasNext()).isTrue();
            then(todoQueryRepository).should(never()).findAllByUserIdAndCompleted(anyLong(), anyBoolean(), any());
        }

        @Test
        @DisplayName("completed=true면 완료된 할 일만 조회한다")
        void completedOnly() {
            // given
            Todo done = todo(11L, user(1L), "완료한 일");
            done.changeCompletion(true);
            given(userQueryRepository.existsById(1L)).willReturn(true);
            given(todoQueryRepository.findAllByUserIdAndCompleted(1L, true, pageable))
                    .willReturn(new PageImpl<>(List.of(done), pageable, 1));

            // when
            PageResponse<TodoResponse> response = todoQueryService.getTodos(1L, true, pageable);

            // then
            assertThat(response.content()).extracting(TodoResponse::id).containsExactly(11L);
            assertThat(response.hasNext()).isFalse();
            then(todoQueryRepository).should(never()).findAllByUserId(anyLong(), any());
        }

        @Test
        @DisplayName("completed=false면 미완료 할 일만 조회한다")
        void incompleteOnly() {
            // given
            given(userQueryRepository.existsById(1L)).willReturn(true);
            given(todoQueryRepository.findAllByUserIdAndCompleted(1L, false, pageable))
                    .willReturn(new PageImpl<>(List.of(todo(10L, user(1L), "미완료 일")), pageable, 1));

            // when
            PageResponse<TodoResponse> response = todoQueryService.getTodos(1L, false, pageable);

            // then
            assertThat(response.content()).extracting(TodoResponse::completed).containsExactly(false);
        }

        @Test
        @DisplayName("사용자가 없으면 빈 목록 대신 USER_NOT_FOUND 예외가 발생한다")
        void userNotFound() {
            // given
            given(userQueryRepository.existsById(99L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> todoQueryService.getTodos(99L, null, pageable))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }
}
