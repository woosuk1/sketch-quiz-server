package com.itcen.whiteboardserver.member.repository;

import com.itcen.whiteboardserver.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE Member m SET m.nickname = :nickname, m.updatedAt = current timestamp WHERE m.id = :id")
    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);}
