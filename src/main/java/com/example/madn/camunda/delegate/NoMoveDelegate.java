package com.example.madn.camunda.delegate;

import com.example.madn.logic.BoardLogic;
import com.example.madn.model.PiecePosition;
import com.example.madn.model.PieceStatus;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class NoMoveDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("moveWasPossible", false);
        PiecePosition pos = new PiecePosition(PieceStatus.valueOf(String.valueOf(execution.getVariable("pieceStatus"))), BoardLogic.toInt(execution.getVariable("pieceSteps"), 0));
        execution.setVariable("movedPiecePos", BoardLogic.toMap(pos));
    }
}
