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
		processFollowUpTasks(processInstanceId);
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

	private void processFollowUpTasks(String processInstanceId) {
		while (true) {
			var followUp = fetchAndLockFollowUpTask(processInstanceId);
			if (followUp == null) {
				return;
			}
			handleFollowUpTask(followUp);
		}
	}

	private ExternalTask fetchAndLockFollowUpTask(String processInstanceId) {
		// Priorität: enterBoard > moveNormally > moveIntoGoal
		var task = fetchAndLockOne(processInstanceId, List.of("enterBoard"));
		if (task == null) {
			task = fetchAndLockOne(processInstanceId, List.of("moveNormally"));
		}
		if (task == null) {
			task = fetchAndLockOne(processInstanceId, List.of("moveIntoGoal"));
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

	private void handleFollowUpTask(ExternalTask task) {
		switch (task.getTopicName()) {
			case "enterBoard" -> handleEnterBoard(task);
			case "moveNormally" -> handleMoveNormally(task);
			case "moveIntoGoal" -> handleMoveIntoGoal(task);
			default -> log.warn("Unbekannter Folge-Task {}", task.getTopicName());
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

	private void handleEnterBoard(ExternalTask task) {
		log.info("[{}] Pasch! Figur geht aufs Startfeld (position=1).", task.getProcessInstanceId());
		externalTaskService.complete(task.getId(), WORKER_ID, Map.of(
				"inStartArea", false,
				"position", 1
		));
	}

	private void handleMoveNormally(ExternalTask task) {
		int pos = getNumber(runtimeService.getVariable(task.getProcessInstanceId(), "position"), 1);
		int dice = getNumber(runtimeService.getVariable(task.getProcessInstanceId(), "dice"), 0);

		int newPos = pos + dice;
		log.info("[{}] Ziehen: {} -> {}", task.getProcessInstanceId(), pos, newPos);

		externalTaskService.complete(task.getId(), WORKER_ID, Map.of("position", newPos));
	}

	private void handleMoveIntoGoal(ExternalTask task) {
		log.info("[{}] Exakt! Figur zieht ins Ziel. 🏁", task.getProcessInstanceId());
		externalTaskService.complete(task.getId(), WORKER_ID, Map.of(
				"position", GameService.GOAL_POS,
				"inGoal", true
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
