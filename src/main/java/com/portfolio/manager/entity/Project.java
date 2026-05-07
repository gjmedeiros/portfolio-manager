package com.portfolio.manager.entity;

import com.portfolio.manager.domain.RiskClassifier;
import com.portfolio.manager.enums.ProjectStatus;
import com.portfolio.manager.enums.RiskClassification;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		@Column(nullable = false, length = 200)
		private String name;

		@Column(name = "data_de_inicio", nullable = false)
		private LocalDate startDate;

		@Column(name = "data_final_esperada", nullable = false)
		private LocalDate expectedEndDate;

		@Column(name = "data_final_real")
		private LocalDate actualEndDate;

		@Column(name = "orcamento_total", nullable = false, precision = 15, scale = 2)
		private BigDecimal totalBudget;

		@Column(columnDefinition = "TEXT")
		private String description;

		@Column(name = "manager_id")
		private Long managerId;

		@Enumerated(EnumType.STRING)
		@Column(nullable = false, length = 30)
		private ProjectStatus status;

		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "project_members", joinColumns = @JoinColumn(name = "project_id"))
		@Column(name = "member_id")
		@Builder.Default
		private Set<Long> memberIds = new HashSet<>();

		@Transient
		public RiskClassification getRiskClassification() {
				return RiskClassifier.classify(this.totalBudget, this.startDate, this.expectedEndDate);
		}
}
