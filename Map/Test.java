package project3.Map;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {

        Graph graph = new Graph();
        graph.initializeGraph();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Entrez l'ID du sommet où se trouve la voiture non accidentée : ");
        int startNode = scanner.nextInt();

        System.out.println("Entrez l'ID du sommet où se trouve la voiture accidentée : ");
        int blockedNode = scanner.nextInt();

        if (!graph.nodeExists(startNode) || !graph.nodeExists(blockedNode)) {
            System.out.println("Erreur : un ou les deux sommets n'existent pas dans le graphe.");
        } else {
            graph.findAllPaths(startNode, blockedNode);
        }

        scanner.close();
    }
}
