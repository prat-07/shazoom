package com.pratham.backend.repository;

import com.pratham.backend.entity.FingerprintId;
import com.pratham.backend.entity.FingerprintRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FingerprintRepository
        extends JpaRepository<FingerprintRecord, FingerprintId> {

    List<FingerprintRecord> findByAddressIn(List<Integer> addresses);
}
