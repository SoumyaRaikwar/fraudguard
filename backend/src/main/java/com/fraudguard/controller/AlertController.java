
package com.fraudguard.controller;

import com.fraudguard.model.Alert;
import com.fraudguard.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public List<Alert> all() { return service.findAll(); }

    @PostMapping
    public Alert create(@RequestBody Alert alert) { return service.save(alert); }
}
