package com.myata_bot.core.botapi;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface InputMessageHandler {
    List<BotApiMethod<?>> handle(Message message);
    BotState getHandlerName();
}
