package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveNormallyDelegate implements JavaDelegate {

	private static final Logger log = LoggerFactory.getLogger(MoveNormallyDelegate.class);

	@Override
	public void execute(DelegateExecution execution) {
		int pos = getNumber(execution.getVariable("position"), 1);
		int dice = getNumber(execution.getVariable("dice"), 0);

		int newPos = pos + dice;
		execution.setVariable("position", newPos);

		log.info("[{}] Ziehen: {} -> {} (dice={})", execution.getProcessInstanceId(), pos, newPos, dice);
	}

	private int getNumber(Object value, int defaultValue) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String str && !str.isBlank()) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException ignored) {
				// ignore and fall through to default
			}
		}
		return defaultValue;
	}
}
