package com.gwork.demo.Controller;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.NutrientService;

@RestController
@RequestMapping("/nutrient")
public class nutrientController {
      private final NutrientService nutrientService = new NutrientService();
    @GetMapping("/targets")
    public double[] getTargets(){
        return nutrientService.getTargets();
    }
}
