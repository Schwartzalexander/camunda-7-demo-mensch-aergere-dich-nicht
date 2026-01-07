package com.example.madn;

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

	public UserTaskRollService(TaskService taskService) {
		this.taskService = taskService;
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
		return findTask(processInstanceId, "Task_RollDice");
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
			case "Task_RollDice" -> handleRollDice(task);
			default -> log.warn("Unbekannter Dice-Task {}", task.getTaskDefinitionKey());
		}
	}

	private void handleRollDice(Task task) {
		int dice1 = 1 + rnd.nextInt(6);
		int dice2 = 1 + rnd.nextInt(6);
		boolean pasch = dice1 == dice2;

		log.info("[{}] Würfeln: {} und {} (Pasch={})", task.getProcessInstanceId(), dice1, dice2, pasch);

		taskService.complete(task.getId(), Map.of(
				"dice1", dice1,
				"dice2", dice2,
				"isPasch", pasch
		));
	}

	public boolean choosePiece(String processInstanceId, int pieceId) {
		var chooseTask = findTask(processInstanceId, "Task_ChoosePiece");
		if (chooseTask.isEmpty()) {
			log.info("[{}] Kein Auswahl-Task gefunden – wahrscheinlich wird noch gewürfelt.", processInstanceId);
			return false;
		}
		taskService.complete(chooseTask.get().getId(), Map.of("chosenPieceId", pieceId));
		return true;
	}
}
