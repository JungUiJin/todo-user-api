package com.example.demo.todo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.example.demo.global.config.JpaAuditingConfig;
import com.example.demo.todo.entity.Todo;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserCommandRepository;
import com.example.demo.user.service.UserCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 실제 H2 DB에서 쿼리 메서드와 사용자 삭제 시 할 일 일괄 삭제 동작을 검증한다.
 */
@DisplayName("Todo 저장소 통합 테스트 (H2)")
@DataJpaTest
@Import({JpaAuditingConfig.class, UserCommandService.class})
class TodoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TodoQueryRepository todoQueryRepository;

    @Autowired
    private TodoCommandRepository todoCommandRepository;

    @Autowired
    private UserCommandRepository userCommandRepository;

    @Autowired
    private UserCommandService userCommandService;

    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        owner = em.persist(User.create("owner@example.com", "소유자"));
        other = em.persist(User.create("other@example.com", "다른 사용자"));

        // owner: 할 일 5개 중 2개 완료 / other: 할 일 1개
        for (int i = 1; i <= 5; i++) {
            Todo todo = Todo.create(owner, "할 일 " + i, null, null);
            if (i % 2 == 0) {
                todo.changeCompletion(true);
            }
            em.persist(todo);
        }
        em.persist(Todo.create(other, "남의 할 일", null, null));
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("목록 조회 (완료 여부 필터링 + 페이징)")
    class FindAll {

        private final PageRequest firstPage = PageRequest.of(0, 2, Sort.by("id"));

        @Test
        @DisplayName("필터 없이 조회하면 해당 사용자의 할 일만 페이지 단위로 반환한다")
        void withoutFilter() {
            Page<Todo> page = todoQueryRepository.findAllByUserId(owner.getId(), firstPage);

            assertThat(page.getContent()).extracting(Todo::getTitle).containsExactly("할 일 1", "할 일 2");
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(3);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("completed=true면 완료된 할 일만 반환하고 전체 개수도 필터 기준으로 계산한다")
        void completedOnly() {
            Page<Todo> page = todoQueryRepository.findAllByUserIdAndCompleted(owner.getId(), true, firstPage);

            assertThat(page.getContent()).extracting(Todo::getTitle).containsExactly("할 일 2", "할 일 4");
            assertThat(page.getContent()).allMatch(Todo::isCompleted);
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.hasNext()).isFalse();
        }

        @Test
        @DisplayName("completed=false면 미완료 할 일만 반환하며 다음 페이지도 올바르게 조회된다")
        void incompleteOnlySecondPage() {
            Page<Todo> page = todoQueryRepository.findAllByUserIdAndCompleted(
                    owner.getId(), false, PageRequest.of(1, 2, Sort.by("id")));

            assertThat(page.getContent()).extracting(Todo::getTitle).containsExactly("할 일 5");
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("단건 조회 (소유자 검증)")
    class FindByIdAndUserId {

        @Test
        @DisplayName("다른 사용자의 할 일 ID로는 조회되지 않는다")
        void otherUsersTodo() {
            Long othersTodoId = todoQueryRepository.findAllByUserId(other.getId(), PageRequest.of(0, 1))
                    .getContent().get(0).getId();

            assertThat(todoQueryRepository.findByIdAndUserId(othersTodoId, other.getId())).isPresent();
            assertThat(todoQueryRepository.findByIdAndUserId(othersTodoId, owner.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자 삭제 시 할 일 일괄 삭제 (U-03)")
    class DeleteUserWithTodos {

        @Test
        @DisplayName("사용자를 삭제하면 그 사용자의 할 일만 함께 삭제되고 FK 제약 위반이 발생하지 않는다")
        void deleteUserCascadesTodos() {
            // when
            userCommandService.deleteUser(owner.getId());
            em.flush();
            em.clear();

            // then
            assertThat(userCommandRepository.findById(owner.getId())).isEmpty();
            List<Todo> remaining = todoCommandRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getUser().getId()).isEqualTo(other.getId());
        }

        @Test
        @DisplayName("일괄 삭제 쿼리는 삭제된 행 수를 반환한다")
        void bulkDeleteReturnsCount() {
            int deleted = todoCommandRepository.deleteAllByUserId(owner.getId());

            assertThat(deleted).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("할 일을 저장하면 생성/수정 일시가 자동으로 채워진다")
    void auditingFields() {
        Todo todo = todoQueryRepository.findAllByUserId(owner.getId(), PageRequest.of(0, 1)).getContent().get(0);

        assertThat(todo.getCreatedAt()).isNotNull();
        assertThat(todo.getUpdatedAt()).isNotNull();
    }
}
