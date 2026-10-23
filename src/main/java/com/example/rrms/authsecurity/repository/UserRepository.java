package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.modal.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    @Query("select count(u) > 0 from User u where lower(u.email) = lower(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("select u from User u where lower(u.email) = lower(:email) and u.tenant.id = :tenantId")
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") UUID tenantId);

    @Query("select u from User u where lower(u.email) = lower(:email) and u.tenant is null")
    Optional<User> findByEmailAndTenantIdIsNull(@Param("email") String email);            // SUPER_ADMIN login

    List<User> findByEmail(String email);

    @Query("select u from User u where u.id = :id and u.tenant.id = :tenantId")
    Optional<User> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("select u from User u where u.tenant.id = :tenantId")
    List<User> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("select u from User u where u.tenant.id = :tenantId and u.role = :role")
    List<User> findByTenantIdAndRole(@Param("tenantId") UUID tenantId, @Param("role") Role role);

    @Query("select count(u) > 0 from User u where lower(u.email) = lower(:email) and u.tenant.id = :tenantId")
    boolean existsByEmailAndTenantId(@Param("email") String email, @Param("tenantId") UUID tenantId);

    @Query("select count(u) > 0 from User u where lower(u.email) = lower(:email) and u.tenant is null")
    boolean existsByEmailAndTenantIdIsNull(@Param("email") String email);

    List<User> findByRole(Role role);

    long countByRole(Role role);

    @Query("select u from User u where u.tenant.id = :tenantId and u.role = :role")
    Optional<User> findFirstByTenantIdAndRole(@Param("tenantId") UUID tenantId, @Param("role") Role role);
}