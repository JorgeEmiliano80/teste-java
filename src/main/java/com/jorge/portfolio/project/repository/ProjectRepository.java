package com.jorge.portfolio.project.repository;

import com.jorge.portfolio.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
