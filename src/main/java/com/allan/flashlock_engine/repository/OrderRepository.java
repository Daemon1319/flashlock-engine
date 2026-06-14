package com.allan.flashlock_engine.repository;

import org.springframework.stereotype.Repository;
import com.allan.flashlock_engine.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  
}
