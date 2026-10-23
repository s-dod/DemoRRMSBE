package com.example.rrms.superadmin.repository;

import com.example.rrms.superadmin.entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TicketReplyRepository extends JpaRepository<TicketReply, UUID> {
}
