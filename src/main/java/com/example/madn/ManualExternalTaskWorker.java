package com.example.madn;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ManualExternalTaskWorker {

	private static final Logger log = LoggerFactory.getLogger(ManualExternalTaskWorker.class);
	private static final Random rnd = new Random();
	private static final String WORKER_ID = "manual-roll-worker";
	private static final long LOCK_DURATION_MS = 30_000L;

	private final ExternalTaskService externalTaskService;
	private final RuntimeService runtimeService;

	public ManualExternalTaskWorker(ExternalTaskService externalTaskService, RuntimeService runtimeService) {
		this.externalTaskService = externalTaskService;
		this.runtimeService = runtimeService;
	}

	public boolean rollOnce(String processInstanceId) {
		var diceTask = fetchAndLockSingleDiceTask(processInstanceId);
		if (diceTask == null) {
			log.info("[{}] Kein würfelbarer External Task gefunden – wahrscheinlich wartet der Prozess bereits.", processInstanceId);
			return false;
		}

		handleDiceTask(diceTask);
		return true;
	}

	// -------- Fetch & Lock (atomar) --------

	private ExternalTask fetchAndLockSingleDiceTask(String processInstanceId) {
		// Priorität: start vor normal
		var task = fetchAndLockOne(processInstanceId, List.of("rollDiceStart"));
		if (task == null) {
			task = fetchAndLockOne(processInstanceId, List.of("rollDiceNormal"));
		}
		return task;
	}

	/**
	 * Holt EXAKT EINEN Task für diese Prozessinstanz und die angegebenen Topics
	 * und lockt ihn atomar für WORKER_ID.
	 * <p>
	 * WICHTIG: Liefert nur ungelockte Tasks (bzw. deren Lock abgelaufen ist).
	 */
	private ExternalTask fetchAndLockOne(String processInstanceId, List<String> topics) {
		var builder = externalTaskService.fetchAndLock(1, WORKER_ID)
				.processInstanceId(processInstanceId);

		for (String topic : topics) {
			builder = builder.topic(topic, LOCK_DURATION_MS);
		}

		return builder.execute()
				.stream()
				.findFirst()
				.orElse(null);
	}

	// -------- Handling --------

	private void handleDiceTask(ExternalTask task) {
		switch (task.getTopicName()) {
			case "rollDiceStart" -> handleRollDiceStart(task);
			case "rollDiceNormal" -> handleRollDiceNormal(task);
			default -> log.warn("Unbekannter Dice-Task {}", task.getTopicName());
		}
	}

	private void handleRollDiceStart(ExternalTask task) {
		int d1 = 1 + rnd.nextInt(6);
		int d2 = 1 + rnd.nextInt(6);
		boolean pasch = d1 == d2;

		log.info("[{}] Startbereich würfeln: {} + {} => Pasch={}", task.getProcessInstanceId(), d1, d2, pasch);

		externalTaskService.complete(task.getId(), WORKER_ID, Map.of(
				"dice1", d1,
				"dice2", d2,
				"isPasch", pasch
		));
	}

	private void handleRollDiceNormal(ExternalTask task) {
		int pos = getNumber(runtimeService.getVariable(task.getProcessInstanceId(), "position"), 1);

		int dice = 1 + rnd.nextInt(6);
		boolean wouldEnterGoal = (pos + dice) >= GameService.GOAL_POS;
		boolean exactGoal = (pos + dice) == GameService.GOAL_POS;

		log.info("[{}] Würfeln: {} (position={}) wouldEnterGoal={} exactGoal={}",
				task.getProcessInstanceId(), dice, pos, wouldEnterGoal, exactGoal);

		externalTaskService.complete(task.getId(), WORKER_ID, Map.of(
				"dice", dice,
				"wouldEnterGoal", wouldEnterGoal,
				"exactGoal", exactGoal
		));
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
