package com.gwork.demo.Service.ilp;

import org.springframework.stereotype.Service;

@Service
public class FixValueFactory {
  private final DataAdjusterService dataAdjusterService;
  public FixValueFactory(DataAdjusterService dataAdjusterService) {
    this.dataAdjusterService = dataAdjusterService;
  }

  public FixValue create(int stapleIndex, int proteinIndex) {
    FixValue fixValue = new FixValue(dataAdjusterService);
    fixValue.init(stapleIndex, proteinIndex);
    return fixValue;
  }
}
