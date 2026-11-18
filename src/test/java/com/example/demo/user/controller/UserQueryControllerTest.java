package com.example.demo.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.global.dto.PageResponse;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.service.UserQueryService;
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserQueryController 슬라이스 테스트")
@WebMvcTest(UserQueryController.class)
class UserQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService userQueryService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 17, 10, 0, 0, 123_456_789);

    @Nested
    @DisplayName("GET /api/v1/users/{userId}")
    class GetUser {

        @Test
        @DisplayName("사용자를 조회하면 200과 사용자 정보를 반환하며, 일시는 yyyy-MM-dd'T'HH:mm:ss 형식이다")
        void success() throws Exception {
            // given
            given(userQueryService.getUser(1L))
                    .willReturn(new UserResponse(1L, "user@example.com", "홍길동", NOW, NOW));

            // when & then
            mockMvc.perform(get("/api/v1/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.createdAt").value("2026-10-17T10:00:00"))
                    .andExpect(jsonPath("$.updatedAt").value("2026-10-17T10:00:00"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 404 USER_NOT_FOUND를 반환한다")
        void userNotFound() throws Exception {
            // given
            given(userQueryService.getUser(99L)).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("사용자 ID가 숫자가 아니면 400 INVALID_INPUT을 반환한다")
        void invalidUserId() throws Exception {
            mockMvc.perform(get("/api/v1/users/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetUsers {

        @Test
        @DisplayName("사용자 목록을 조회하면 200과 페이지 정보를 반환한다")
        void success() throws Exception {
            // given
            PageResponse<UserResponse> page = new PageResponse<>(
                    List.of(new UserResponse(1L, "user@example.com", "홍길동", NOW, NOW)),
                    0, 20, 1, 1, false);
            given(userQueryService.getUsers(any(Pageable.class))).willReturn(page);

            // when & then
            mockMvc.perform(get("/api/v1/users").param("page", "0").param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].createdAt").value("2026-10-17T10:00:00"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("페이지 파라미터가 없으면 기본값(page=0, size=20, id 오름차순)으로 조회한다")
        void defaultPageable() throws Exception {
            // given
            given(userQueryService.getUsers(any(Pageable.class)))
                    .willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

            // when
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk());

            // then
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            then(userQueryService).should().getUsers(captor.capture());
            Pageable pageable = captor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        }

        @Test
        @DisplayName("size가 최대값(100)을 넘으면 100으로 제한된다")
        void maxPageSize() throws Exception {
            // given
            given(userQueryService.getUsers(any(Pageable.class)))
                    .willReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, false));

            // when
            mockMvc.perform(get("/api/v1/users").param("size", "1000"))
                    .andExpect(status().isOk());

            // then
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            then(userQueryService).should().getUsers(captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(100);
        }
    }
}
