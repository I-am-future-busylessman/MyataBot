package com.myata_bot.core.handler;

import com.myata_bot.core.botapi.BotState;
import com.myata_bot.core.botapi.InputMessageHandler;
import com.myata_bot.core.repository.UserRepository;
import com.myata_bot.core.services.ReplyService;
import com.myata_bot.core.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NewUserHandler implements InputMessageHandler {
    private final UserService userService;
    private final ReplyService messageService;

    public NewUserHandler(UserService userService, ReplyService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        if (userService.getUserCurrentState(message.getFrom().getId()).equals(BotState.FILLING_PROFILE)) {
            userService.setUserCurrentState(message.getFrom().getId(), BotState.ASK_NAME);
        }
        return processUserInput(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.FILLING_PROFILE;
    }

    private List<BotApiMethod<?>> processUserInput(Message message) {
        int userId = message.getFrom().getId();
        long chatId = message.getChatId();

        BotState botState = userService.getUserCurrentState(userId);

        List<BotApiMethod<?>> reply = new ArrayList<>();

        if (botState.equals(BotState.ASK_NAME)) {
            reply.add(messageService.getReplyMessage(chatId, "От имени Myata Lounge Center приветствую вас!\n" +
                    "Как я могу к вам обращаться?"));
            userService.setUserCurrentState(userId, BotState.ASK_TO_DO_FROM_UNKNOWN);
        }
        System.out.println("here");
        return reply;
    }
}
