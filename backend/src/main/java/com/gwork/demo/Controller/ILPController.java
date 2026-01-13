package com.gwork.demo.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gwork.demo.Service.ilp.ILPService;
import com.gwork.demo.dto.ILPReplaceRequestDTO;
import com.gwork.demo.dto.ILPResultDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/ilp-results")
public class ILPController {
  
  private final ILPService iLPService;
  public ILPController (ILPService iLPService) {
    this.iLPService = iLPService;
  }

  // 最適化計算を行った後、DBを更新する
  @PutMapping("/{userId}")
  public void replaceResults (@PathVariable("userId") String userId, @RequestBody ILPReplaceRequestDTO ilpReplaceRequestDTO) {
    iLPService.replaceResults(userId, ilpReplaceRequestDTO);
  }

  // DBからレコードのリストを取得する
  @GetMapping("/{userId}")
  public List<ILPResultDTO> findAllByUserId (@PathVariable("userId") String userId) {
    return iLPService.findAllByUserId(userId);
  }
}
