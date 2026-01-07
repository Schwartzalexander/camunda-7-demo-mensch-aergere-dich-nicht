package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import com.example.madn.model.PieceStatus;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnterBoardDelegate implements JavaDelegate {

	private static final Logger log = LoggerFactory.getLogger(EnterBoardDelegate.class);

	@Override
	public void execute(DelegateExecution execution) {
		String playerId = Objects.toString(execution.getVariable("currentPlayerId"));
		int pieceId = BoardLogic.toInt(execution.getVariable("chosenPieceId"), 0);

		Map<String, Object> boardState = (Map<String, Object>) execution.getVariable("boardState");
		List<Map<String, Object>> players = (List<Map<String, Object>>) execution.getVariable("players");
		List<Map<String, Object>> pieces = (List<Map<String, Object>>) boardState.get(playerId);
		pieces.set(pieceId, Map.of("status", PieceStatus.BOARD.name(), "index", 0));

		Map<String, Object> updated = Map.of("status", PieceStatus.BOARD.name(), "index", 0);
		execution.setVariable("boardState", boardState);
		execution.setVariable("movedPiecePos", updated);
		execution.setVariable("moveWasPossible", true);
		execution.setVariable("kicked", false);

		log.info("[{}] {} betritt das Brett mit Figur {} -> {}", execution.getProcessInstanceId(), playerId, pieceId, updated);
	}
}
