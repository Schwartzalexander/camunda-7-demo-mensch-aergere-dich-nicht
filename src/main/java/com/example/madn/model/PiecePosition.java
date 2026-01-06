package com.example.madn.model;

import java.io.Serializable;
import java.util.Objects;

public class PiecePosition implements Serializable {
    private PieceStatus status;
    private int index; // position on main track or in goal (0-3)

    public PiecePosition() {
    }

    public PiecePosition(PieceStatus status, int index) {
        this.status = status;
        this.index = index;
    }

    public PieceStatus getStatus() {
        return status;
    }

    public void setStatus(PieceStatus status) {
        this.status = status;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PiecePosition that = (PiecePosition) o;
        return index == that.index && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, index);
    }

    @Override
    public String toString() {
        return "PiecePosition{" +
                "status=" + status +
                ", index=" + index +
                '}';
    }
}
