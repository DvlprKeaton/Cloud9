package com.example.pos;

public class ButtonData {
    private String name;
    private double price;

    public ButtonData(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
