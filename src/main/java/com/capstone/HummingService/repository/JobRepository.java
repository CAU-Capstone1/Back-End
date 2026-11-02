package com.capstone.HummingService.repository;

import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByPublicJobId(String publicJobId);

    List<Job> findByStatusIn(Collection<JobStatus> statuses);
}
