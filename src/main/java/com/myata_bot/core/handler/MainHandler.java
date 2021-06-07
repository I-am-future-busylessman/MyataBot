package com.myata_bot.core.handler;

import com.myata_bot.core.botapi.BotState;
import com.myata_bot.core.botapi.InputMessageHandler;
import com.myata_bot.core.models.ReservationEntity;
import com.myata_bot.core.models.UserEntity;
import com.myata_bot.core.keyboards.AdminKeyboards;
import com.myata_bot.core.keyboards.UserKeyboards;
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
public class MainHandler implements InputMessageHandler {
    private final UserService userService;
    private final ReservationService resService;
    private final ReplyService messageService;

    public MainHandler(UserService userService, ReservationService resService, ReplyService messageService) {
        this.userService = userService;
        this.resService = resService;
        this.messageService = messageService;
    }

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        if (userService.getUserCurrentState(message.getFrom().getId()).equals(BotState.MAIN)){
            userService.setUserCurrentState(message.getFrom().getId(), BotState.ASK_TO_DO);
        }
        return processUserInput(message);
    }

    private List<BotApiMethod<?>> processUserInput(Message message){
        int userId = message.getFrom().getId();

        BotState botState = userService.getUserCurrentState(userId);
        UserEntity userEntity = userService.findUserByID(userId);

        List<BotApiMethod<?>> reply = new ArrayList<>();

        if (botState.equals(BotState.ASK_TO_DO_FROM_UNKNOWN)){
            reply.add(messageService.getReplyMessage(userId,"Рад приветствовать вас, " + message.getText()
                    + "\nЧем могу помочь?", UserKeyboards.userMainPanel()));
            userEntity.setName(message.getText());
            userService.save(userEntity);
            userService.setUserCurrentState(userId, BotState.COLLECT_TO_DO);
        }
        if (botState.equals(BotState.ASK_TO_DO)) {
            reply.add(messageService.getReplyMessage(userId, "Чем могу помочь?", UserKeyboards.userMainPanel()));
            userService.setUserCurrentState(userId, BotState.COLLECT_TO_DO);
        }
        if (botState.equals(BotState.COLLECT_TO_DO)) {
            switch (message.getText()) {
                case "Забронировать столик" -> {
                    if (!resService.findByUserID(userId).isDone()){
                        reply.add(messageService.getReplyMessage(userId, "У вас уже имеется активная бронь."));
                    } else {
                        ReservationEntity reservationEntity = new ReservationEntity();
                        reservationEntity.setUserID(userId);
                        reservationEntity.setName(userEntity.getName());
                        resService.save(reservationEntity);
                        reply.add(messageService.getReplyMessage(userId, "Подскажите на какую дату вы хотели бы забронировать столик?" +
                                "\n формат: 22.09"));
                        userService.setUserCurrentState(userId, BotState.COLLECT_DATE_ASK_TIME);
                    }
                }
                case "Покажи мою бронь" -> {
                    if (resService.findByUserID(userId) != null) {
                        reply.add(messageService.getReplyMessage(userId, resService.getReservationInfo(userId) + "Чем могу помочь?", UserKeyboards.userMainPanel()));
                    } else {
                        reply.add(messageService.getReplyMessage(userId, "У вас нет активных броней", UserKeyboards.userMainPanel()));

                    }
                    userService.setUserCurrentState(userId, BotState.COLLECT_TO_DO);
                }
                case "Отмени бронь" -> {
                    if (resService.findByUserID(userId) != null) {
                        for (UserEntity admin : userService.findAllAdmins()
                        ) {
                            reply.add(messageService.getReplyMessage(admin.getUserID(), "Пользователь" + userEntity.getName() + " отменил бронь на " + resService.findByUserID(userId).getDateTime(), AdminKeyboards.adminMainPanel()));
                        }
                        resService.delete(userId);
                        reply.add(messageService.getReplyMessage(userId, "Бронь успешно отменена" +
                                "\nМожет я могу помочь чем-то ешё?", UserKeyboards.userMainPanel()));
                    } else {
                        reply.add(messageService.getReplyMessage(userId, "У вас нет активных броней", UserKeyboards.userMainPanel()));
                    }
                    userService.setUserCurrentState(userId, BotState.COLLECT_TO_DO);
                }
                default -> {
                    reply.add(messageService.getReplyMessage(userId, "Не понял вас..."));
                    userService.setUserCurrentState(userId, BotState.COLLECT_TO_DO);
                }
            }
        }
        return reply;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.MAIN;
    }
}
