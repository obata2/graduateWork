package com.gwork.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.mapper.MIngredientsMapper;
import com.gwork.demo.dto.MIngredientsSummaryDTO;

@RestController
@RequestMapping("/mapper")
public class MapperController {

  private MIngredientsMapper mIngredientsMapper;
  public MapperController (MIngredientsMapper mIngredientsMapper) {
    this.mIngredientsMapper = mIngredientsMapper;
  }

  @GetMapping("/mIngredients")
  public MIngredientsSummaryDTO getSummary () {
    return mIngredientsMapper.toSummary();
  }
}
