package com.gwork.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.filter.ingredientNameFilterService;

@RestController
@RequestMapping("/filter")
public class FilterController {
    // --- 食材名のfilteredを取得 ---
    @GetMapping("/filteredIngAndName")
    public Map<String, String> getFilteredIngAndName() {
      return ingredientNameFilterService.nameAndId;
    }

    @GetMapping("/idAndPriceUnit")
    public Map<String, String> getIdAndPriceUnit() {
      return ingredientNameFilterService.idAndPriceUnit;
    }
}
