package com.gwork.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.gwork.demo.Service.favorites.FavoritesService;
import com.gwork.demo.dto.FavoritesExistenceResDTO;
import com.gwork.demo.dto.FavoritesRequestDTO;
import com.gwork.demo.model.Favorites;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {
  @Autowired
  FavoritesService favoritesService;

  // お気に入りの献立を新規登録する
  @PutMapping("/{userId}")
  public void save(@PathVariable("userId") String userId, @RequestBody FavoritesRequestDTO favoritesRequestDTO) {
    favoritesService.save(userId, favoritesRequestDTO);
  }

  // 既存のレコードに対し、memoの内容のみ更新する
  @PatchMapping("/{userId}/{menuId}")
  public void updateMemo(@PathVariable("userId") String userId, @PathVariable("menuId") Integer menuId, @RequestBody Map<String, String> body) {
    favoritesService.updateMemo(userId, menuId, body.get("memo"));
  }

  // すでにお気に入りに同じ献立が登録されているか確認し、併せてmemoの内容も取り出す
  @PostMapping("/{userId}/existence")
  public FavoritesExistenceResDTO exist(@PathVariable("userId") String userId, @RequestBody Favorites favorites) {
    return favoritesService.existsByUserIdAndObjectHash(userId, favorites);
  }

  // userId で検索してレコードをリストで取得
  @GetMapping("/{userId}")
  public List<Favorites> findAll(@PathVariable("userId") String userId) {
    return favoritesService.findByUserId(userId);
  }

  // userId と menuId で該当するレコードを削除
  @DeleteMapping("/{userId}/{menuId}")
  public void deleteById(@PathVariable("userId") String userId, @PathVariable("menuId") Integer menuId) {
    favoritesService.deleteById(userId, menuId);
  }

}