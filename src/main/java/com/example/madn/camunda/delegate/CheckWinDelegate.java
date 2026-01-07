package com.example.madn.camunda.delegate;

import com.example.madn.model.PieceStatus;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CheckWinDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String playerId = Objects.toString(execution.getVariable("currentPlayerId"));
        Map<String, Object> boardState = (Map<String, Object>) execution.getVariable("boardState");
        List<Map<String, Object>> pieces = (List<Map<String, Object>>) boardState.get(playerId);
        boolean allGoal = pieces.stream().allMatch(p -> Objects.equals(p.get("status"), PieceStatus.GOAL.name()));
        if (allGoal) {
            execution.setVariable("winnerPlayerId", playerId);
        } else {
            execution.setVariable("winnerPlayerId", null);
        }
    }
}
