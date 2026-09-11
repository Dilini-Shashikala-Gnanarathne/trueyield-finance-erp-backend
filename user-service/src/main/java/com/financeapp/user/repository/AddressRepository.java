package com.financeapp.user.repository;

import com.financeapp.user.domain.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, String> {
    Optional<AddressEntity> findByUserId(String userId);
}
