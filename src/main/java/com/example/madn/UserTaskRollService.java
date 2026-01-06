package com.example.madn;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class UserTaskRollService {

	private static final Logger log = LoggerFactory.getLogger(UserTaskRollService.class);
	private static final Random rnd = new Random();

	private final TaskService taskService;
	private final RuntimeService runtimeService;

	public UserTaskRollService(TaskService taskService, RuntimeService runtimeService) {
		this.taskService = taskService;
		this.runtimeService = runtimeService;
	}

	public boolean rollOnce(String processInstanceId) {
		var diceTask = findSingleDiceTask(processInstanceId);
		if (diceTask.isEmpty()) {
			log.info("[{}] Kein würfelbarer User Task gefunden – wahrscheinlich wartet der Prozess bereits.", processInstanceId);
			return false;
		}

		handleDiceTask(diceTask.get());
		return true;
	}

	// -------- Find open user tasks --------

	private Optional<Task> findSingleDiceTask(String processInstanceId) {
		// Priorität: Startbereich vor normalem Wurf
		var startTask = findTask(processInstanceId, "Task_RollDice_StartArea");
		if (startTask.isPresent()) {
			return startTask;
		}
		return findTask(processInstanceId, "Task_RollDice_Normal");
	}

	private Optional<Task> findTask(String processInstanceId, String taskDefinitionKey) {
		return taskService.createTaskQuery()
				.processInstanceId(processInstanceId)
				.taskDefinitionKey(taskDefinitionKey)
				.active()
				.orderByTaskCreateTime()
				.desc()
				.listPage(0, 1)
				.stream()
				.findFirst();
	}

	// -------- Handling --------

	private void handleDiceTask(Task task) {
		switch (task.getTaskDefinitionKey()) {
			case "Task_RollDice_StartArea" -> handleRollDiceStart(task);
			case "Task_RollDice_Normal" -> handleRollDiceNormal(task);
			default -> log.warn("Unbekannter Dice-Task {}", task.getTaskDefinitionKey());
		}
	}

	private void handleRollDiceStart(Task task) {
		int d1 = 1 + rnd.nextInt(6);
		int d2 = 1 + rnd.nextInt(6);
		boolean pasch = d1 == d2;

		log.info("[{}] Startbereich würfeln: {} + {} => Pasch={}", task.getProcessInstanceId(), d1, d2, pasch);

		taskService.complete(task.getId(), Map.of(
				"dice1", d1,
				"dice2", d2,
				"isPasch", pasch
		));
	}

	private void handleRollDiceNormal(Task task) {
		int pos = getNumber(runtimeService.getVariable(task.getProcessInstanceId(), "position"), 1);

		int dice = 1 + rnd.nextInt(6);
		boolean wouldEnterGoal = (pos + dice) >= GameService.GOAL_POS;
		boolean exactGoal = (pos + dice) == GameService.GOAL_POS;

		log.info("[{}] Würfeln: {} (position={}) wouldEnterGoal={} exactGoal={}",
				task.getProcessInstanceId(), dice, pos, wouldEnterGoal, exactGoal);

		taskService.complete(task.getId(), Map.of(
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
