package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import com.example.madn.model.PiecePosition;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoadPieceStateDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String playerId = Objects.toString(execution.getVariable("currentPlayerId"));
        int pieceId = BoardLogic.toInt(execution.getVariable("chosenPieceId"), 0);
        Map<String, Object> boardState = (Map<String, Object>) execution.getVariable("boardState");
        List<Map<String, Object>> pieces = (List<Map<String, Object>>) boardState.get(playerId);
        PiecePosition pos = BoardLogic.toPiecePosition(pieces.get(pieceId));

        execution.setVariable("pieceStatus", pos.getStatus().name());
        execution.setVariable("pieceSteps", pos.getIndex());
    }
}
