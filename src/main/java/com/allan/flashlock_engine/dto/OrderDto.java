package com.allan.flashlock_engine.dto;

import java.time.Instant;

public record OrderDto(
  Long id,
  Long productId,
  String username,
  Instant createdAt) {
}
