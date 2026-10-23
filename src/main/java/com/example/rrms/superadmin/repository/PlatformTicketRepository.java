package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.PlatformTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PlatformTicketRepository extends JpaRepository<PlatformTicket, UUID> {
    List<PlatformTicket> findAllByOrderByCreatedAtDesc();
    Page<PlatformTicket> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<PlatformTicket> findTop10ByOrderByCreatedAtDesc();
    long countByStatus(TicketStatus status);
    long countByPriorityAndStatusNot(TicketPriority priority, TicketStatus status);
    List<PlatformTicket> findByTenantCode(String tenantCode);
}
