package com.gwork.demo.Controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.ILPResultDTO;
import com.gwork.demo.Service.ilp.SolveILPService;

@RestController
@RequestMapping("/ILP")
public class ILPController {
  @Autowired
    private final SolveILPService integerLinearService = new SolveILPService();

  @GetMapping("/solve")
  public ArrayList<ILPResultDTO> solve(){
      return integerLinearService.getILPResultList();
  }
}
