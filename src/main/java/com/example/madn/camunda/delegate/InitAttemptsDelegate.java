package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class InitAttemptsDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		execution.setVariable("attempts", 0);
	}
}
