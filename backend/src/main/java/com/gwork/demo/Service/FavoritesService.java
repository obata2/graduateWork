package com.gwork.demo.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

  // 同じハッシュ値になるようなレコードが存在するかを判定
  public boolean existsByUserIdAndObjectHash(FavoritesRequestDTO dto) {
    String userId = dto.getUserId();
    String hash = generateHash(dto);
    boolean isExist = repository.existsByUserIdAndHash(userId, hash);
    if(isExist){
      System.out.println(dto.getMenuName() + "は存在します！");
    }else{
      System.out.println(dto.getMenuName() + "は存在しません");
    }
    return isExist;
  }

  // ハッシュ値生成
  private String generateHash(FavoritesRequestDTO dto) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // ハッシュ対象フィールドを明示的に連結
      String source =
          dto.getMenuName() + "|" +
          dto.getDishName();

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