package com.example.madn.model;

import java.io.Serializable;

public class MoveResult implements Serializable {
    private boolean moveWasPossible;
    private PiecePosition movedPosition;
    private boolean kicked;
    private String kickedPlayerId;
    private Integer kickedPieceId;

    public boolean isMoveWasPossible() {
        return moveWasPossible;
    }

    public void setMoveWasPossible(boolean moveWasPossible) {
        this.moveWasPossible = moveWasPossible;
    }

    public PiecePosition getMovedPosition() {
        return movedPosition;
    }

    public void setMovedPosition(PiecePosition movedPosition) {
        this.movedPosition = movedPosition;
    }

    public boolean isKicked() {
        return kicked;
    }

    public void setKicked(boolean kicked) {
        this.kicked = kicked;
    }

    public String getKickedPlayerId() {
        return kickedPlayerId;
    }

    public void setKickedPlayerId(String kickedPlayerId) {
        this.kickedPlayerId = kickedPlayerId;
    }

    public Integer getKickedPieceId() {
        return kickedPieceId;
    }

    public void setKickedPieceId(Integer kickedPieceId) {
        this.kickedPieceId = kickedPieceId;
    }
}
