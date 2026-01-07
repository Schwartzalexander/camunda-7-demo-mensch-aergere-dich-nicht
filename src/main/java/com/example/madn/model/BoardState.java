package com.example.madn.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BoardState implements Serializable {
    private Map<String, PieceState> playerPieces;

    public BoardState() {
    }

    public BoardState(Map<String, PieceState> playerPieces) {
        this.playerPieces = playerPieces;
    }

    public Map<String, PieceState> getPlayerPieces() {
        return playerPieces;
    }

    public void setPlayerPieces(Map<String, PieceState> playerPieces) {
        this.playerPieces = playerPieces;
    }

    public List<PiecePosition> getPiecesFor(String playerId) {
        return playerPieces.get(playerId).getPieces();
    }
}
