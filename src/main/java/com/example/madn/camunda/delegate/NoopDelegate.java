package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class NoopDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		// intentionally do nothing
	}
}
