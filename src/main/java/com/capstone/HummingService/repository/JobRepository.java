package com.capstone.HummingService.repository;

import com.capstone.HummingService.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByPublicJobId(String publicJobId);
}
