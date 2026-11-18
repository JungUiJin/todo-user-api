package com.example.demo.user.controller;

import static org.hamcrest.Matchers.containsString;
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

import java.time.LocalDateTime;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.service.UserCommandService;
import com.example.demo.user.service.UserQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("UserCommandController 슬라이스 테스트")
@WebMvcTest(UserCommandController.class)
class UserCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 17, 10, 0, 0, 123_456_789);

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUser {

        @Test
        @DisplayName("사용자를 등록하면 201과 Location 헤더, 사용자 정보를 반환한다")
        void success() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest("user@example.com", "홍길동");
            given(userCommandService.createUser(any(UserCreateRequest.class))).willReturn(1L);
            given(userQueryService.getUser(1L))
                    .willReturn(new UserResponse(1L, "user@example.com", "홍길동", NOW, NOW));

            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/users/1"))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.createdAt").value("2026-10-17T10:00:00"))
                    .andExpect(jsonPath("$.updatedAt").value("2026-10-17T10:00:00"));
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400 INVALID_INPUT을 반환한다")
        void invalidEmail() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest("not-an-email", "홍길동");

            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                    .andExpect(jsonPath("$.message").value(containsString("email")));
            then(userCommandService).should(never()).createUser(any());
        }

        @Test
        @DisplayName("이름이 비어 있으면 400 INVALID_INPUT을 반환한다")
        void blankName() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest("user@example.com", " ");

            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                    .andExpect(jsonPath("$.message").value(containsString("name")));
        }

        @Test
        @DisplayName("요청 본문이 올바른 JSON이 아니면 400 INVALID_INPUT을 반환한다")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("Content-Type이 JSON이 아니면 415 UNSUPPORTED_MEDIA_TYPE을 반환한다")
        void unsupportedMediaType() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("email=user@example.com&name=홍길동"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
        }

        @Test
        @DisplayName("이미 등록된 이메일이면 409 DUPLICATE_EMAIL을 반환한다")
        void duplicateEmail() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest("user@example.com", "홍길동");
            given(userCommandService.createUser(any(UserCreateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));

            // when & then
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                    .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."))
                    .andExpect(jsonPath("$.timestamp").value(
                            org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}")
    class UpdateUser {

        @Test
        @DisplayName("사용자 이름을 수정하면 200과 수정된 사용자 정보를 반환한다")
        void success() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest("김철수");
            given(userCommandService.updateUser(eq(1L), any(UserUpdateRequest.class))).willReturn(1L);
            given(userQueryService.getUser(1L))
                    .willReturn(new UserResponse(1L, "user@example.com", "김철수", NOW, NOW));

            // when & then
            mockMvc.perform(patch("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("김철수"));
        }

        @Test
        @DisplayName("이름이 50자를 넘으면 400 INVALID_INPUT을 반환한다")
        void nameTooLong() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest("가".repeat(51));

            // when & then
            mockMvc.perform(patch("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 404 USER_NOT_FOUND를 반환한다")
        void userNotFound() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest("김철수");
            given(userCommandService.updateUser(eq(99L), any(UserUpdateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(patch("/api/v1/users/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId}")
    class DeleteUser {

        @Test
        @DisplayName("사용자를 삭제하면 204를 반환한다")
        void success() throws Exception {
            mockMvc.perform(delete("/api/v1/users/1"))
                    .andExpect(status().isNoContent());
            then(userCommandService).should().deleteUser(1L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 404 USER_NOT_FOUND를 반환한다")
        void userNotFound() throws Exception {
            // given
            willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .given(userCommandService).deleteUser(99L);

            // when & then
            mockMvc.perform(delete("/api/v1/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        }
    }
}
