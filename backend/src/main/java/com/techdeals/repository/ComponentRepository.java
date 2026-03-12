package com.techdeals.repository;

import com.techdeals.model.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    List<Component> findByNameContainingIgnoreCase(String name);
    Optional<Component> findByNameIgnoreCase(String name);
}
