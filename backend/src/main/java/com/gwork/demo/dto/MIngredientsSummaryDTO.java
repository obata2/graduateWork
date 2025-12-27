package com.gwork.demo.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class MIngredientsSummaryDTO {
  private LinkedHashMap<String, String> nameAndId;
  private LinkedHashMap<String, String> nameAndPriceUnitQty;
}
