package com.gwork.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@IdClass(PricesLatestId.class)
@Table(name = "prices_latest")
public class PricesLatest {
  // 各カラムの値をそれぞれのフィールドに保持する
    @Id
    @Column(name="user_id")
    private String userId;

    @Id
    @Column(name="ingredient_id")
    private String ingredientId;

    @Column(name="is_fixed")
    private boolean isFixed;

    @Column(name="price_latest")
    private Integer priceLatest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ingredient_id", nullable=false)
    private MIngredients mIngredients;
}