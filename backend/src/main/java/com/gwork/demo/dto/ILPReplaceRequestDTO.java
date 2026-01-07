package com.gwork.demo.dto;

import java.util.HashSet;

import lombok.Data;

@Data
public class ILPReplaceRequestDTO {
  private String userId;
  private HashSet<String> selected;
  private HashSet<String> excluded;
}
