package com.myata_bot.core.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Service
public class ReplyService {

    public ReplyService() {
    }

    public SendMessage getReplyMessage(long userID, String message) {
        return new SendMessage(userID, message);
    }

    public SendMessage getReplyMessage(long userID, String message, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage(userID, message);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }
}

