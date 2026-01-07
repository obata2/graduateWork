package com.gwork.demo.Service.favorites;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gwork.demo.dto.FavoritesRequestDTO;
import com.gwork.demo.model.Favorites;
import com.gwork.demo.repository.FavoritesRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FavoritesService {

  @Autowired
  FavoritesRepository repository;

  // レコードを新規登録する
  public void save(FavoritesRequestDTO dto) {
    Favorites entity = new Favorites();
    BeanUtils.copyProperties(dto, entity);
    String hash = generateHash(dto);
    entity.setHash(hash);
    repository.save(entity);
  }

  // 同じuserIdで、同じハッシュ値になるようなレコードが存在するかを判定
  public boolean existsByUserIdAndObjectHash(FavoritesRequestDTO dto) {
    String userId = dto.getUserId();
    String hash = generateHash(dto);
    boolean isExist = repository.existsByUserIdAndHash(userId, hash);
    
    if(isExist){
      System.out.println(dto.getMenuName() + "は存在します！");
    }else{
      System.out.println("hash : " + hash);
      System.out.println(dto.getMenuName() + "は存在しません");
    }
    return isExist;
  }

  // テーブルから全件取得
  public List<Favorites> findAll () {
    return repository.findAll();
  }

  // レコードを削除
  public void deleteById (String userId, Integer menuId) {
    repository.deleteByUserIdAndMenuId(userId, menuId);
  }

  // ハッシュ値生成
  private String generateHash(FavoritesRequestDTO dto) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // jsonb型ではmapの辞書順が統一されないので、ここでソートしておく
      TreeMap<String, Object> sortedDishName = new TreeMap<>(dto.getDishName());
      // ハッシュ対象フィールドを明示的に連結
      String source =
          dto.getMenuName() + "|" +
          sortedDishName;

      byte[] hashBytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();

    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Hash generation failed", e);
    }
  }
}