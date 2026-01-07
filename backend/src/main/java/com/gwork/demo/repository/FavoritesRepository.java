package com.gwork.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gwork.demo.model.Favorites;

public interface FavoritesRepository extends JpaRepository<Favorites, Integer> {

  //↓クエリ自動生成
  // userIdとhashで検索して存在を判定
  boolean existsByUserIdAndHash(String userId, String hash);

  // userIdとmenuIdで検索して該当レコードを削除
  void deleteByUserIdAndMenuId(String userId, Integer menuId);
}
