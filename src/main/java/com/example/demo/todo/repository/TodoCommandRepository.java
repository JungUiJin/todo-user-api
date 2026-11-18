package com.example.demo.todo.repository;

import java.util.Optional;

import com.example.demo.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 할 일 생성/수정/삭제용 저장소.
 */
public interface TodoCommandRepository extends JpaRepository<Todo, Long> {

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자의 할 일을 단일 DELETE 쿼리로 일괄 삭제한다.
     * 영속성 컨텍스트를 거치지 않으므로 호출 전후로 해당 할 일 엔티티를 사용하지 않아야 한다.
     */
    @Modifying
    @Query("delete from Todo t where t.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
