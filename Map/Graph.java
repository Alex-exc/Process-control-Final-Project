package project3.Map;

import java.util.*;
import java.util.Map;

public class Graph {

    static Map<Integer, Node> nodes = new HashMap<>(); // Contains the vertices and their coordinates
    Map<Integer, List<Integer>> edges = new HashMap<>(); // Edges between the vertices


    public void addNode(int id, double x, double y) {
        nodes.put(id, new Node(id, x, y));
        edges.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(int from, int to) {
        edges.get(from).add(to);
        edges.get(to).add(from);
    }

    // BFS implementation with directions validations.
    public List<Integer> findShortestPathBetween(int startNode, int endNode) {
        Queue<List<Integer>> pathsQueue = new LinkedList<>();
        pathsQueue.add(Collections.singletonList(startNode));

        while (!pathsQueue.isEmpty()) {
            List<Integer> currentPath = pathsQueue.poll();
            int currentNode = currentPath.get(currentPath.size() - 1);

            // If the path goes to the correct end point
            if (currentNode == endNode) {
                List<String> validatedDirections = validatePath(currentPath);
                if (validatedDirections.size() == currentPath.size() - 1) {
                    return currentPath; // Return the valid path
                } else {
                    System.out.println("Invalid path detected: " + currentPath);
                }
            }

            // Add the adjacent nodes that hasn't been visited
            for (int neighbor : edges.get(currentNode)) {
                if (!currentPath.contains(neighbor)) {
                    List<Integer> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    pathsQueue.add(newPath);
                }
            }
        }
        System.out.println("No valid path found after exploring all options.");
        return Collections.emptyList();
    }

    public static String getDirection(Node currentNode, Node nextNode){
        String direction = "";
        if(currentNode.getX() == nextNode.getX() && currentNode.getY() > nextNode.getY()){
            direction = "S"; // Going from (0,0) to (0,-1)
            return direction;
        }
        if(currentNode.getX() > nextNode.getX() && currentNode.getY() > nextNode.getY()){
            direction = "SW"; // Going from (0.5,0.5) to (0,0)
            return direction;
        }
        if(currentNode.getX() > nextNode.getX() && currentNode.getY() == nextNode.getY()){
            direction = "W"; // Going from (0,0) to (-1,0)
            return direction;
        }
        if(currentNode.getX() < nextNode.getX() && currentNode.getY() > nextNode.getY()){
            direction = "SE"; // Going from (0.5,0.5) to (1,0)
            return direction;
        }
        if(currentNode.getX() == nextNode.getX() && currentNode.getY() < nextNode.getY()){
            direction = "N"; // Going from (0,0) to (0,1)
            return direction;
        }
        if(currentNode.getX() < nextNode.getX() && currentNode.getY() < nextNode.getY()){
            direction = "NE"; // Going from (0.5,0.5) to (1,1)
            return direction;
        }
        if(currentNode.getX() < nextNode.getX() && currentNode.getY() == nextNode.getY()){
            direction = "E"; // Going from (0,0) to (1,0)
            return direction;
        }
        if(currentNode.getX() > nextNode.getX() && currentNode.getY() < nextNode.getY()){
            direction = "NW"; // Going from (0.5,0.5) to (0,1)
            return direction;
        }
        return direction;
    }
    private static List<Node> getNodes(List<Integer> path) {
        List<Node> nodeList = new ArrayList<>();
        for (int id : path) {
            Node node = nodes.get(id); // Get the node id
            if (node != null) {
                nodeList.add(node); // Add the node in the list
            } else {
                System.out.println("Node with ID " + id + " does not exist.");
            }
        }
        return nodeList;
    }

    private List<String> validatePath(List<Integer> path) {
        List<Node> nodes = getNodes(path);
        List<String> directions = new ArrayList<>();

        for (int i = 0; i < nodes.size() - 1; i++) {
            Node currentNode = nodes.get(i);
            Node nextNode = nodes.get(i + 1);
            directions.add(getDirection(currentNode, nextNode));
        }

        for (int i = 0; i < directions.size() - 1; i++) {
            String currentDirection = directions.get(i);
            String nextDirection = directions.get(i + 1);

            if (
                    (
                            currentDirection.equals("N") && (
                                    nextDirection.equals("S") || nextDirection.equals("SW") || nextDirection.equals("SE")
                        )
                    ) || (
                            currentDirection.equals("S") && (
                                    nextDirection.equals("N") || nextDirection.equals("NW") || nextDirection.equals("NE")
                            )
                    ) || (
                            currentDirection.equals("E") && (
                                    nextDirection.equals("W") || nextDirection.equals("NW") || nextDirection.equals("SW")
                            )
                    ) || (
                            currentDirection.equals("W") && (
                                    nextDirection.equals("E") || nextDirection.equals("NE") || nextDirection.equals("SE")
                            )
                    ) || (
                            currentDirection.equals("SW") && (
                                    nextDirection.equals("E") || nextDirection.equals("NE") || nextDirection.equals("N") ||
                                            nextDirection.equals("NW") || nextDirection.equals("SE")
                        )
                    ) || (
                            currentDirection.equals("SE") && (
                                    nextDirection.equals("W") || nextDirection.equals("NW") || nextDirection.equals("N") ||
                                            nextDirection.equals("NE") || nextDirection.equals("SW")
                            )
                    ) || (
                            currentDirection.equals("NE") && (
                                    nextDirection.equals("S") || nextDirection.equals("SW") || nextDirection.equals("W") ||
                                            nextDirection.equals("NW") || nextDirection.equals("SE")
                            )
                    ) || (
                            currentDirection.equals("NW") && (
                                    nextDirection.equals("S") || nextDirection.equals("SE") || nextDirection.equals("E") ||
                                            nextDirection.equals("NE") || nextDirection.equals("SW")
                            )
                        )
                ){
                // Cut the invalid directions
                return directions.subList(0, i + 1);
            }
        }

        return directions; // Valid directions
    }

    public void initializeGraph() {
        // If id start with 1, then consider it as zone 1 as a set
        addNode(1, 0, 1);
        addNode(101, 1.1, 0.9);
        addNode(102, 1.1, 1.1);
        // Same logic
        addNode(9, 0, 2);
        addNode(901, 0.1, 1.9);
        addNode(902, 0.1, 2.1);

        addNode(18, 0, 3);
        addNode(1801, 0.1, 2.9);
        addNode(1802, 0.1, 3.1);

        // 2 (4,1)
        addNode(2, 4, 1);
        addNode(201, 3.9, 0.9);
        addNode(202, 3.9,1.1);

        // 12 (4,2)
        addNode(12, 4, 2);
        addNode(1201,3.9,1.9);
        addNode(1202, 3.9, 2.1);

        //19 (4,3)
        addNode(19, 4, 3);
        addNode(1901, 3.9, 2.9);
        addNode(1902, 3.9, 3.1);


        addNode(3, 2, 4);
        addNode(301,1.9,3.9);
        addNode(302,2.1,3.9);

        addNode(4, 2, 0);
        addNode(401, 1.9, 0.1);
        addNode(402, 2.1, 0.1);

        addNode(5, 2, 1);
        addNode(501,1.9, 0.9);
        addNode(502, 2.1, 0.9);

        addNode(6, 2, 2);
        addNode(601,1.9, 2.1);
        addNode(602, 2.1, 2.1);

        addNode(7, 1, 1);
        addNode(8, 3, 1);

        addNode(10, 3, 2);
        addNode(11, 1, 2);

        addNode(13, 0, 0);
        addNode(14, 0, 4);
        addNode(15, 4, 4);
        addNode(16, 4, 0);

        // Node not really useful here
        addNode(17, 2, 3);

        addNode(20, 1, 0);
        addNode(21, 3, 0);
        addNode(22, 1, 3);
        addNode(23, 3, 3);
        addNode(24, 1, 4);
        addNode(25, 3, 4);

        // Left side
        addEdge(13,1);
        addEdge(1,9);
        addEdge(9,18);
        addEdge(18,14);

        // Bottom
        addEdge(14,24);
        addEdge(24,3);
        addEdge(3,25);
        addEdge(25,15);

        // Right side
        addEdge(15,19);
        addEdge(19,12);
        addEdge(12,2);
        addEdge(2,16);

        // Top
        addEdge(13, 20);
        addEdge(20,4);
        addEdge(4,21);
        addEdge(21,16);

        // Intersection 1
        addEdge(13,101);
        addEdge(101,7);
        addEdge(7,102);

        // Intersection 9
        addEdge(102,901);
        addEdge(901, 11);
        addEdge(11, 902);

        // Intersection 18
        addEdge(902, 1801);
        addEdge(1801, 22);
        addEdge(22,1802);
        addEdge(1802, 14);

        // Intersection 17

            // Intersection 3-6
            addEdge(24,301);
            addEdge(301,601);
            addEdge(601,11);
            addEdge(11,6);
            addEdge(6,10);
            addEdge(10,602);
            addEdge(602,302);
            addEdge(302,25);

            // Edge 22-23
            addEdge(22,23);

        // Intersection 19
        addEdge(15,1902);
        addEdge(1902,23);
        addEdge(23,1901);

        // Intersection 12
        addEdge(1901,1202);
        addEdge(1202,10);
        addEdge(10,1201);

        // Intersection 2
        addEdge(1201,202);
        addEdge(202,8);
        addEdge(8,201);
        addEdge(201,16);

        // Intersection 5
        addEdge(7,501);
        addEdge(501,401);
        addEdge(8,502);
        addEdge(502,402);
        addEdge(5,8);
        addEdge(7,5);

        // Intersection 4
        addEdge(20,401);
        addEdge(21, 402);

    }


    public static void main(String[] args) {

        // TEST

        Graph graph = new Graph();
        graph.initializeGraph();

        // Find the path
        List<Integer> validPath = graph.findShortestPathBetween(14, 5); // Change the input to test
        // 402 to 601 is a good example
        // 13 to 23 too

        if (!validPath.isEmpty()) {
            System.out.println("Valid path: " + validPath);

            // Prints the nodes and the directions
            List<Node> nodes = getNodes(validPath);
            for (Node node : nodes) {
                System.out.println("Node ID: " + node.getID() + ", Coordinates: (" + node.getX() + ", " + node.getY() + ")");
            }

            // Compute and return the directions
            List<String> directions = new ArrayList<>();
            for (int i = 0; i < nodes.size() - 1; i++) {
                directions.add(getDirection(nodes.get(i), nodes.get(i + 1)));
            }
            System.out.println("Directions: " + directions);
        } else {
            System.out.println("No valid path could be found.");
        }
    }

}

