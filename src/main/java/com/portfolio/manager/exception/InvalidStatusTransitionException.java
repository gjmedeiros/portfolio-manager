package com.portfolio.manager.exception;

import com.portfolio.manager.enums.ProjectStatus;

public class InvalidStatusTransitionException extends BusinessException {

		public InvalidStatusTransitionException(ProjectStatus from, ProjectStatus to) {
				super(
						"Transição de status inválida: de '" + from.getDisplayName() + "' para '" + to.getDisplayName() + "' não é permitida.");
		}
}
