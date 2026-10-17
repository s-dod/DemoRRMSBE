package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.domain.modal.AuditLog;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.repository.AuditLogRepository;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UserPrincipal actor, String action, String resourceType,
                    String resourceId, String result, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(actor.getTenantId());
        auditLog.setActorId(actor.getId());
        auditLog.setActorRole(actor.getRole().name());
        auditLog.setActorStaffType(actor.getStaffType() == null ? null : actor.getStaffType().name());
        fill(auditLog, action, resourceType, resourceId, result, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, String action, String resourceType, Object resourceId,
                    String result, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(actor.getTenantId());
        auditLog.setActorId(actor.getId());
        auditLog.setActorRole(actor.getRole().name());
        auditLog.setActorStaffType(actor.getStaffType() == null ? null : actor.getStaffType().name());
        fill(auditLog, action, resourceType, resourceId == null ? null : resourceId.toString(), result, details);
    }

    private void fill(AuditLog auditLog, String action, String type, String id, String result, String details) {
        auditLog.setAction(action);
        auditLog.setResourceType(type);
        auditLog.setResourceId(id);
        auditLog.setResult(result);
        auditLog.setDetails(details);
        auditLog.setIp(currentIp());
        auditLogRepository.save(auditLog);
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}
