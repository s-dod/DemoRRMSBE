package com.example.rrms.authsecurity.security.user;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    /** username == user id (string), as stored in the JWT "sub" claim. */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return loadUserById(UUID.fromString(userId));
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean tenantActive = true;
        if (user.getTenantId() != null) {
            tenantActive = tenantRepository.findById(user.getTenantId())
                    .map(t -> t.getStatus() == TenantStatus.ACTIVE)
                    .orElse(false);
        }
        return UserPrincipal.from(user, tenantActive);
    }
}