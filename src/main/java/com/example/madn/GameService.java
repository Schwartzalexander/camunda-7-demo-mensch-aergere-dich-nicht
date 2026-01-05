package com.example.madn;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class GameService {

	public static final String PROCESS_KEY = "Process_MaDn_Solo_OneFigure";
	public static final int GOAL_POS = 10; // simple straight line board: 1..10

	private final RuntimeService runtimeService;

	public GameService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public String startNewGame() {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(
				PROCESS_KEY,
				Map.of(
						"inStartArea", true,
						"inGoal", false,
						"position", 0
				)
		);
		return pi.getId();
	}

	public Optional<String> findAnyRunningInstanceId() {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
				.processDefinitionKey(PROCESS_KEY)
				.active()
				.orderByProcessInstanceId()
				.desc()
				.listPage(0, 1)
				.stream()
				.findFirst()
				.orElse(null);
		return Optional.ofNullable(pi).map(ProcessInstance::getId);
	}

	public Optional<Map<String, Object>> getState(String processInstanceId) {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.singleResult();
		if (pi == null) {
			return Optional.empty();
		}

		var vars = runtimeService.getVariables(processInstanceId);

		Map<String, Object> state = new HashMap<>();
		state.put("processInstanceId", processInstanceId);
		state.put("inStartArea", vars.getOrDefault("inStartArea", true));
		state.put("attempts", vars.getOrDefault("attempts", 0));
		state.put("position", vars.getOrDefault("position", 0));
		state.put("lastDice1", vars.getOrDefault("dice1", null));
		state.put("lastDice2", vars.getOrDefault("dice2", null));
		state.put("lastDice", vars.getOrDefault("dice", null));
		state.put("isPasch", vars.getOrDefault("isPasch", null));
		state.put("wouldEnterGoal", vars.getOrDefault("wouldEnterGoal", null));
		state.put("exactGoal", vars.getOrDefault("exactGoal", null));
		state.put("inGoal", vars.getOrDefault("inGoal", false));
		return Optional.of(state);
	}
}
