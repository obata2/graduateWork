package com.gwork.demo.model;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "favorites")
public class Favorites {

    // 各カラムの値をそれぞれのフィールドに保持する
    @Column(name="user_id")
    private String userId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="menu_id")
    private Integer menuId;

    @Column(name="hash")
    private String hash;
	
    @Column(name="ingredients", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String,String> ingredients;
	
    @Column(name="total_price")
    private Integer totalPrice;

    @Column(name="total_kcal")
    private Integer totalKcal;

    @Column(name="pfc_kcal")
    private Integer[] pfcKcal;

    @Column(name="calculated_nutrients")
    private double[] calculatedNutrients;

    @Column(name="nutrients_contri_rate", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, double[]> nutrientsContriRate;

    @Column(name="pfc_contri_rate", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, double[]> pfcContriRate;

    @Column(name="menu_name")
    private String menuName;

    @Column(name="dish_name", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> dishName;

    @Column(name="instructions", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> instructions;

    @Column(name="seasonings", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> seasonings;

    @Column(name="memo")
    private String memo;
}
