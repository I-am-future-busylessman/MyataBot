package com.myata_bot.core;

import com.myata_bot.core.botapi.TelegramFacade;
import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Setter
public class MyataBot extends TelegramLongPollingBot {
    private String botUserName;
    private String botToken;

    private TelegramFacade telegramFacade;

    public MyataBot (DefaultBotOptions botOptions, TelegramFacade telegramFacade){
        super(botOptions);
        this.telegramFacade = telegramFacade;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            for (BotApiMethod<?> method:telegramFacade.handleUpdate(update)) {
                execute(method);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}

