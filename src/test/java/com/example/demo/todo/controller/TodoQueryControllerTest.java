package com.example.demo.todo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.service.TodoQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("TodoQueryController 슬라이스 테스트")
@WebMvcTest(TodoQueryController.class)
class TodoQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoQueryService todoQueryService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 17, 10, 0, 0, 123_456_789);

    private static TodoResponse response(Long id, boolean completed) {
        return new TodoResponse(id, 1L, "할 일 " + id, null, completed, LocalDate.of(2026, 10, 20), NOW, NOW);
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/todos/{todoId}")
    class GetTodo {

        @Test
        @DisplayName("할 일을 조회하면 200과 할 일 정보를 반환한다")
        void success() throws Exception {
            // given
            given(todoQueryService.getTodo(1L, 10L)).willReturn(response(10L, false));

            // when & then
            mockMvc.perform(get("/api/v1/users/1/todos/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.dueDate").value("2026-10-20"))
                    .andExpect(jsonPath("$.createdAt").value("2026-10-17T10:00:00"));
        }

        @Test
        @DisplayName("다른 사용자의 할 일이면 404 TODO_NOT_FOUND를 반환한다")
        void todoNotFound() throws Exception {
            // given
            given(todoQueryService.getTodo(2L, 10L)).willThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/users/2/todos/10"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("할 일을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/todos")
    class GetTodos {

        @Test
        @DisplayName("completed 파라미터가 없으면 null로 전달하고 기본 페이지(0, 20, id 오름차순)로 조회한다")
        void withoutFilter() throws Exception {
            // given
            given(todoQueryService.getTodos(eq(1L), isNull(), any(Pageable.class)))
                    .willReturn(new PageResponse<>(List.of(response(10L, false), response(11L, true)),
                            0, 20, 2, 1, false));

            // when & then
            mockMvc.perform(get("/api/v1/users/1/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            then(todoQueryService).should().getTodos(eq(1L), isNull(), captor.capture());
            assertThat(captor.getValue().getPageNumber()).isZero();
            assertThat(captor.getValue().getPageSize()).isEqualTo(20);
            assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        }

        @Test
        @DisplayName("completed=false와 페이지 파라미터를 서비스에 그대로 전달한다")
        void withFilterAndPaging() throws Exception {
            // given
            given(todoQueryService.getTodos(eq(1L), eq(false), any(Pageable.class)))
                    .willReturn(new PageResponse<>(List.of(response(12L, false)), 1, 5, 6, 2, false));

            // when & then
            mockMvc.perform(get("/api/v1/users/1/todos")
                            .param("completed", "false")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].completed").value(false))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(5));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            then(todoQueryService).should().getTodos(eq(1L), eq(false), captor.capture());
            assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
            assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("completed 값이 boolean이 아니면 400 INVALID_INPUT을 반환한다")
        void invalidCompleted() throws Exception {
            mockMvc.perform(get("/api/v1/users/1/todos").param("completed", "maybe"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("사용자가 없으면 404 USER_NOT_FOUND를 반환한다")
        void userNotFound() throws Exception {
            // given
            given(todoQueryService.getTodos(eq(99L), any(), any(Pageable.class)))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/users/99/todos"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        }
    }
}
