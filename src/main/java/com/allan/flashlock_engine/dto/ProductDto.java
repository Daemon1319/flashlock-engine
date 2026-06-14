package com.allan.flashlock_engine.dto;

import java.math.BigDecimal;

public record ProductDto(
  Long id,
  String name,
  BigDecimal price,
  Integer stock
) {}
