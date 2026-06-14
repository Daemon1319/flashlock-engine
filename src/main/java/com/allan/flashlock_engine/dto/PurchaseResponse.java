package com.allan.flashlock_engine.dto;

public record PurchaseResponse(
  String status,
  String message,
  String lockMode,
  Integer remainingStock,
  Integer attempts
) {}
