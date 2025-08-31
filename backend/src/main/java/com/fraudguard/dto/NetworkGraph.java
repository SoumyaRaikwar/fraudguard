
package com.fraudguard.dto;

import java.util.List;

public record NetworkGraph(List<Node> nodes, List<Edge> edges) {
    public record Node(String id, String label, String type) {}
    public record Edge(String from, String to, double weight) {}
}
