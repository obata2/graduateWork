package com.gwork.demo.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.DataAdjusterService;
import com.gwork.demo.Service.ilp.SolveILPService;
import com.gwork.demo.Service.nutrient.NutrientService;
import com.gwork.demo.dto.ILPResultDTO;

import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/nutrient")
public class NutrientController {
    private final NutrientService nutrientService = new NutrientService();
    @GetMapping("/targets")
    public double[] getTargets(){
        return NutrientService.targets;
    }

    @GetMapping("/check")
    public LinkedHashMap<String, Object> check(){
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("unitQuantity", DataAdjusterService.vegUnitQuantity);
        result.put("adjustenNutrientTable", DataAdjusterService.adjustedNutrientTable);
        result.put("adjustedPrices", DataAdjusterService.adjustedPrices);
        result.put("adjustedStdQty", DataAdjusterService.adjustedStandardQty);
        return result;
    }

    @GetMapping("/nameList")
    public String[] getNameList() {
        return NutrientService.name[1];
    }
}
