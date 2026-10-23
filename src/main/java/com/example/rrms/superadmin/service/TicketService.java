package com.example.rrms.superadmin.service;

import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import com.example.rrms.common.exception.ApiException;
import com.example.rrms.superadmin.DTO.request.CreateTicketRequest;
import com.example.rrms.superadmin.DTO.request.TicketReplyRequest;
import com.example.rrms.superadmin.DTO.request.UpdateTicketRequest;
import com.example.rrms.superadmin.DTO.response.TicketDetailResponse;
import com.example.rrms.superadmin.DTO.response.TicketReplyResponse;
import com.example.rrms.superadmin.DTO.response.TicketResponse;
import com.example.rrms.superadmin.entity.PlatformTicket;
import com.example.rrms.superadmin.entity.TicketReply;
import com.example.rrms.superadmin.enums.AuditSeverity;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.enums.TicketStatus;
import com.example.rrms.superadmin.repository.PlatformTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final PlatformTicketRepository ticketRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TicketResponse> getAll() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAll(Pageable pageable) {
        return ticketRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse getById(UUID id) {
        PlatformTicket ticket = findTicketOrThrow(id);
        return toDetailResponse(ticket);
    }

    @Transactional
    public TicketDetailResponse create(CreateTicketRequest req) {
        Optional<UserPrincipal> current = CurrentUser.optional();
        UUID creatorId = current.map(UserPrincipal::getId)
                .orElse(req.creatorId() != null ? req.creatorId() : UUID.randomUUID());

        String ownerName = req.ownerName() != null && !req.ownerName().isBlank()
                ? req.ownerName().trim()
                : current.map(UserPrincipal::getEmail).orElse("Super Admin");

        String ownerEmail = req.ownerEmail() != null && !req.ownerEmail().isBlank()
                ? req.ownerEmail().trim()
                : current.map(UserPrincipal::getEmail).orElse("admin@rrms.com");

        PlatformTicket ticket = PlatformTicket.create(
                req.tenantCode().trim(),
                req.resortName() != null ? req.resortName().trim() : req.tenantCode().trim(),
                ownerName,
                ownerEmail,
                creatorId,
                req.title().trim(),
                req.category(),
                req.priority(),
                req.description().trim()
        );

        ticket = ticketRepository.save(ticket);

        auditService.record(
                ownerName,
                "TICKET_CREATE",
                "Created support ticket #" + ticket.getId() + " - " + ticket.getTitle(),
                req.priority() == TicketPriority.CRITICAL ? AuditSeverity.CRITICAL : AuditSeverity.INFO,
                ticket.getTenantCode(),
                null
        );

        return toDetailResponse(ticket);
    }

    @Transactional
    public TicketDetailResponse update(UUID id, UpdateTicketRequest req) {
        PlatformTicket ticket = findTicketOrThrow(id);
        Optional<UserPrincipal> current = CurrentUser.optional();
        String actor = current.map(UserPrincipal::getEmail).orElse("SUPER_ADMIN");
        String role = current.map(p -> p.getRole().name()).orElse("SUPER_ADMIN");

        if (req.assigneeId() != null) {
            ticket.assignAgent(req.assigneeId(), req.assigneeName(), req.assigneeEmail(), actor, role);
        }

        if (req.priority() != null) {
            if (req.priority() == TicketPriority.CRITICAL) {
                ticket.escalateToCritical(actor, role);
            }
        }

        if (req.status() != null) {
            if (req.status() == TicketStatus.RESOLVED) {
                ticket.resolve(req.resolutionNotes() != null ? req.resolutionNotes() : "Resolved by " + actor, actor, role);
            } else if (req.status() == TicketStatus.CLOSED) {
                ticket.close(actor, role);
            }
        }

        ticket = ticketRepository.save(ticket);

        auditService.record(
                actor,
                "TICKET_UPDATE",
                "Updated ticket #" + ticket.getId() + " status=" + ticket.getStatus() + ", priority=" + ticket.getPriority(),
                AuditSeverity.INFO,
                ticket.getTenantCode(),
                null
        );

        return toDetailResponse(ticket);
    }

    @Transactional
    public TicketReplyResponse addReply(UUID id, TicketReplyRequest req) {
        PlatformTicket ticket = findTicketOrThrow(id);
        Optional<UserPrincipal> current = CurrentUser.optional();

        UUID senderId = req.senderId() != null ? req.senderId()
                : current.map(UserPrincipal::getId).orElse(UUID.randomUUID());

        String senderName = req.senderName() != null && !req.senderName().isBlank() ? req.senderName().trim()
                : current.map(UserPrincipal::getEmail).orElse("Support Team");

        String senderRole = req.senderRole() != null && !req.senderRole().isBlank() ? req.senderRole().trim()
                : current.map(p -> p.getRole().name()).orElse("SUPER_ADMIN");

        TicketReply reply = ticket.addReply(senderId, senderName, senderRole, req.message().trim());
        ticket = ticketRepository.save(ticket);

        auditService.record(
                senderName,
                "TICKET_REPLY",
                "Added reply on ticket #" + ticket.getId(),
                AuditSeverity.INFO,
                ticket.getTenantCode(),
                null
        );

        return new TicketReplyResponse(
                reply.getId(),
                reply.getSenderId(),
                reply.getSenderName(),
                reply.getSenderRole(),
                reply.getMessage(),
                reply.getTimestamp()
        );
    }

    private PlatformTicket findTicketOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("TICKET_NOT_FOUND", "Ticket not found with ID: " + id));
    }

    public TicketResponse toResponse(PlatformTicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTenantCode(),
                ticket.getResortName(),
                ticket.getOwnerName(),
                ticket.getOwnerEmail(),
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssigneeId(),
                ticket.getAssigneeName(),
                ticket.getDescription(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketDetailResponse toDetailResponse(PlatformTicket ticket) {
        List<TicketReplyResponse> replies = ticket.getReplies().stream()
                .map(r -> new TicketReplyResponse(
                        r.getId(),
                        r.getSenderId(),
                        r.getSenderName(),
                        r.getSenderRole(),
                        r.getMessage(),
                        r.getTimestamp()
                ))
                .collect(Collectors.toList());

        List<TicketDetailResponse.TicketAttachmentResponse> attachments = ticket.getAttachments().stream()
                .map(a -> new TicketDetailResponse.TicketAttachmentResponse(
                        a.getId(),
                        a.getName(),
                        a.getType(),
                        a.getSize(),
                        a.getStorageKey(),
                        a.getDataUrl(),
                        a.getTimestamp()
                ))
                .collect(Collectors.toList());

        List<TicketDetailResponse.TicketHistoryResponse> history = ticket.getHistory().stream()
                .map(h -> new TicketDetailResponse.TicketHistoryResponse(
                        h.getId(),
                        h.getActorName(),
                        h.getActorRole(),
                        h.getAction(),
                        h.getDetails(),
                        h.getTimestamp()
                ))
                .collect(Collectors.toList());

        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTenantCode(),
                ticket.getResortName(),
                ticket.getOwnerName(),
                ticket.getOwnerEmail(),
                ticket.getCreatorId(),
                ticket.getAssigneeId(),
                ticket.getAssigneeName(),
                ticket.getAssigneeEmail(),
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getDescription(),
                ticket.getResolutionNotes(),
                replies,
                attachments,
                history,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
