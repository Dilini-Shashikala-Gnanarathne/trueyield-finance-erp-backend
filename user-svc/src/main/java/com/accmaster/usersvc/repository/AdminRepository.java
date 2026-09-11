package com.accmaster.usersvc.repository;

import com.accmaster.usersvc.domain.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminRepository extends JpaRepository<AdminEntity, String> {
    List<AdminEntity> findAll();
}
