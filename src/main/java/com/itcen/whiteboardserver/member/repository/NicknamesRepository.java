package com.itcen.whiteboardserver.member.repository;

import com.itcen.whiteboardserver.member.dto.NicknameDTO;
import com.itcen.whiteboardserver.member.entity.Nicknames;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NicknamesRepository extends JpaRepository<Nicknames, Long> {
    @Query(value = "SELECT n.id as id, n.nickname as nickname FROM nicknames n WHERE n.is_used = false ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
//    String findRandomNickname();
    NicknameDTO findRandomNickname();

    @Modifying
    @Query(value = "UPDATE nicknames SET is_used = :isUsed WHERE id = :id", nativeQuery = true)
    Integer updateIsUsed(Long id, boolean isUsed);

    @Modifying
    @Query(value = "UPDATE nicknames SET is_used = false WHERE nickname = :nickname", nativeQuery = true)
    Integer updateIsUsedByNickname(String nickname);
}