package com.gwork.demo.model;

import java.util.LinkedHashMap;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@IdClass(ILPResultsId.class)
@Table(name="ilp_results")
public class ILPResults {
  @Id
  @Column(name="user_id")
  private String userId;

  @Id
  @Column(name="result_id")
  private Integer resultId;

  @Column(name="ingredients")
  @JdbcTypeCode(SqlTypes.JSON)
  private LinkedHashMap<String, String> ingredients;

  @Column(name="total_price")
  private int totalPrice;

  @Column(name="total_kcal")
  private int totalKcal;

  @Column(name="pfc_kcal")
  private double[] pfcKcal;

  @Column(name="calculated_nutrients")
  private double[] calculatedNutrients;

  @Column(name="nutrients_contri_rate")
  @JdbcTypeCode(SqlTypes.JSON)
  private LinkedHashMap<String, double[]> nutrientsContriRate;

  @Column(name="pfc_contri_rate")
  @JdbcTypeCode(SqlTypes.JSON)
  private LinkedHashMap<String, double[]> pfcContriRate;

  @Column(name="price_breakdown", columnDefinition = "JSON")
  @JdbcTypeCode(SqlTypes.JSON)
  private LinkedHashMap<String, Integer> priceBreakdown;
}
