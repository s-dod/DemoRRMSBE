package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.PlatformTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlatformTicketRepository extends JpaRepository<PlatformTicket, UUID> {
}
