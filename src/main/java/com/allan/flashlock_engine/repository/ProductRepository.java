package com.allan.flashlock_engine.repository;

import org.springframework.stereotype.Repository;
import com.allan.flashlock_engine.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.id = :id")
  Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);

  @Query(value = "SELECT stock FROM products WHERE id = :id", nativeQuery = true)
  Optional<Integer> findStockByIdNative(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Query(value = "UPDATE products SET stock = :stock WHERE id = :id", nativeQuery = true)
  void updateStockByIdNative(@Param("id") Long id, @Param("stock") int stock);
}
