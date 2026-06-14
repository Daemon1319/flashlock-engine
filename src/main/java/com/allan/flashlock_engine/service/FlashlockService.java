package com.allan.flashlock_engine.service;

import com.allan.flashlock_engine.dto.OrderDto;
import com.allan.flashlock_engine.dto.ProductDto;
import com.allan.flashlock_engine.dto.PurchaseResponse;
import com.allan.flashlock_engine.entity.Order;
import com.allan.flashlock_engine.entity.Product;
import com.allan.flashlock_engine.exception.OutOfStockException;
import com.allan.flashlock_engine.exception.ProductNotFoundException;
import com.allan.flashlock_engine.repository.OrderRepository;
import com.allan.flashlock_engine.repository.ProductRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class FlashlockService {

  public enum LockMode {
    VULNERABLE,
    OPTIMISTIC,
    PESSIMISTIC
  }

  private static final int MAX_OPTIMISTIC_RETRIES = 5;

  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final TransactionTemplate txTemplate;

  private final AtomicReference<LockMode> lockMode = new AtomicReference<>(LockMode.PESSIMISTIC);

  public FlashlockService(ProductRepository productRepository, OrderRepository orderRepository,
      TransactionTemplate txTemplate) {
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
    this.txTemplate = txTemplate;
  }

  public LockMode cycleLockMode() {
    return lockMode.updateAndGet(current -> switch (current) {
      case PESSIMISTIC -> LockMode.OPTIMISTIC;
      case OPTIMISTIC -> LockMode.VULNERABLE;
      case VULNERABLE -> LockMode.PESSIMISTIC;
    });
  }

  public LockMode getLockMode() {
    return lockMode.get();
  }

  // 1. THE CORE ENGINE — each process method manages its own transactions
  // via TransactionTemplate so optimistic retries get fresh transactions.
  public PurchaseResponse buyProduct(Long productId, String username) {
    return switch (lockMode.get()) {
      case PESSIMISTIC -> processPessimistic(productId, username);
      case OPTIMISTIC -> processOptimistic(productId, username);
      case VULNERABLE -> processVulnerable(productId, username);
    };
  }

  private PurchaseResponse processPessimistic(Long productId, String username) {
    return txTemplate.execute(status -> {
      Product product = productRepository.findByIdWithPessimisticLock(productId)
          .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: " + productId));

      if (product.getStock() <= 0) {
        throw new OutOfStockException("SOLD OUT: No stock remaining");
      }

      product.setStock(product.getStock() - 1);
      productRepository.save(product);
      orderRepository.save(new Order(product, username));

      return new PurchaseResponse("SUCCESS", username + " secured the item!",
          lockMode.get().name(), product.getStock(), 1);
    });
  }

  private PurchaseResponse processOptimistic(Long productId, String username) {
    for (int attempt = 1; attempt <= MAX_OPTIMISTIC_RETRIES; attempt++) {
      try {
        final int currentAttempt = attempt;
        return txTemplate.execute(status -> {
          Product product = productRepository.findById(productId)
              .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: " + productId));

          if (product.getStock() <= 0) {
            throw new OutOfStockException("SOLD OUT: No stock remaining");
          }

          product.setStock(product.getStock() - 1);
          productRepository.saveAndFlush(product);
          orderRepository.save(new Order(product, username));

          return new PurchaseResponse("SUCCESS",
              username + " secured the item! (attempt " + currentAttempt + "/" + MAX_OPTIMISTIC_RETRIES + ")",
              lockMode.get().name(), product.getStock(), currentAttempt);
        });
      } catch (ObjectOptimisticLockingFailureException e) {
        if (attempt >= MAX_OPTIMISTIC_RETRIES) {
          throw e; // exhausted all retries — let GlobalExceptionHandler handle it
        }
        // version conflict detected — retry with a fresh transaction
      }
    }
    throw new RuntimeException("Unexpected: optimistic retry loop exited without result");
  }

  private PurchaseResponse processVulnerable(Long productId, String username) {
    return txTemplate.execute(status -> {
      Integer stock = productRepository.findStockByIdNative(productId)
          .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: " + productId));

      if (stock <= 0) {
        throw new OutOfStockException("SOLD OUT: No stock remaining");
      }

      productRepository.updateStockByIdNative(productId, stock - 1);

      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: " + productId));
      orderRepository.save(new Order(product, username));

      return new PurchaseResponse("SUCCESS", username + " secured the item!",
          lockMode.get().name(), stock - 1, 1);
    });
  }

  @Transactional
  public ProductDto setupDatabase() {
    List<Product> existingProducts = productRepository.findAll();
    Product product;

    if (existingProducts.isEmpty()) {
      product = new Product("Leobog HI75C Mechanical Keyboard", new BigDecimal("99.99"), 10);
    } else {
      product = existingProducts.get(0);
      product.setName("Leobog HI75C Mechanical Keyboard");
      product.setPrice(new BigDecimal("99.99"));
      product.setStock(10);
    }

    Product saved = productRepository.save(product);
    return mapToDto(saved);
  }

  @Transactional
  public void clearDatabase() {
    orderRepository.deleteAll();
    List<Product> products = productRepository.findAll();
    if (!products.isEmpty()) {
      Product product = products.get(0);
      product.setStock(0);
      productRepository.save(product);
    }
  }

  public List<OrderDto> getAllOrders() {
    return orderRepository.findAll().stream()
        .map(order -> new OrderDto(order.getId(), order.getProductId(), order.getUsername(), order.getCreatedAt()))
        .collect(Collectors.toList());
  }

  public ProductDto getProduct() {
    List<Product> products = productRepository.findAll();
    return products.isEmpty() ? null : mapToDto(products.get(0));
  }

  private ProductDto mapToDto(Product product) {
    return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
  }
}
