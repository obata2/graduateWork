package com.gwork.demo.dto;

public record PriceLatestRowDTO (
  String ingredientId,
  boolean isFixed,
  String ingredientName,
  Integer priceLatest,
  String priceUnitQty
){}
