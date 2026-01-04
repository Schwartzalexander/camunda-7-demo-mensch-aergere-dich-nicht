package com.example.madn;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class WorkersConfig {

	private static final Logger log = LoggerFactory.getLogger(WorkersConfig.class);
	private static final Random rnd = new Random();

	@Bean
	public ExternalTaskClient externalTaskClient() {
		// Connect to the embedded engine's REST API
		return ExternalTaskClient.create()
				.baseUrl("http://localhost:8080/engine-rest")
				.asyncResponseTimeout(10_000)
				.build();
	}

	@Bean
	public Object subscriptions(ExternalTaskClient client) {
		client.subscribe("rollDiceStart")
				.lockDuration(5_000)
				.handler(rollDiceStartArea())
				.open();

		client.subscribe("enterBoard")
				.lockDuration(5_000)
				.handler(enterBoard())
				.open();

		client.subscribe("rollDiceNormal")
				.lockDuration(5_000)
				.handler(rollDiceNormal())
				.open();

		client.subscribe("moveNormally")
				.lockDuration(5_000)
				.handler(moveNormally())
				.open();

		client.subscribe("moveIntoGoal")
				.lockDuration(5_000)
				.handler(moveIntoGoal())
				.open();

		return new Object();
	}

	private ExternalTaskHandler rollDiceStartArea() {
		return (ExternalTask task, ExternalTaskService service) -> {
			int d1 = 1 + rnd.nextInt(6);
			int d2 = 1 + rnd.nextInt(6);
			boolean pasch = d1 == d2;

			log.info("[{}] Startbereich würfeln: {} + {} => Pasch={}",
					task.getProcessInstanceId(), d1, d2, pasch);

			service.complete(task, java.util.Map.of(
					"dice1", d1,
					"dice2", d2,
					"isPasch", pasch
			));
		};
	}

	private ExternalTaskHandler enterBoard() {
		return (ExternalTask task, ExternalTaskService service) -> {
			log.info("[{}] Pasch! Figur geht aufs Startfeld (position=1).", task.getProcessInstanceId());
			service.complete(task, java.util.Map.of(
					"inStartArea", false,
					"position", 1
			));
		};
	}

	private ExternalTaskHandler rollDiceNormal() {
		return (ExternalTask task, ExternalTaskService service) -> {
			int dice = 1 + rnd.nextInt(6);

			Integer pos = (Integer) task.getVariable("position");
			if (pos == null) pos = 1;

			boolean wouldEnterGoal = (pos + dice) >= GameService.GOAL_POS;
			boolean exactGoal = (pos + dice) == GameService.GOAL_POS;

			log.info("[{}] Würfeln: {} (position={}) wouldEnterGoal={} exactGoal={}",
					task.getProcessInstanceId(), dice, pos, wouldEnterGoal, exactGoal);

			service.complete(task, java.util.Map.of(
					"dice", dice,
					"wouldEnterGoal", wouldEnterGoal,
					"exactGoal", exactGoal
			));
		};
	}

	private ExternalTaskHandler moveNormally() {
		return (ExternalTask task, ExternalTaskService service) -> {
			Integer pos = (Integer) task.getVariable("position");
			Integer dice = (Integer) task.getVariable("dice");
			if (pos == null) pos = 1;
			if (dice == null) dice = 0;

			int newPos = pos + dice;
			log.info("[{}] Ziehen: {} -> {}", task.getProcessInstanceId(), pos, newPos);

			service.complete(task, java.util.Map.of("position", newPos));
		};
	}

	private ExternalTaskHandler moveIntoGoal() {
		return (ExternalTask task, ExternalTaskService service) -> {
			log.info("[{}] Exakt! Figur zieht ins Ziel. 🏁", task.getProcessInstanceId());
			service.complete(task, java.util.Map.of(
					"position", GameService.GOAL_POS,
					"inGoal", true
			));
		};
	}
}
