package com.example.madn.camunda.delegate;

import com.example.madn.GameService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveIntoGoalDelegate implements JavaDelegate {

	private static final Logger log = LoggerFactory.getLogger(MoveIntoGoalDelegate.class);

	@Override
	public void execute(DelegateExecution execution) {
		execution.setVariable("position", GameService.GOAL_POS);
		execution.setVariable("inGoal", true);

		log.info("[{}] Exakt! Figur zieht ins Ziel. 🏁", execution.getProcessInstanceId());
	}
}
