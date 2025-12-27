package com.gwork.demo.dto;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class EstatAPIParamDTO {
  private LinkedHashMap<String, String> areaParam;
  private LinkedHashMap<String, String> timeFromParam;
}
