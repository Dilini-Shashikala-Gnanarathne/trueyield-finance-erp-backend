package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminRepository extends JpaRepository<AdminEntity, String> {
    List<AdminEntity> findAll();
}
