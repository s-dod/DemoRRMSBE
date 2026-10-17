package com.example.rrms.authsecurity.security.mfa;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.modal.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class StaticOtpMfaService implements MfaService {
    public boolean isRequired(User user) { return user.getRole() != Role.GUEST; }
    public boolean isEnrolled(User user) { return true; }                    // no authenticator needed in dev
    public String beginEnrollment(User user) { throw new UnsupportedOperationException("dev profile: use code 123456"); }
    public boolean verify(User user, String code) { return "123456".equals(code); }
}
