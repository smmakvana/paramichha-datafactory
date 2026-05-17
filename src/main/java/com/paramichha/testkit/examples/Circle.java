package com.paramichha.testkit.examples;

import jakarta.validation.constraints.Positive;

public class Circle {
    int x;
    int y;
    @Positive
    float r;

    public Circle(int x, int y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public float area() {
        return (float) (Math.PI * r * r);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }
}
