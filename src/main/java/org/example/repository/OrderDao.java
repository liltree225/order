package org.example.repository;

import org.example.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDao extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(Long userId);
}
