package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterBoardDelegate implements JavaDelegate {

	private static final Logger log = LoggerFactory.getLogger(EnterBoardDelegate.class);

	@Override
	public void execute(DelegateExecution execution) {
		execution.setVariable("inStartArea", false);
		execution.setVariable("position", 1);

		log.info("[{}] Pasch! Figur geht aufs Startfeld (position=1).", execution.getProcessInstanceId());
	}
}
