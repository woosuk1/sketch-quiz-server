package com.itcen.whiteboardserver.member.repository;

import com.itcen.whiteboardserver.member.entity.Nicknames;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NicknamesRepository extends JpaRepository<Nicknames, Long> {
    @Query(value = "SELECT nickname FROM nicknames ORDER BY random() LIMIT 1", nativeQuery = true)
    String findRandomNickname();
}