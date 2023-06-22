package com.study.domain.repository;

import com.study.domain.model.JoinGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinGroupRepository extends JpaRepository<JoinGroup, Long> {

}
