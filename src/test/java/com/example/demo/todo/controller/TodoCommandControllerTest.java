package com.example.demo.todo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.dto.TodoCompletionRequest;
import com.example.demo.todo.dto.TodoCreateRequest;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.dto.TodoUpdateRequest;
import com.example.demo.todo.service.TodoCommandService;
import com.example.demo.todo.service.TodoQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TodoCommandController 슬라이스 테스트")
@WebMvcTest(TodoCommandController.class)
class TodoCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoCommandService todoCommandService;

    @MockitoBean
    private TodoQueryService todoQueryService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 17, 10, 0, 0, 123_456_789);

    private static TodoResponse response(boolean completed) {
        return new TodoResponse(10L, 1L, "PRD 작성", "4단계", completed, LocalDate.of(2026, 10, 20), NOW, NOW);
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/todos")
    class CreateTodo {

        @Test
        @DisplayName("할 일을 등록하면 201과 Location 헤더, 할 일 정보를 반환한다")
        void success() throws Exception {
            // given
            given(todoCommandService.createTodo(eq(1L), any(TodoCreateRequest.class))).willReturn(10L);
            given(todoQueryService.getTodo(1L, 10L)).willReturn(response(false));

            // when & then
            mockMvc.perform(post("/api/v1/users/1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "PRD 작성", "description": "4단계", "dueDate": "2026-10-20"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/users/1/todos/10"))
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.title").value("PRD 작성"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.dueDate").value("2026-10-20"))
                    .andExpect(jsonPath("$.createdAt").value("2026-10-17T10:00:00"))
                    .andExpect(jsonPath("$.updatedAt").value("2026-10-17T10:00:00"));

            ArgumentCaptor<TodoCreateRequest> captor = ArgumentCaptor.forClass(TodoCreateRequest.class);
            then(todoCommandService).should().createTodo(eq(1L), captor.capture());
            assertThat(captor.getValue().dueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
        }

        @Test
        @DisplayName("제목이 비어 있으면 400 INVALID_INPUT을 반환한다")
        void blankTitle() throws Exception {
            mockMvc.perform(post("/api/v1/users/1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": " "}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            then(todoCommandService).should(never()).createTodo(any(), any());
        }

        @Test
        @DisplayName("제목이 100자를 넘으면 400 INVALID_INPUT을 반환한다")
        void titleTooLong() throws Exception {
            mockMvc.perform(post("/api/v1/users/1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"" + "가".repeat(101) + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("마감일 형식이 yyyy-MM-dd가 아니면 400 INVALID_INPUT을 반환한다")
        void invalidDueDate() throws Exception {
            mockMvc.perform(post("/api/v1/users/1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "제목", "dueDate": "2026/10/20"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("사용자가 없으면 404 USER_NOT_FOUND를 반환한다")
        void userNotFound() throws Exception {
            // given
            given(todoCommandService.createTodo(eq(99L), any(TodoCreateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/users/99/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "제목"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}/todos/{todoId}")
    class UpdateTodo {

        @Test
        @DisplayName("일부 필드만 보내도 200과 수정된 할 일 정보를 반환한다")
        void partialUpdate() throws Exception {
            // given
            given(todoCommandService.updateTodo(eq(1L), eq(10L), any(TodoUpdateRequest.class))).willReturn(10L);
            given(todoQueryService.getTodo(1L, 10L)).willReturn(response(false));

            // when & then
            mockMvc.perform(patch("/api/v1/users/1/todos/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "PRD 작성"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.title").value("PRD 작성"));

            ArgumentCaptor<TodoUpdateRequest> captor = ArgumentCaptor.forClass(TodoUpdateRequest.class);
            then(todoCommandService).should().updateTodo(eq(1L), eq(10L), captor.capture());
            assertThat(captor.getValue().description()).isNull();
            assertThat(captor.getValue().dueDate()).isNull();
        }

        @Test
        @DisplayName("제목을 공백으로 보내면 400 INVALID_INPUT을 반환한다")
        void blankTitle() throws Exception {
            mockMvc.perform(patch("/api/v1/users/1/todos/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "   "}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("할 일이 없으면 404 TODO_NOT_FOUND를 반환한다")
        void todoNotFound() throws Exception {
            // given
            given(todoCommandService.updateTodo(eq(1L), eq(99L), any(TodoUpdateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND));

            // when & then
            mockMvc.perform(patch("/api/v1/users/1/todos/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "새 제목"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}/todos/{todoId}/completion")
    class ChangeCompletion {

        @Test
        @DisplayName("완료 상태를 변경하면 200과 변경된 할 일 정보를 반환한다")
        void success() throws Exception {
            // given
            given(todoCommandService.changeCompletion(eq(1L), eq(10L), any(TodoCompletionRequest.class)))
                    .willReturn(10L);
            given(todoQueryService.getTodo(1L, 10L)).willReturn(response(true));

            // when & then
            mockMvc.perform(patch("/api/v1/users/1/todos/10/completion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"completed": true}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true));
        }

        @Test
        @DisplayName("completed 값이 없으면 400 INVALID_INPUT을 반환한다")
        void missingCompleted() throws Exception {
            mockMvc.perform(patch("/api/v1/users/1/todos/10/completion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            then(todoCommandService).should(never()).changeCompletion(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId}/todos/{todoId}")
    class DeleteTodo {

        @Test
        @DisplayName("할 일을 삭제하면 204를 반환한다")
        void success() throws Exception {
            mockMvc.perform(delete("/api/v1/users/1/todos/10"))
                    .andExpect(status().isNoContent());
            then(todoCommandService).should().deleteTodo(1L, 10L);
        }

        @Test
        @DisplayName("다른 사용자의 할 일이면 404 TODO_NOT_FOUND를 반환한다")
        void otherUsersTodo() throws Exception {
            // given
            willThrow(new BusinessException(ErrorCode.TODO_NOT_FOUND))
                    .given(todoCommandService).deleteTodo(2L, 10L);

            // when & then
            mockMvc.perform(delete("/api/v1/users/2/todos/10"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"));
        }
    }
}
