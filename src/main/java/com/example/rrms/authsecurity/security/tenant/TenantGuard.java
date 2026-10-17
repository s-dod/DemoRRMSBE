package com.example.rrms.authsecurity.security.tenant;

import com.example.rrms.authsecurity.security.user.CurrentUser;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantGuard")
public class TenantGuard {
    public boolean sameTenant(UUID tenantId) {
        UUID mine = CurrentUser.get().getTenantId();
        return mine != null && mine.equals(tenantId);
    }
}
