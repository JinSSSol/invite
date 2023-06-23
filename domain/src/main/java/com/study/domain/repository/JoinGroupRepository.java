package com.study.domain.repository;

import com.study.domain.model.JoinGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinGroupRepository extends JpaRepository<JoinGroup, Long> {
    Optional<JoinGroup> findByGroup_Id(Long groupId);
    Optional<JoinGroup> findByUser_IdAndGroup_Id(Long userId, Long groupId);
}
