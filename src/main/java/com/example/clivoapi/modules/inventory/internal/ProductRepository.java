package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByNameAsc();
}
