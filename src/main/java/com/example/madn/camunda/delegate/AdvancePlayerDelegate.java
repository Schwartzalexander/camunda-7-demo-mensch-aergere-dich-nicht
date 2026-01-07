package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Map;

public class AdvancePlayerDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        List<Map<String, Object>> players = (List<Map<String, Object>>) execution.getVariable("players");
        int currentIndex = BoardLogic.toInt(execution.getVariable("currentPlayerIndex"), 0);
        boolean isPasch = Boolean.TRUE.equals(execution.getVariable("isPasch"));

        if (!isPasch) {
            currentIndex = (currentIndex + 1) % players.size();
            execution.setVariable("currentPlayerIndex", currentIndex);
        }
        execution.setVariable("currentPlayerId", players.get(currentIndex).get("id"));
        execution.setVariable("chosenPieceId", null);
        execution.setVariable("moveWasPossible", null);
        execution.setVariable("kicked", null);
        execution.setVariable("kickedPlayerId", null);
        execution.setVariable("kickedPieceId", null);
    }
}
