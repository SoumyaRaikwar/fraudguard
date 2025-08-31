
package com.fraudguard.controller;

import com.fraudguard.dto.NetworkGraph;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    @GetMapping("/analysis")
    public NetworkGraph analysis() {
        var nodes = List.of(
                new NetworkGraph.Node("C1","Global Imports Inc.","COMPANY"),
                new NetworkGraph.Node("P1","John D. Smith","PERSON"),
                new NetworkGraph.Node("EX1","Crypto Exchange","EXCHANGE")
        );
        var edges = List.of(
                new NetworkGraph.Edge("C1","EX1", 47800.0),
                new NetworkGraph.Edge("P1","EX1", 18450.0)
        );
        return new NetworkGraph(nodes, edges);
    }
}
