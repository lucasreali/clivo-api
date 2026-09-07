package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.Product;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByOrderByNameAsc();
}
