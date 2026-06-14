package com.allan.flashlock_engine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnore
  private Product product;

  @NotBlank
  @Column(nullable = false)
  private String username;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  public Order() {}

  public Order(Product product, String username) {
    this.product = product;
    this.username = username;
  }

  public Long getId() { 
    return id; 
  }

  public void setId(Long id) { 
    this.id = id; 
  }

  public Product getProduct() { 
    return product; 
  }

  public void setProduct(Product product) { 
    this.product = product; 
  }

  public Long getProductId() { 
    return product != null ? product.getId() : null; 
  }

  public String getUsername() { 
    return username; 
  }

  public void setUsername(String username) { 
    this.username = username; 
  }

  public Instant getCreatedAt() { 
    return createdAt; 
  }
}