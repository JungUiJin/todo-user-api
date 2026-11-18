package com.example.demo.todo.repository;

import static com.example.demo.todo.entity.QTodo.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import com.example.demo.todo.entity.Todo;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

/**
 * {@link TodoQueryRepositoryCustom}의 QueryDSL 구현체.
 * Spring Data가 "저장소 인터페이스명 + Impl" 규칙으로 자동 감지하여 TodoQueryRepository에 조합한다.
 */
@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepositoryCustom {

    /**
     * 정렬 허용 속성 화이트리스트.
     * 클라이언트가 보낸 정렬 문자열을 쿼리에 직접 반영하지 않고, 미리 정의한 Q-Type 경로로만 변환한다.
     */
    private static final Map<String, ComparableExpressionBase<?>> SORTABLE_PROPERTIES = Map.of(
            "id", todo.id,
            "title", todo.title,
            "completed", todo.completed,
            "dueDate", todo.dueDate,
            "createdAt", todo.createdAt,
            "updatedAt", todo.updatedAt
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Todo> findTodos(Long userId, Boolean completed, Pageable pageable) {
        List<Todo> content = queryFactory
                .selectFrom(todo)
                .where(userIdEq(userId), completedEq(completed))
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(todo.count())
                .from(todo)
                .where(userIdEq(userId), completedEq(completed));

        // 첫/마지막 페이지에서 조회 결과만으로 전체 개수를 알 수 있으면 count 쿼리를 생략한다.
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression userIdEq(Long userId) {
        return todo.user.id.eq(userId);
    }

    /**
     * 완료 여부 동적 조건. null을 반환하면 QueryDSL where 절에서 무시되어 전체 조회가 된다.
     */
    private BooleanExpression completedEq(Boolean completed) {
        return completed == null ? null : todo.completed.eq(completed);
    }

    /**
     * Pageable의 정렬 조건을 OrderSpecifier로 변환한다.
     * 정렬 조건이 없으면 PRD 기본 정렬(id 오름차순)을 적용하고, 허용되지 않은 속성이면 예외를 던진다.
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{todo.id.asc()};
        }
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            ComparableExpressionBase<?> path = SORTABLE_PROPERTIES.get(order.getProperty());
            if (path == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "정렬할 수 없는 속성입니다: " + order.getProperty());
            }
            orders.add(order.isAscending() ? path.asc() : path.desc());
        }
        return orders.toArray(OrderSpecifier[]::new);
    }
}
