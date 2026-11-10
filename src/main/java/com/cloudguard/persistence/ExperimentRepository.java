package com.cloudguard.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ExperimentRepository extends JpaRepository<ExperimentEntity,String> {
    List<ExperimentEntity> findTop50ByOrderByCreatedAtDesc();
}
