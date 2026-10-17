package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.modal.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("select t from Task t where t.id = :id and t.tenant.id = :tenantId")
    Optional<Task> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("select t from Task t where t.tenant.id = :tenantId")
    List<Task> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("select t from Task t where t.tenant.id = :tenantId and t.assignedStaff.id = :assignedStaffId")
    List<Task> findByTenantIdAndAssignedStaffId(@Param("tenantId") UUID tenantId, @Param("assignedStaffId") UUID assignedStaffId);
}
