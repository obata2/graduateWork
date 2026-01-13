package com.gwork.demo.Service.ilp;

import org.springframework.stereotype.Service;

import com.gwork.demo.Service.estat.EstatService;

@Service
public class DataAdjusterFactory {
  private final EstatService estatService;
  public DataAdjusterFactory (EstatService estatService) {
    this.estatService = estatService;
  }

  public DataAdjuster create (String userId) {
    return new DataAdjuster(userId, estatService);
  }
}
