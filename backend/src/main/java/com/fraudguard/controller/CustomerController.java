
package com.fraudguard.controller;

import com.fraudguard.model.Customer;
import com.fraudguard.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository repo;

    public CustomerController(CustomerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Customer> all() { return repo.findAll(); }

    @PostMapping
    public Customer create(@RequestBody Customer c) { return repo.save(c); }
}
