package com.example.madn.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class PieceState implements Serializable {

    private List<PiecePosition> pieces;

    public PieceState() {
    }

    public PieceState(List<PiecePosition> pieces) {
        this.pieces = pieces;
    }

    public static PieceState allHome() {
        return new PieceState(Arrays.asList(
                new PiecePosition(PieceStatus.HOME, 0),
                new PiecePosition(PieceStatus.HOME, 0),
                new PiecePosition(PieceStatus.HOME, 0),
                new PiecePosition(PieceStatus.HOME, 0)
        ));
    }

    public List<PiecePosition> getPieces() {
        return pieces;
    }

    public void setPieces(List<PiecePosition> pieces) {
        this.pieces = pieces;
    }
}
