package com.gwork.demo.dto;

// prices_latestとm_ingredientsを結合したDTO            userIdが無いのがアカン
public record PriceLatestRowDTO (
  String userId,
  String ingredientId,
  Boolean isFixed,
  String ingredientName,
  Integer priceLatest,
  String priceUnitQty
){}
