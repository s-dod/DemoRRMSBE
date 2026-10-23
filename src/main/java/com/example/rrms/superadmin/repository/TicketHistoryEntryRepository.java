package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.TicketHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TicketHistoryEntryRepository extends JpaRepository<TicketHistoryEntry, UUID> {
}
