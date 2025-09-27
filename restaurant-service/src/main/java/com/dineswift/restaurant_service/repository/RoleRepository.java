package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Role;
import com.dineswift.restaurant_service.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRoleName(RoleName roleName);
}
