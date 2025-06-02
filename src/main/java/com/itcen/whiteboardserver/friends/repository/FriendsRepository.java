package com.itcen.whiteboardserver.friends.repository;

import com.itcen.whiteboardserver.friends.entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    // Define custom query methods if needed
}
