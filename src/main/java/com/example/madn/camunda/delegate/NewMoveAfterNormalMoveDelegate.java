package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewMoveAfterNormalMoveDelegate implements JavaDelegate {

	private static final Logger log = LoggerFactory.getLogger(NewMoveAfterNormalMoveDelegate.class);

	@Override
	public void execute(DelegateExecution execution) {
		log.info("[{}] Neuer Zug nach normalem Zug.", execution.getProcessInstanceId());
	}
}
