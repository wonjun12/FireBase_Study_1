package com.example.poject_firebase.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// TODO: 2023-08-04 채팅 방 사용자들과, 메시지 저장 클래스
public class ChatRoom implements Serializable {
    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, Message> messages = new HashMap<>();

    public ChatRoom() {
    }
}
