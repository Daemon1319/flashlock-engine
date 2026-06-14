package com.allan.flashlock_engine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
  name = "products",
  check = @CheckConstraint(
    name = "chk_stock_non_negative",
    constraint = "stock >= 0"
  )
)
@EntityListeners(AuditingEntityListener.class)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank                          
  @Column(nullable = false)
  private String name;

  @NotNull
  @DecimalMin("0.00")               
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @NotNull
  @PositiveOrZero
  @Column(nullable = false)
  private Integer stock;

  @Version
  private Long version;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public Product() {}

  public Product(String name, BigDecimal price, Integer stock) {
    this.name = name;
    this.price = price;
    this.stock = stock;
  }

  public Long getId() { 
    return id; 
  }

  public void setId(Long id) { 
    this.id = id; 
  }

  public String getName() { 
    return name; 
  }

  public void setName(String name) { 
    this.name = name; 
  }

  public BigDecimal getPrice() { 
    return price; 
  }

  public void setPrice(BigDecimal price) { 
    this.price = price; 
  }

  public Integer getStock() { 
    return stock; 
  }

  public void setStock(Integer stock) { 
    this.stock = stock; 
  }

  public Long getVersion() { 
    return version; 
  }

  public Instant getCreatedAt() { 
    return createdAt; 
  }

  public Instant getUpdatedAt() { 
    return updatedAt; 
  }
}