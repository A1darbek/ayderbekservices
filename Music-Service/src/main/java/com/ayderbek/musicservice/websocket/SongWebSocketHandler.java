package com.ayderbek.musicservice.websocket;

import com.ayderbek.common.SongDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class SongWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = new HashSet<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle incoming messages from clients (if needed)
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessions.remove(session);
    }

    public void notifyClientsAboutNewSong(SongDTO song) {
        TextMessage message = new TextMessage(convertSongDtoToJson(song));

        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                // Handle the exception (e.g., client closed connection)
            }
        }
    }

    private String convertSongDtoToJson(SongDTO song) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(song);
        } catch (JsonProcessingException e) {
            // Handle the exception (e.g., log it) and return an appropriate fallback value
            return "{}";
        }
    }
}

