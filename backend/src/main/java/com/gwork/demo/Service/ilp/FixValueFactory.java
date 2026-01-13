package com.gwork.demo.Service.ilp;

import org.springframework.stereotype.Service;

@Service
public class FixValueFactory {
  private final DataAdjusterFactory dataAdjusterFactory;
  public FixValueFactory(DataAdjusterFactory dataAdjusterFactory) {
    this.dataAdjusterFactory = dataAdjusterFactory;
  }

  public FixValue create(String userId, int stapleIndex, int proteinIndex) {
    DataAdjuster dataAdjuster = dataAdjusterFactory.create(userId);
    FixValue fixValue = new FixValue(dataAdjuster);
    fixValue.init(stapleIndex, proteinIndex);
    return fixValue;
  }
}
