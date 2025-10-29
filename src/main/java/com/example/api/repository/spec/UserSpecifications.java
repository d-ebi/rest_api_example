package com.example.api.repository.spec;

import com.example.api.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    public static Specification<UserEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.where(null);
        return (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
    }
}

