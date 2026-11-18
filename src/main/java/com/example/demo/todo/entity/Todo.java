package com.example.demo.todo.entity;

import java.time.LocalDate;

import com.example.demo.global.entity.BaseTimeEntity;
import com.example.demo.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 할 일 엔티티.
 * User와 N:1 단방향으로 매핑하며, 사용자 삭제 시 할 일 일괄 삭제는 UserCommandService에서 처리한다.
 */
@Getter
@Entity
@Table(name = "todos", indexes = @Index(name = "idx_todos_user_id_completed", columnList = "user_id, completed"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    private LocalDate dueDate;

    private Todo(User user, String title, String description, LocalDate dueDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public static Todo create(User user, String title, String description, LocalDate dueDate) {
        return new Todo(user, title, description, dueDate);
    }

    /**
     * 부분 수정. null로 전달된 필드는 변경하지 않는다.
     */
    public void update(String title, String description, LocalDate dueDate) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (dueDate != null) {
            this.dueDate = dueDate;
        }
    }

    public void changeCompletion(boolean completed) {
        this.completed = completed;
    }
}
