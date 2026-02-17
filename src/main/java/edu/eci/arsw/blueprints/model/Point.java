package edu.eci.arsw.blueprints.model;

import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class Point {
    
    private int x;
    private int y;
    
    // Constructor sin argumentos para JPA
    public Point() {
    }
    
    // Constructor con parametros 
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    // Getters y setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    // Para mantener compatibilidad con record 
    public int x() { return x; }
    public int y() { return y; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;
        return x == point.x && y == point.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + ']';
    }
}