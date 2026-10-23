package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, UUID> {
}
