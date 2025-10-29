package com.example.api.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * usersテーブルに対応するJPAエンティティ。
 * 日付はSQLiteのTEXT(yyyy/MM/dd)として保持します。
 */
@Entity
@Table(name = "users")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "age", nullable = false)
    private Integer age;

    // SQLite TEXT yyyy/MM/dd
    @Column(name = "birthday", nullable = false, length = 10)
    private String birthday;

    @Column(name = "height")
    private Double height;

    @Column(name = "zip_code", length = 8)
    private String zipCode;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CareerHistoryEntity> careerHistories;
}
