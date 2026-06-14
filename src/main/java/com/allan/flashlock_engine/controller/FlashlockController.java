package com.allan.flashlock_engine.controller;

import com.allan.flashlock_engine.dto.OrderDto;
import com.allan.flashlock_engine.dto.ProductDto;
import com.allan.flashlock_engine.dto.PurchaseResponse;
import com.allan.flashlock_engine.service.FlashlockService;
import com.allan.flashlock_engine.service.FlashlockService.LockMode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashsale")
public class FlashlockController {
  
  private final FlashlockService flashlockService;

  public FlashlockController(FlashlockService flashlockService) {
    this.flashlockService = flashlockService;
  }

  @PostMapping("/setup")
  public ResponseEntity<ProductDto> setupDatabase() {
    return ResponseEntity.ok(flashlockService.setupDatabase());
  }

  @PostMapping("/buy")
  public ResponseEntity<PurchaseResponse> buy(@RequestParam Long productId, @RequestParam String username) {
    PurchaseResponse response = flashlockService.buyProduct(productId, username);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/clear")
  public ResponseEntity<Map<String, String>> clearDatabase() {
    flashlockService.clearDatabase();
    return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "All orders wiped and stock reset to 0."));
  }

  @GetMapping("/orders")
  public ResponseEntity<List<OrderDto>> getAllOrders() {
    return ResponseEntity.ok(flashlockService.getAllOrders());
  }

  @GetMapping("/product")
  public ResponseEntity<ProductDto> getProduct() {
    ProductDto productDto = flashlockService.getProduct();
    if (productDto == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(productDto); 
  }

  @PostMapping("/cycle-lock")
  public ResponseEntity<Map<String, String>> cycleLock() {
    LockMode newMode = flashlockService.cycleLockMode();
    return ResponseEntity.ok(Map.of("lockMode", newMode.name()));
  }

  @GetMapping("/lock-status")
  public ResponseEntity<Map<String, String>> getLockStatus() {
    return ResponseEntity.ok(Map.of("lockMode", flashlockService.getLockMode().name()));
  }
}
