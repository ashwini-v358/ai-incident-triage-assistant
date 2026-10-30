package com.incident.triage.repository;

import com.incident.triage.model.entity.Incident;
import com.incident.triage.model.entity.IncidentCategory;
import com.incident.triage.model.entity.IncidentSeverity;
import com.incident.triage.model.entity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByStatus(IncidentStatus status);

    long countBySeverity(IncidentSeverity severity);

    long countByCategory(IncidentCategory category);

    @Query("SELECT i.category, COUNT(i) FROM Incident i GROUP BY i.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countGroupBySeverity();

    @Query("SELECT i.analysisProvider, COUNT(i) FROM Incident i GROUP BY i.analysisProvider")
    List<Object[]> countGroupByAnalysisProvider();

    @Query("SELECT AVG(i.confidenceScore) FROM Incident i WHERE i.confidenceScore IS NOT NULL")
    Double getAverageConfidenceScore();
}
