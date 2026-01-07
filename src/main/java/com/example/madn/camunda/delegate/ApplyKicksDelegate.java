package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import com.example.madn.model.MoveResult;
import com.example.madn.model.PiecePosition;
import com.example.madn.model.PieceStatus;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplyKicksDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        Map<String, Object> boardState = (Map<String, Object>) execution.getVariable("boardState");
        List<Map<String, Object>> players = (List<Map<String, Object>>) execution.getVariable("players");

        MoveResult result = new MoveResult();
        result.setMoveWasPossible(Boolean.TRUE.equals(execution.getVariable("moveWasPossible")));
        Object moved = execution.getVariable("movedPiecePos");
        PiecePosition movedPos = BoardLogic.toPiecePosition((Map<String, Object>) moved);
        result.setMovedPosition(movedPos);

        if (!result.isMoveWasPossible() || movedPos.getStatus() != PieceStatus.BOARD) {
            execution.setVariable("kicked", false);
            return;
        }

        String playerId = Objects.toString(execution.getVariable("currentPlayerId"));
        int pieceId = BoardLogic.toInt(execution.getVariable("chosenPieceId"), 0);

        BoardLogic.applyKick(boardState, players, playerId, pieceId, result);
        execution.setVariable("boardState", boardState);
        execution.setVariable("kicked", result.isKicked());
        execution.setVariable("kickedPlayerId", result.getKickedPlayerId());
        execution.setVariable("kickedPieceId", result.getKickedPieceId());
    }
}
