package project3.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Graph {
    Map<Integer, Node> nodes = new HashMap<>(); // Contient les sommets avec leurs coordonnées
    Map<Integer, List<Integer>> edges = new HashMap<>(); // Connexions entre sommets

    public void addNode(int id, double x, double y) {
        nodes.put(id, new Node(id, x, y));
        edges.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(int from, int to) {
        edges.get(from).add(to);
        edges.get(to).add(from); // Pour un graphe non orienté
    }

        public static void main(String[] args) {
            Graph graph = new Graph();

            // Ajout des sommets avec des coordonnées arbitraires
            graph.addNode(1, 0, 1);
            graph.addNode(2, 4, 1);
            graph.addNode(3, 2, 4);
            graph.addNode(4, 2, 0);
            graph.addNode(5, 2, 1);
            graph.addNode(6, 2, 2);
            graph.addNode(7, 1, 1);
            graph.addNode(8, 3, 1);
            graph.addNode(9, 0, 2);
            graph.addNode(10, 3, 2);
            graph.addNode(11, 1, 2);
            graph.addNode(12, 4, 2);
            graph.addNode(13, 0, 0);
            graph.addNode(14, 0, 4);
            graph.addNode(15, 4, 4);
            graph.addNode(16, 4, 0);
            graph.addNode(17, 2, 3);
            graph.addNode(18, 0, 3);
            graph.addNode(19, 4, 3);
            graph.addNode(20, 1, 0);
            graph.addNode(21, 3, 0);
            graph.addNode(22, 1, 3);
            graph.addNode(23, 3, 3);
            graph.addNode(24, 1, 4);
            graph.addNode(25, 3, 4);

            // Add Edges
            graph.addEdge(13,20);
            graph.addEdge(20,4);
            graph.addEdge(4,21);
            graph.addEdge(21,16);
            graph.addEdge(16,2);
            graph.addEdge(2,8);
            graph.addEdge(8,5);
            graph.addEdge(5,4);
            graph.addEdge(5,7);
            graph.addEdge(7,1);
            graph.addEdge(1,13);
            graph.addEdge(1,9);
            graph.addEdge(9,11);
            graph.addEdge(11,6);
            graph.addEdge(6,10);
            graph.addEdge(10,12);
            graph.addEdge(12,2);
            graph.addEdge(12,19);
            graph.addEdge(19,23);
            graph.addEdge(23,17);
            graph.addEdge(17,6);
            graph.addEdge(17,22);
            graph.addEdge(22,18);
            graph.addEdge(18,9);
            graph.addEdge(18,14);
            graph.addEdge(14,24);
            graph.addEdge(24,3);
            graph.addEdge(3,17);
            graph.addEdge(3,25);
            graph.addEdge(25,15);
            graph.addEdge(15,19);
            // End of the graph

        }
    }

