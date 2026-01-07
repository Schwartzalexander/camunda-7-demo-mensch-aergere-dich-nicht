package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Map;

public class SelectCurrentPlayerDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        List<Map<String, Object>> players = (List<Map<String, Object>>) execution.getVariable("players");
        int currentIndex = BoardLogic.toInt(execution.getVariable("currentPlayerIndex"), 0);
        if (players == null || players.isEmpty()) {
            return;
        }
        Map<String, Object> player = players.get(currentIndex % players.size());
        execution.setVariable("currentPlayerId", player.get("id"));
    }
}
