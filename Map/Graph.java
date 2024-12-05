package project3.Map;

import java.util.*;
import java.util.Map;

public class Graph {
    static Map<Integer, Node> nodes = new HashMap<>(); // Contains the vertices and their coordinates
    Map<Integer, List<Integer>> edges = new HashMap<>(); // Edges between the vertices

    public void addNode(int id, double x, double y) {
        nodes.put(id, new Node(id, x, y));
        System.out.println(id + ": " + x + ", " + y);
        edges.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(int from, int to) {
        edges.get(from).add(to);
        edges.get(to).add(from);
    }

    public Map<Integer, List<Integer>> getEdges() {
        return edges;
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public double getNodeX() {
        double x = 0;
        for (Node node : nodes.values()) {
            x = node.getX();
        }
        return x;
    }

    public void findAllPaths(int startNode, int blockedNode) {
        Set<Integer> visited = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        List<List<String>> allPaths = new ArrayList<>();

        findPathsDFS(startNode, blockedNode, visited, currentPath, allPaths);

        int pathCount = 1;
        for (List<String> path : allPaths) {
            System.out.println("Possible paths : " + pathCount++);
            for (String edge : path) {
                System.out.println(edge + " is the next way");
            }
            System.out.println();
        }
    }

    private void findPathsDFS(int current, int blockedNode, Set<Integer> visited, List<String> currentPath, List<List<String>> allPaths) {
        if (current == blockedNode) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }

        visited.add(current);

        for (int neighbor : edges.get(current)) {
            if (!visited.contains(neighbor)) {
                String edge = formatEdge(current, neighbor);
                currentPath.add(edge);
                findPathsDFS(neighbor, blockedNode, visited, currentPath, allPaths);
                currentPath.remove(currentPath.size() - 1); // backtrack
            }
        }

        visited.remove(current);
    }

    private String formatEdge(int from, int to) {
        return from < to ? from + "-" + to : to + "-" + from;
    }

    public boolean nodeExists(int id) {
        return nodes.containsKey(id);
    }

    public void initializeGraph() {
        addNode(1, 0, 1);
        addNode(2, 4, 1);
        addNode(3, 2, 4);
        addNode(4, 2, 0);
        addNode(5, 2, 1);
        addNode(6, 2, 2);
        addNode(7, 1, 1);
        addNode(8, 3, 1);
        addNode(9, 0, 2);
        addNode(10, 3, 2);
        addNode(11, 1, 2);
        addNode(12, 4, 2);
        addNode(13, 0, 0);
        addNode(14, 0, 4);
        addNode(15, 4, 4);
        addNode(16, 4, 0);
        addNode(17, 2, 3);
        addNode(18, 0, 3);
        addNode(19, 4, 3);
        addNode(20, 1, 0);
        addNode(21, 3, 0);
        addNode(22, 1, 3);
        addNode(23, 3, 3);
        addNode(24, 1, 4);
        addNode(25, 3, 4);

        addEdge(13, 20);
        addEdge(20, 4);
        addEdge(4, 21);
        addEdge(21, 16);
        addEdge(16, 2);
        addEdge(2, 8);
        addEdge(8, 5);
        addEdge(5, 4);
        addEdge(5, 7);
        addEdge(7, 1);
        addEdge(1, 13);
        addEdge(1, 9);
        addEdge(9, 11);
        addEdge(11, 6);
        addEdge(6, 10);
        addEdge(10, 12);
        addEdge(12, 2);
        addEdge(12, 19);
        addEdge(19, 23);
        addEdge(23, 17);
        addEdge(17, 6);
        addEdge(17, 22);
        addEdge(22, 18);
        addEdge(18, 9);
        addEdge(18, 14);
        addEdge(14, 24);
        addEdge(24, 3);
        addEdge(3, 17);
        addEdge(3, 25);
        addEdge(25, 15);
        addEdge(15, 19);
    }
}

