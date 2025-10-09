package com.capstone.HummingService.repository;

import com.capstone.HummingService.domain.Audio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioRepository extends JpaRepository<Audio, Long> {
}
