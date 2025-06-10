package com.itcen.whiteboardserver.friends.controller;

import com.itcen.whiteboardserver.friends.dto.FriendsDTO;
import com.itcen.whiteboardserver.friends.service.FriendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendsController {

    private final FriendsService friendsService;

    @PostMapping("/friends")
     public ResponseEntity<FriendsDTO> postFriend(@RequestBody FriendsDTO friendsDTO) {

        friendsService.postFriend(friendsDTO);

        return ResponseEntity.ok(friendsDTO);
     }
}
