package com.aneirine.service.api;

import com.aneirine.service.entities.PayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayerRepository extends JpaRepository<PayerEntity, Long> {
    boolean existsByPaypalId(String id);
    PayerEntity findByPaypalId(String id);
}
