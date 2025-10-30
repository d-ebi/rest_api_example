package com.example.api.repository.spec;

import com.example.api.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * ユーザー検索用のSpecificationユーティリティ。
 */
public class UserSpecifications {
    /**
     * name列に対して部分一致検索を行うSpecificationを返します。
     * 引数が空の場合は無条件（全件）のSpecificationを返します。
     *
     * @param name 部分一致検索キーワード
     * @return name LIKE '%keyword%' を表すSpecification
     */
    public static Specification<UserEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.where(null);
        return (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
    }
}
