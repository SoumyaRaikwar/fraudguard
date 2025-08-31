
package com.fraudguard.service;

import com.fraudguard.model.Alert;
import com.fraudguard.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {
    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    public List<Alert> findAll() { return repository.findAll(); }
    public Alert save(Alert a) { return repository.save(a); }
}
