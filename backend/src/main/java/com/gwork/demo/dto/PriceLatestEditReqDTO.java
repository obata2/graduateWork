package com.gwork.demo.dto;

import org.springframework.stereotype.Service;

import lombok.Data;

// 価格書き換えリクエストを受ける際のDTO
@Data
@Service
public class PriceLatestEditReqDTO {
  private String userId;
  private String ingredientId;
  private Boolean isFixed;
  private Integer priceLatest;
}