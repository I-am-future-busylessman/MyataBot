package com.myata_bot.core.handler;

import com.myata_bot.core.botapi.BotState;
import com.myata_bot.core.botapi.InputMessageHandler;
import com.myata_bot.core.models.ReservationEntity;
import com.myata_bot.core.services.ReplyService;
import com.myata_bot.core.services.ReservationService;
import com.myata_bot.core.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FeedbackHandler implements InputMessageHandler {
    private final UserService userService;
    private final ReplyService messageService;
    private final ReservationService reservationService;

    public FeedbackHandler(UserService userService, ReplyService messageService, ReservationService reservationService) {
        this.userService = userService;
        this.messageService = messageService;
        this.reservationService = reservationService;
    }

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        if (userService.getUserCurrentState(message.getFrom().getId()).equals(BotState.COLLECTING_FEEDBACK)) {
            userService.setUserCurrentState(message.getFrom().getId(), BotState.COLLECT_FEEDBACK_SCORE);
        }
        return processUserInput(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.COLLECTING_FEEDBACK;
    }

    private List<BotApiMethod<?>> processUserInput(Message message) {
        int userId = message.getFrom().getId();
        long chatId = message.getChatId();
        ReservationEntity reservation = reservationService.findByUserID(userId);

        BotState botState = userService.getUserCurrentState(userId);

        List<BotApiMethod<?>> reply = new ArrayList<>();

        if (botState.equals(BotState.COLLECT_FEEDBACK_SCORE)) {
            reply.add(messageService.getReplyMessage(chatId, "Спасибо что поставили оценку нашему заведению. Будем благодарны если вы опишите что вам понравилось или не понравилось в нагем заведении."));
            reservation.setScore(message.getText());
            reservationService.save(reservation);
            userService.setUserCurrentState(userId, BotState.COLLECT_FEEDBACK_COMMENT);
        }
        if (botState.equals(BotState.COLLECT_FEEDBACK_COMMENT)) {
            reservation.setFeedback(message.getText());
            reservationService.delete(userId);
            reservation.setUserID(0);
            reservationService.save(reservation);
            reply.add(messageService.getReplyMessage(chatId, "Спасибо что оставили отзыв. Будем рады видеть вас снова!"));
            userService.setUserCurrentState(userId, BotState.COLLECT_FEEDBACK_COMMENT);
        }
        return reply;
    }
}

