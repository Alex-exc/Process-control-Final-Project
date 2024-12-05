package project3.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Represents a vertex
class Node {
    int id;
    double x; // Coordinate x
    double y; // Coordinate y

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getID(){
        return id;
    }
}

