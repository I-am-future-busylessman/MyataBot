package com.myata_bot.core.botapi;

import com.myata_bot.core.models.UserEntity;
import com.myata_bot.core.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramFacade {
    private final BotStateContext botStateContext;
    private final UserService userService;

    public TelegramFacade(BotStateContext botStateContext, UserService userService) {
        this.botStateContext = botStateContext;
        this.userService = userService;
    }

    public List<BotApiMethod<?>> handleUpdate(Update update) {
        List<BotApiMethod<?>> replyMessage = new ArrayList<>();
        Message message = update.getMessage();
        if(message != null && message.hasText()) {
            log.info("New message from User: {}, charId: {}, with text: {}",
                    message.getFrom().getUserName(), message.getChatId(), message.getText());
            replyMessage = handleInputMessage(message);
        }
        return replyMessage;
    }

    private List<BotApiMethod<?>> handleInputMessage(Message message) {
        String inputMessage = message.getText();
        int userId = message.getFrom().getId();
        BotState botState;

        if (inputMessage.equals("/start") && userService.findUserByID(userId) == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUserID(userId);
            userEntity.setRole("guest");
            userEntity.setState(BotState.ASK_NAME.name());
            userService.save(userEntity);
            botState = BotState.ASK_NAME;
        }
        else if (inputMessage.equals("/startAdmin") && userService.findUserByID(userId) == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUserID(userId);
            userEntity.setRole("admin");
            userEntity.setState(BotState.ASK_NAME.name());
            userService.save(userEntity);
            botState = BotState.ASK_ADMIN_NAME;
        }
        else {
            botState = userService.getUserCurrentState(userId);
        }
        userService.setUserCurrentState(userId, botState);

        return botStateContext.processInputMessage(botState, message);
    }
}
