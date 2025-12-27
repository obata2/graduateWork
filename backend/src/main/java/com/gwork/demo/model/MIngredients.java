package com.gwork.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="m_ingredients")
public class MIngredients {
  @Id
  @Column(name="ingredient_id")
  private String ingredientId;

  @Column(name="ingredient_name")
  private String ingredientName;

  @Column(name="price_unit_qty")
  private String priceUnitQty;
}
