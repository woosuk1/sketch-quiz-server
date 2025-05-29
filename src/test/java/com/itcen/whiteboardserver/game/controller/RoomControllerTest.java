package com.itcen.whiteboardserver.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcen.whiteboardserver.game.dto.request.RoomRequest;
import com.itcen.whiteboardserver.game.dto.response.RoomResponse;
import com.itcen.whiteboardserver.game.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@AutoConfigureDataJpa
@AutoConfigureDataMongo
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateRoomSuccess() throws Exception {
        // Arrange
        RoomRequest request = new RoomRequest(1L);
        RoomResponse response = new RoomResponse(1L);
        when(roomService.createRoom(any(RoomRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
//                .andDo(print()); // 결과 출력을 통해 자세한 정보 확인
    }

    @Test
    void testCreateRoomFailsValidation() throws Exception {
        // Arrange
        RoomRequest invalidRequest = new RoomRequest();

        // Act & Assert
        mockMvc.perform(post("/api/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
//                .andDo(print()); // 결과 출력을 통해 자세한 정보 확인
    }
}