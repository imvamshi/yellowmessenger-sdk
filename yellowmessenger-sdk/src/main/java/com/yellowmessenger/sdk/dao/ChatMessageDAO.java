package com.yellowmessenger.sdk.dao;

import com.activeandroid.query.Select;
import com.yellowmessenger.sdk.models.db.ChatMessage;

import java.util.List;

public class ChatMessageDAO extends BaseDAO<ChatMessage>{

    public static ChatMessage findById(Long id) {
        return new Select()
                .from(ChatMessage.class)
                .where("id = ?", id)
                .executeSingle();
    }

    public static List<ChatMessage> findAllByUsername(String username, int limit, int offset) {
        return new Select()
                .from(ChatMessage.class)
                .where("username = ?", username)
                .limit(limit)
                .offset(offset)
                .orderBy("id DESC")
                .execute();
    }

    public static ChatMessage getChatMessageByStanzaId(String stanzaId) {
        return new Select()
                .from(ChatMessage.class)
                .where("stanza_id = ?", stanzaId)
                .executeSingle();
    }

    public static List<ChatMessage> getUnsentMessages() {
        return new Select()
                .from(ChatMessage.class)
                .where("unsent = ?", true)
                .orderBy("id ASC")
                .execute();
    }

    public static List<ChatMessage> findAllByUsernameAndIdGreaterThan(String username, long l) {
        return new Select()
                .from(ChatMessage.class)
                .where("username = ? and id > ?", username,l)
                .orderBy("id ASC")
                .execute();
    }
}
