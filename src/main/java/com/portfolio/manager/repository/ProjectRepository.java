package com.portfolio.manager.repository;

import com.portfolio.manager.entity.Project;
import com.portfolio.manager.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

		Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

		@Query("""
				SELECT p FROM Project p
				WHERE (:status IS NULL OR p.status = :status)
				  AND (:name IS NULL OR p.name ILIKE :name)
				  AND (:startDateFrom IS NULL OR p.startDate >= :startDateFrom)
				  AND (:startDateTo IS NULL OR p.startDate <= :startDateTo)
				""")
		Page<Project> findWithFilters(
				@Param("status") ProjectStatus status,
				@Param("name") String name,
				@Param("startDateFrom") LocalDate startDateFrom,
				@Param("startDateTo") LocalDate startDateTo,
				Pageable pageable
		);

		@Query("SELECT p.status, COUNT(p), SUM(p.totalBudget) FROM Project p GROUP BY p.status")
		List<Object[]> findStatusSummary();

		@Query("""
				SELECT AVG(CAST(DATEDIFF(DAY, p.startDate, p.actualEndDate) AS double))
				FROM Project p WHERE p.status = 'ENCERRADO' AND p.actualEndDate IS NOT NULL
				""")
		Double findAverageDurationOfClosedProjects();

		@Query("SELECT DISTINCT memberId FROM Project p JOIN p.memberIds memberId WHERE p.status IN :statuses")
		List<Long> findUniqueMemberIdsByStatusIn(@Param("statuses") List<ProjectStatus> statuses);

		@Query("""
				SELECT COUNT(p) FROM Project p
				JOIN p.memberIds memberId
				WHERE memberId = :memberId
				  AND p.status NOT IN :excludedStatuses
				""")
		long countActiveProjectsByMemberId(
				@Param("memberId") Long memberId,
				@Param("excludedStatuses") List<ProjectStatus> excludedStatuses
		);

		@Query("SELECT p FROM Project p JOIN p.memberIds memberId WHERE memberId = :memberId")
		List<Project> findAllByMemberId(@Param("memberId") Long memberId);
}
