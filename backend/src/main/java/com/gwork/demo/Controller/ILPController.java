package com.gwork.demo.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.ILPService;
import com.gwork.demo.dto.ILPReplaceRequestDTO;
import com.gwork.demo.dto.ILPResultDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/ILP")
public class ILPController {
  
  private final ILPService iLPService;
  public ILPController (ILPService iLPService) {
    this.iLPService = iLPService;
  }


  @PostMapping("/replaceResults")
  public void replaceResults (@RequestBody ILPReplaceRequestDTO ilpReplaceRequestDTO) {
    iLPService.replaceResults(ilpReplaceRequestDTO);
  }


  @PostMapping("/findAll")
  public List<ILPResultDTO> findAllByUserId (@RequestBody Map<String, String> body) {
    String userId = body.get("userId");
    return iLPService.findAllByUserId(userId);
  }
  
  



  @GetMapping("/find")
  public List<ILPResultDTO> find() {
    return iLPService.findAllByUserId("admin");
  }
  
  
}
