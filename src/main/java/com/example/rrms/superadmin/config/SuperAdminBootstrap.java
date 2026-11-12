package com.example.rrms.superadmin.config;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.superadmin.entity.PlatformConfig;
import com.example.rrms.superadmin.entity.PlatformTicket;
import com.example.rrms.superadmin.enums.TicketCategory;
import com.example.rrms.superadmin.enums.TicketPriority;
import com.example.rrms.superadmin.repository.PlatformConfigRepository;
import com.example.rrms.superadmin.repository.PlatformTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PlatformConfigRepository configRepository;
    private final PlatformTicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        bootstrapSuperAdmin();
        bootstrapConfig();
        bootstrapDemoData();
    }

    private void bootstrapSuperAdmin() {
        // 1. Seed admin@rrms.com
        if (userRepository.findByEmailIgnoreCase("admin@rrms.com").isEmpty()) {
            User admin = new User();
            admin.setName("Platform Admin");
            admin.setEmail("admin@rrms.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin@RRMS12345"));
            admin.setRole(Role.SUPER_ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setMustChangePassword(false);
            userRepository.save(admin);
            log.info("Bootstrapped Super Admin user: admin@rrms.com");
        }

        // 2. Seed admin@rrms.demo
        if (userRepository.findByEmailIgnoreCase("admin@rrms.demo").isEmpty()) {
            User demoAdmin = new User();
            demoAdmin.setName("Platform Administrator");
            demoAdmin.setEmail("admin@rrms.demo");
            demoAdmin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            demoAdmin.setRole(Role.SUPER_ADMIN);
            demoAdmin.setStatus(UserStatus.ACTIVE);
            demoAdmin.setMustChangePassword(false);
            userRepository.save(demoAdmin);
            log.info("Bootstrapped Demo Super Admin user: admin@rrms.demo");
        }
    }

    private void bootstrapConfig() {
        if (configRepository.findById(PlatformConfig.GLOBAL_CONFIG).isEmpty()) {
            configRepository.save(PlatformConfig.createDefault());
            log.info("Bootstrapped Global Platform Config");
        }
    }

    private void bootstrapDemoData() {
        if (tenantRepository.count() == 0) {
            Tenant palm = new Tenant();
            palm.setCode("PALM");
            palm.setName("The Palm Luxury Resort");
            palm.setCity("Candolim");
            palm.setState("Goa");
            palm.setLocation("Candolim, Goa · India");
            palm.setAddress("Candolim Beach Road, North Goa");
            palm.setPhone("9876543210");
            palm.setRoomsCount(35);
            palm.setTier("Enterprise");
            palm.setStatus(TenantStatus.ACTIVE);
            palm = tenantRepository.save(palm);

            User owner = new User();
            owner.setTenantId(palm.getId());
            owner.setName("Vikram Oberoi");
            owner.setEmail("owner@rrms.demo");
            owner.setPhone("9876543210");
            owner.setPasswordHash(passwordEncoder.encode("Resort@123"));
            owner.setRole(Role.OWNER);
            owner.setStatus(UserStatus.ACTIVE);
            owner.setMustChangePassword(false);
            userRepository.save(owner);

            // Sample Ticket
            PlatformTicket ticket = PlatformTicket.create(
                    palm.getCode(),
                    palm.getName(),
                    owner.getName(),
                    owner.getEmail(),
                    owner.getId(),
                    "Payment gateway webhook failure during check-out",
                    TicketCategory.PAYMENTS,
                    TicketPriority.HIGH,
                    "Razorpay webhook is occasionally timing out when confirming guest folios during high traffic."
            );
            ticketRepository.save(ticket);
            log.info("Bootstrapped initial demo tenant PALM and sample ticket");
        }
    }
}

