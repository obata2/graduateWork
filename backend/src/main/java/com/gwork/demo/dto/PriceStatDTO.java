package com.gwork.demo.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class PriceStatDTO {
  private LinkedHashMap<String, ArrayList<String>> dateLabel;
  private LinkedHashMap<String, ArrayList<Integer>> priceTransition;
  private LinkedHashMap<String, Integer> priceLatest;
}