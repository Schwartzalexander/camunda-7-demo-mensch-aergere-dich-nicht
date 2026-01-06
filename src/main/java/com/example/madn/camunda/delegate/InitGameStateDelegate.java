package com.example.madn.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class InitGameStateDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("winnerPlayerId", null);
        execution.setVariable("moveWasPossible", null);
        execution.setVariable("kicked", null);
        execution.setVariable("kickedPlayerId", null);
        execution.setVariable("kickedPieceId", null);
        execution.setVariable("chosenPieceId", null);
        execution.setVariable("movedPiecePos", null);
    }
}
