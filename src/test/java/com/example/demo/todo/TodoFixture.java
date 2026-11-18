package com.example.demo.todo;

import java.time.LocalDate;

import com.example.demo.todo.entity.Todo;
import com.example.demo.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 테스트용 엔티티 생성 헬퍼.
 */
public final class TodoFixture {

    private TodoFixture() {
    }

    public static User user(Long id) {
        User user = User.create("user" + id + "@example.com", "사용자" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static Todo todo(Long id, User user, String title) {
        Todo todo = Todo.create(user, title, "설명", LocalDate.of(2026, 10, 20));
        ReflectionTestUtils.setField(todo, "id", id);
        return todo;
    }
}
