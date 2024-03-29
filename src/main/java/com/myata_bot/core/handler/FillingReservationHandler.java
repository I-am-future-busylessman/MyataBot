package com.myata_bot.core.handler;

import com.myata_bot.core.botapi.BotState;
import com.myata_bot.core.botapi.InputMessageHandler;
import com.myata_bot.core.models.ReservationEntity;
import com.myata_bot.core.models.UserEntity;
import com.myata_bot.core.services.ReplyService;
import com.myata_bot.core.services.ReservationService;
import com.myata_bot.core.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;


import java.time.DateTimeException;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class FillingReservationHandler implements InputMessageHandler {
    private final UserService userService;
    private final ReplyService messageService;
    private final ReservationService resService;

    public FillingReservationHandler(UserService userService, ReplyService messageService, ReservationService resService) {
        this.userService = userService;
        this.messageService = messageService;
        this.resService = resService;
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
        long chatId = message.getChatId();

        BotState botState = userService.getUserCurrentState(userId);
        UserEntity userEntity = userService.findUserByID(userId);
        ReservationEntity reservationEntity = resService.findByUserID(userId);
        boolean valid = true;
        LocalDateTime date = LocalDateTime.now();
        List<BotApiMethod<?>> reply = new ArrayList<>();

        if (botState.equals(BotState.COLLECT_DATE_ASK_TIME)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm");
            try {
                date = LocalDateTime.parse(message.getText() + ".2021 00:00", formatter);
            }catch (DateTimeParseException e) {
                e.printStackTrace();
                reply.add(messageService.getReplyMessage(chatId, "Неправильный ввод даты. \n Формат: 22.09"));
                userService.setUserCurrentState(userId, BotState.COLLECT_DATE_ASK_TIME);
                valid = false;
            }
            if (valid){
                reservationEntity.setDateTime(date);
                reply.add(messageService.getReplyMessage(chatId, "В какое время вам было бы удобнее всего нас посетить? " +
                        "\n Формат: 17:05"));
                userService.setUserCurrentState(userId, BotState.COLLECT_TIME_ASK_AMOUNT_OF_PEOPLE);
                resService.save(reservationEntity);
            }
        }
        if (botState.equals(BotState.COLLECT_TIME_ASK_AMOUNT_OF_PEOPLE)) {
            try {
                date = resService.findByUserID(userId).getDateTime().withHour(Integer.parseInt(message.getText().split(":")[0])).withMinute(Integer.parseInt(message.getText().split(":")[1]));
            }catch (DateTimeException e) {
                e.printStackTrace();
                reply.add(messageService.getReplyMessage(chatId, "Неправильный ввод времени. \n Формат: 17:05"));
                userService.setUserCurrentState(userId, BotState.COLLECT_TIME_ASK_AMOUNT_OF_PEOPLE);
                valid = false;
            }
            if (valid) {
                reservationEntity.setDateTime(date);
                reply.add(messageService.getReplyMessage(chatId, "На какое количество человек забронировать столик?"));
                userService.setUserCurrentState(userId, BotState.COLLECT_AMOUNT_OF_PEOPLE_ASK_PHONE_NUMBER);
                resService.save(reservationEntity);
            }
        }
        if (botState.equals(BotState.COLLECT_AMOUNT_OF_PEOPLE_ASK_PHONE_NUMBER)) {
            reservationEntity.setAmountOfPeople(message.getText());
            reply.add(messageService.getReplyMessage(chatId, "По какому номеру с вами возможно связаться?"));
            userService.setUserCurrentState(userId, BotState.COLLECT_PHONE_NUMBER_ASK_COMMENT);
            resService.save(reservationEntity);
        }
        if (botState.equals(BotState.COLLECT_PHONE_NUMBER_ASK_COMMENT)) {
            if (!message.getText().matches("^(\\s*)?(\\+)?([- _():=+]?\\d[- _():=+]?){10,14}(\\s*)?$")){
                reply.add(messageService.getReplyMessage(chatId, "Неправильный ввод номера."));
                valid = false;
            }
            if (valid) {
                reservationEntity.setPhoneNumber(message.getText());
                reply.add(messageService.getReplyMessage(chatId, "Если есть какие-то дополнительные пожелания, напишите их ниже."));
                userService.setUserCurrentState(userId, BotState.COLLECT_COMMENT_AND_COMPLETE);
                resService.save(reservationEntity);
            }
        }
        if (botState.equals(BotState.COLLECT_COMMENT_AND_COMPLETE)) {
            reservationEntity.setComment(message.getText());
            reservationEntity.setCompleted(true);
            resService.save(reservationEntity);
            for (UserEntity admin: userService.findAllAdmins()
                 ) {
                reply.add(messageService.getReplyMessage(admin.getUserID(), "Новый запрос!\n" + resService.getReservationInfo(userId)));
            }
            reply.add(messageService.getReplyMessage(chatId, "Спасибо что забронировали у нас столик!" +
                    "\n Ожидайте подтверждения."));
            userService.setUserCurrentState(userId, BotState.COLLECT_TO_DO);
        }
        return reply;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.FILLING_RESERVATION;
    }
}
