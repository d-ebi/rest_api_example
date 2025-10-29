package com.example.api.repository;

import com.example.api.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * ユーザーエンティティのSpring Data JPAリポジトリ。
 * 派生クエリとSpecification実行を提供します。
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    /** 指定したnameが存在するか */
    boolean existsByName(String name);
    /** 指定ID以外で同名が存在するか（更新時の一意制約確認） */
    boolean existsByNameAndIdNot(String name, Long id);
    /** name部分一致での件数 */
    long countByNameContaining(String name);
    /** name部分一致でのページ取得 */
    Page<UserEntity> findByNameContaining(String name, Pageable pageable);

    /** 職歴をEAGERロードしてID検索 */
    @EntityGraph(attributePaths = {"careerHistories"})
    Optional<UserEntity> findWithCareerHistoriesById(Long id);
}
