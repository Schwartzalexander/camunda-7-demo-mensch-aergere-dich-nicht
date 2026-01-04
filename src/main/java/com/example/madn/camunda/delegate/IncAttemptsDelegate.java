package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class IncAttemptsDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		Object v = execution.getVariable("attempts");
		int attempts = 0;

		if (v instanceof Number n) {
			attempts = n.intValue();
		} else if (v instanceof String s && !s.isBlank()) {
			attempts = Integer.parseInt(s);
		}

		execution.setVariable("attempts", attempts + 1);
	}
}
