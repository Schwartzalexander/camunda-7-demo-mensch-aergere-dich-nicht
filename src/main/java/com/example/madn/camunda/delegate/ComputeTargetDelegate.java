package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import com.example.madn.model.MoveResult;
import com.example.madn.model.PiecePosition;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComputeTargetDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ComputeTargetDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String playerId = Objects.toString(execution.getVariable("currentPlayerId"));
        int pieceId = BoardLogic.toInt(execution.getVariable("chosenPieceId"), 0);
        int dice = BoardLogic.toInt(execution.getVariable("dice"), 0);

        Map<String, Object> boardState = (Map<String, Object>) execution.getVariable("boardState");
        List<Map<String, Object>> pieces = (List<Map<String, Object>>) boardState.get(playerId);

        MoveResult result = BoardLogic.movePiece(pieces.get(pieceId), dice);
        if (result.isMoveWasPossible()) {
            pieces.set(pieceId, BoardLogic.toMap(result.getMovedPosition()));
            execution.setVariable("boardState", boardState);
        }

        execution.setVariable("moveWasPossible", result.isMoveWasPossible());
        execution.setVariable("movedPiecePos", BoardLogic.toMap(result.getMovedPosition()));
        log.info("[{}] {} moved piece {} with dice {} -> {}", execution.getProcessInstanceId(), playerId, pieceId, dice, result.getMovedPosition());
    }
}
