package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.modal.TechnicalTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TechnicalTicketRepository extends JpaRepository<TechnicalTicket, UUID> {

    @Query("select t from TechnicalTicket t where t.tenant.id = :tenantId order by t.createdAt desc")
    List<TechnicalTicket> findByTenantIdOrderByCreatedAtDesc(@Param("tenantId") UUID tenantId);

    List<TechnicalTicket> findAllByOrderByCreatedAtDesc();
}
