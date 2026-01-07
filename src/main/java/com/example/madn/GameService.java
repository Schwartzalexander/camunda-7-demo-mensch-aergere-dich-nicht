package com.example.madn;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

	public static final String PROCESS_KEY = "Game_MaDn_Multiplayer";
	public static final int BOARD_SIZE = 40; // main track fields
	public static final int GOAL_LENGTH = 4; // goal slots per player
	public static final int MAX_PLAYERS = 4;

	private final RuntimeService runtimeService;

	public GameService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public String startNewGame(int playerCount) {
		int count = Math.min(Math.max(playerCount, 2), MAX_PLAYERS);
		List<Map<String, Object>> players = buildPlayers(count);
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(
				PROCESS_KEY,
				Map.of(
						"players", players,
						"currentPlayerIndex", 0,
						"currentPlayerId", players.get(0).get("id"),
						"boardState", initBoardState(players)
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
		state.put("players", vars.getOrDefault("players", List.of()));
		state.put("currentPlayerId", vars.getOrDefault("currentPlayerId", null));
		state.put("currentPlayerIndex", vars.getOrDefault("currentPlayerIndex", 0));
		state.put("boardState", vars.getOrDefault("boardState", Map.of()));
		state.put("boardSize", BOARD_SIZE);
		state.put("goalLength", GOAL_LENGTH);
		state.put("dice1", vars.getOrDefault("dice1", null));
		state.put("dice2", vars.getOrDefault("dice2", null));
		state.put("isPasch", vars.getOrDefault("isPasch", null));
		state.put("chosenPieceId", vars.getOrDefault("chosenPieceId", null));
		state.put("moveWasPossible", vars.getOrDefault("moveWasPossible", null));
		state.put("kicked", vars.getOrDefault("kicked", null));
		state.put("kickedPlayerId", vars.getOrDefault("kickedPlayerId", null));
		state.put("kickedPieceId", vars.getOrDefault("kickedPieceId", null));
		state.put("winnerPlayerId", vars.getOrDefault("winnerPlayerId", null));
		return Optional.of(state);
	}

	private List<Map<String, Object>> buildPlayers(int playerCount) {
		List<Map<String, Object>> players = new ArrayList<>();
		String[] colors = new String[]{"red", "blue", "green", "yellow"};
		for (int i = 0; i < Math.min(playerCount, MAX_PLAYERS); i++) {
			players.add(Map.of(
					"id", "player" + (i + 1),
					"name", "Player " + (i + 1),
					"color", colors[i],
					"startIndex", (i * BOARD_SIZE / MAX_PLAYERS)
			));
		}
		return players;
	}

	private Map<String, Object> initBoardState(List<Map<String, Object>> players) {
		Map<String, Object> board = new HashMap<>();
		for (Map<String, Object> player : players) {
			board.put((String) player.get("id"), new ArrayList<>(List.of(
					new HashMap<>(Map.of("status", "HOME", "index", 0)),
					new HashMap<>(Map.of("status", "HOME", "index", 0)),
					new HashMap<>(Map.of("status", "HOME", "index", 0)),
					new HashMap<>(Map.of("status", "HOME", "index", 0))
			)));
		}
		return board;
	}
}
