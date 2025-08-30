
package com.fraudguard.repository;

import com.fraudguard.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> { }
