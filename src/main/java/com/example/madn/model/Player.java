package com.example.madn.model;

import java.io.Serializable;

public class Player implements Serializable {
    private String id;
    private String name;
    private String color;
    private int startIndex; // position on main track where this player enters

    public Player() {
    }

    public Player(String id, String name, String color, int startIndex) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.startIndex = startIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
