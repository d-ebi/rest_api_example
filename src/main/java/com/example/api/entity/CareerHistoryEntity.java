package com.example.api.entity;

import lombok.*;

import javax.persistence.*;

/**
 * career_historiesテーブルに対応するJPAエンティティ。
 * 期間はSQLiteのTEXT(yyyy/MM/dd)で保持します。
 */
@Entity
@Table(name = "career_histories")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CareerHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // SQLite TEXT yyyy/MM/dd
    @Column(name = "period_from", nullable = false, length = 10)
    private String periodFrom;

    @Column(name = "period_to", nullable = false, length = 10)
    private String periodTo;
}
