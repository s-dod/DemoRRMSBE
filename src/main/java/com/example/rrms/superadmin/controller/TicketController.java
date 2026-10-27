package com.example.rrms.superadmin.controller;

import com.example.rrms.superadmin.DTO.request.CreateTicketRequest;
import com.example.rrms.superadmin.DTO.request.TicketReplyRequest;
import com.example.rrms.superadmin.DTO.request.UpdateTicketRequest;
import com.example.rrms.superadmin.DTO.response.TicketDetailResponse;
import com.example.rrms.superadmin.DTO.response.TicketReplyResponse;
import com.example.rrms.superadmin.DTO.response.TicketResponse;
import com.example.rrms.superadmin.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OWNER', 'MANAGEMENT')")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAll();
    }

    @GetMapping("/page")
    public Page<TicketResponse> getTicketsPaged(Pageable pageable) {
        return ticketService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public TicketDetailResponse getTicketById(@PathVariable UUID id) {
        return ticketService.getById(id);
    }

    @PostMapping
    public ResponseEntity<TicketDetailResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDetailResponse created = ticketService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TicketDetailResponse updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.update(id, request);
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<TicketReplyResponse> addReply(
            @PathVariable UUID id,
            @Valid @RequestBody TicketReplyRequest request
    ) {
        TicketReplyResponse reply = ticketService.addReply(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }
}


