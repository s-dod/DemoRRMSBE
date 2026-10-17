package com.example.rrms;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.modal.Task;
import com.example.rrms.authsecurity.domain.modal.TechnicalTicket;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.dto.request.CreateTaskRequest;
import com.example.rrms.authsecurity.dto.request.CreateTechnicalTicketRequest;
import com.example.rrms.authsecurity.dto.request.CreateUserRequest;
import com.example.rrms.authsecurity.dto.response.TechnicalTicketResponse;
import com.example.rrms.authsecurity.dto.response.TenantResponse;
import com.example.rrms.authsecurity.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.record.RecordModule;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ModelMapperTest {

    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        modelMapper.registerModule(new RecordModule());
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true);
    }

    @Test
    void testRequestRecordToEntity() {
        CreateTechnicalTicketRequest request = new CreateTechnicalTicketRequest(
                "Wifi Issue", "NETWORK", "HIGH", "Cannot connect to resort wifi"
        );
        TechnicalTicket ticket = new TechnicalTicket();
        modelMapper.map(request, ticket);

        assertEquals("Wifi Issue", ticket.getTitle());
        assertEquals("NETWORK", ticket.getCategory());
        assertEquals("HIGH", ticket.getPriority());
        assertEquals("Cannot connect to resort wifi", ticket.getDescription());
    }

    @Test
    void testTaskRequestRecordToEntity() {
        UUID staffId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("Clean Villa", StaffType.HOUSEKEEPING, staffId);
        Task task = new Task();
        modelMapper.map(request, task);

        assertEquals("Clean Villa", task.getTitle());
        assertEquals(StaffType.HOUSEKEEPING, task.getDepartment());
    }

    @Test
    void testUserRequestRecordToEntity() {
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", "1234567890", Role.STAFF, StaffType.HOUSEKEEPING);
        User user = new User();
        modelMapper.map(request, user);

        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    void testEntityToResponseRecord() {
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        TechnicalTicket ticket = new TechnicalTicket();
        ticket.setId(ticketId);
        ticket.setTenantId(tenantId);
        ticket.setTenantCode("PALM");
        ticket.setResortName("Palm Resort");
        ticket.setOwnerId(ownerId);
        ticket.setTitle("Broken AC");
        ticket.setCategory("SYSTEM");
        ticket.setPriority("HIGH");
        ticket.setStatus("OPEN");
        ticket.setDescription("AC not cooling in Room 102");
        ticket.setOwnerName("Manager Bob");
        ticket.setOwnerEmail("bob@resort.com");

        TechnicalTicketResponse response = modelMapper.map(ticket, TechnicalTicketResponse.class);
        assertNotNull(response);
        assertEquals(ticketId, response.id());
        assertEquals(tenantId, response.tenantId());
        assertEquals("PALM", response.tenantCode());
        assertEquals("Palm Resort", response.resortName());
        assertEquals(ownerId, response.ownerId());
        assertEquals("Broken AC", response.title());
        assertEquals("SYSTEM", response.category());
        assertEquals("HIGH", response.priority());
        assertEquals("OPEN", response.status());
        assertEquals("AC not cooling in Room 102", response.description());
        assertEquals("Manager Bob", response.ownerName());
        assertEquals("bob@resort.com", response.ownerEmail());
    }

    @Test
    void testUserToUserResponse() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Alice");
        user.setEmail("alice@resort.com");
        user.setRole(Role.MANAGEMENT);

        UserResponse response = modelMapper.map(user, UserResponse.class);
        assertNotNull(response);
        assertEquals("Alice", response.name());
        assertEquals("alice@resort.com", response.email());
        assertEquals(Role.MANAGEMENT, response.role());
    }

    @Test
    void testTenantToTenantResponse() {
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setCode("ROYAL");
        tenant.setName("Royal Palms");
        tenant.setStatus(com.example.rrms.authsecurity.domain.enums.TenantStatus.ACTIVE);

        TenantResponse response = modelMapper.map(tenant, TenantResponse.class);
        assertNotNull(response);
        assertEquals("ROYAL", response.code());
        assertEquals("Royal Palms", response.name());
        assertEquals(com.example.rrms.authsecurity.domain.enums.TenantStatus.ACTIVE, response.status());
    }
}
