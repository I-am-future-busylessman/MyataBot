package com.myata_bot.core.handler;

import com.myata_bot.core.botapi.BotState;
import com.myata_bot.core.keyboards.AdminKeyboards;
import com.myata_bot.core.botapi.InputMessageHandler;
import com.myata_bot.core.keyboards.UserKeyboards;
import com.myata_bot.core.models.ReservationEntity;
import com.myata_bot.core.models.UserEntity;
import com.myata_bot.core.services.ReplyService;
import com.myata_bot.core.services.ReservationService;
import com.myata_bot.core.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class AdminHandler implements InputMessageHandler {
    private final UserService userService;
    private final ReplyService messageService;
    private final ReservationService resService;

    public AdminHandler(UserService userService, ReplyService messageService, ReservationService resService) {
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

    private List<BotApiMethod<?>> processUserInput(Message message) {
        int userId = message.getFrom().getId();
        long chatId = message.getChatId();

        BotState botState = userService.getUserCurrentState(userId);
        UserEntity user = userService.findUserByID(userId);
        List<BotApiMethod<?>> reply = new ArrayList<>();
        System.out.println(botState.toString());
        if (botState.equals(BotState.ASK_ADMIN_NAME)) {
            reply.add(messageService.getReplyMessage(userId,"Как тебя зовут?"));
            userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_NAME);
        }
        if (botState.equals(BotState.COLLECT_ADMIN_NAME)) {
            reply.add(messageService.getReplyMessage(userId, "Понял, " + message.getText() + " здесь босс.\n" +
                    "Чем могу помочь?", AdminKeyboards.adminMainPanel()));
            user.setName(message.getText());
            user.setRole("Admin");
            userService.save(user);
            userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
        }
        if (botState.equals(BotState.ASK_ADMIN_TO_DO)) {
            reply.add(messageService.getReplyMessage(userId, "Чем могу помочь?"));
            userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
        }
        if (botState.equals(BotState.COLLECT_ADMIN_TO_DO)) {
            userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
            switch (message.getText()) {
                case "Список броней":
                    if (resService.findAllNearest().size() == 0) {
                        reply.add(messageService.getReplyMessage(userId, "Нет ближайших броней", AdminKeyboards.adminMainPanel()));
                    }
                    else {
                        resService.findAllNearest().forEach(reservation
                                -> reply.add(messageService.getReplyMessage(chatId,
                                resService.getReservationInfo(reservation.getUserID()))));
                        reply.add(messageService.getReplyMessage(userId, "Чем ещё могу помочь?", AdminKeyboards.adminMainPanel()));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                    }
                    break;
                case "Подтвердить бронь":
                    if (resService.findAllUnconfirmed().size() == 0) {
                        reply.add(messageService.getReplyMessage(userId, "Все брони подтверждены", AdminKeyboards.adminMainPanel()));
                    } else {
                        reply.add(messageService.getReplyMessage(userId, "Какую из броней хотите подтвердить",
                                AdminKeyboards.adminReservationsPanel(resService.findAllUnconfirmed())));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_CONFIRM_RESERVATION);
                    }
                    break;
                case "Отменить бронь":
                    if (resService.findAllUndone().size() == 0) {
                        reply.add(messageService.getReplyMessage(userId, "Нет броней которые можно отменить", AdminKeyboards.adminMainPanel()));
                    } else {
                        reply.add(messageService.getReplyMessage(userId, "Какую из броней хотите отменить",
                                AdminKeyboards.adminReservationsPanel(resService.findAllUndone())));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_DELETE_RESERVATION);
                    }
                    break;
                case "Бронь пришла":
                    if (resService.findAllNearest().size() == 0){
                        reply.add(messageService.getReplyMessage(userId, "Нет броней которые могли прийти", AdminKeyboards.adminMainPanel()));
                    }else {
                        reply.add(messageService.getReplyMessage(userId, "Какая из броней пришла?",
                                AdminKeyboards.adminReservationsPanel(resService.findAllNearest())));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_ARRIVED_RESERVATION);
                    }
                    break;
                case "Столик ушёл":
                    if (resService.findAllActive().size() == 0){
                        reply.add(messageService.getReplyMessage(userId, "Нет активных столиков", AdminKeyboards.adminMainPanel()));
                    }else {
                        reply.add(messageService.getReplyMessage(userId, "Какая из броней ушла?",
                                AdminKeyboards.adminReservationsPanel(resService.findAllActive())));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_LEAVE_RESERVATION);
                    }
                    break;
                default:
                    reply.add(messageService.getReplyMessage(userId, "Не понял вас...\n" +
                            "Чем ещё могу помочь?", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                    break;
            }
        }
        if (botState.equals(BotState.COLLECT_ADMIN_CONFIRM_RESERVATION)){
            AtomicReference<ReservationEntity> reservationFound = new AtomicReference<>();
            if (resService.findAllUnconfirmed().size() > 0) {
                resService.findAllUnconfirmed().forEach(reservation -> {
                    if (reservation.getName().equals(message.getText().split(" ")[0]) &&
                            reservation.getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")).equals(message.getText().substring(message.getText().indexOf(" ") + 1))) {
                        reservation.setConfirmed(true);
                        reservation.setConfirmedBy(user.getName());
                        resService.save(reservation);
                        reservationFound.set(reservation);
                        reply.add(messageService.getReplyMessage(reservation.getUserID(), "Ваша бронь подтверждена." +
                                "\n Ждём вас в Amigo Lounge"));
                    }
                });
                if (reservationFound.get().isConfirmed()) {
                    for (UserEntity admin: userService.findAllAdmins()){
                        reply.add(messageService.getReplyMessage(admin.getUserID(), "Бронь на имя " + reservationFound.get().getName() + " на время " + reservationFound.get().getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")) + " подтверждена." , AdminKeyboards.adminMainPanel()));
                    }
                    reply.add(messageService.getReplyMessage(chatId,
                            "Чем ещё могу помочь?", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                } else{
                    reply.add(messageService.getReplyMessage(chatId, "Нет активных броней с такими параметрами", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                }
            }else {
                reply.add(messageService.getReplyMessage(chatId, "Нет активных броней", AdminKeyboards.adminMainPanel()));
                userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
            }
        }
        if (botState.equals(BotState.COLLECT_ADMIN_DELETE_RESERVATION)) {
            AtomicReference<ReservationEntity> reservationFound = new AtomicReference<>();
            if (resService.findAllUndone().size() > 0) {
                resService.findAllUndone().forEach(reservation -> {
                    if (reservation.getName().equals(message.getText().split(" ")[0]) &&
                            reservation.getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")).equals(message.getText().substring(message.getText().indexOf(" ") + 1))) {
                        reservationFound.set(reservation);
                        reply.add(messageService.getReplyMessage(reservation.getUserID(), "Ваша бронь отменена администратором."));
                    }
                });
                System.out.println(reservationFound);
                if (reservationFound.get().isCompleted()) {
                    for (UserEntity admin: userService.findAllAdmins()){
                        reply.add(messageService.getReplyMessage(admin.getUserID(), "Бронь на имя " + reservationFound.get().getName() + " на время " + reservationFound.get().getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")) + " отменена." , AdminKeyboards.adminMainPanel()));
                    }
                    ReservationEntity reservation = reservationFound.get();
                    reservation.setUserID(0);
                    reservation.setFeedback("Отменена Администратором");
                    resService.save(reservation);
                    reply.add(messageService.getReplyMessage(chatId,
                            "Чем ещё могу помочь?", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                } else{
                    reply.add(messageService.getReplyMessage(chatId, "Нет активных броней с такими параметрами", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                }
            }else {
                reply.add(messageService.getReplyMessage(chatId, "Нет активных броней", AdminKeyboards.adminMainPanel()));
                userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
            }
        }
        if (botState.equals(BotState.COLLECT_ADMIN_ARRIVED_RESERVATION)) {
            AtomicReference<ReservationEntity> reservationFound = new AtomicReference<>();
            if (resService.findAllNearest().size() > 0) {
                resService.findAllNearest().forEach(reservation -> {
                    if (reservation.getName().equals(message.getText().split(" ")[0]) &&
                            reservation.getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")).equals(message.getText().substring(message.getText().indexOf(" ") + 1))) {
                        reservation.setDone(true);
                        reservationFound.set(reservation);
                    }
                });
                if (reservationFound.get().isConfirmed()) {
                    for (UserEntity admin : userService.findAllAdmins()) {
                        reply.add(messageService.getReplyMessage(admin.getUserID(), "Бронь на имя " + reservationFound.get().getName() + " на время " + reservationFound.get().getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")) + " пришла.", AdminKeyboards.adminMainPanel()));
                    }
                        resService.save(reservationFound.get());
                        reply.add(messageService.getReplyMessage(chatId,
                                "Чем ещё могу помочь?", AdminKeyboards.adminMainPanel()));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                    } else{
                        reply.add(messageService.getReplyMessage(chatId, "Нет ближайших броней с такими параметрами", AdminKeyboards.adminMainPanel()));
                        userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                    }
                } else {
                    reply.add(messageService.getReplyMessage(chatId, "Нет ближайших броней", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                }
            }
        if (botState.equals(BotState.COLLECT_ADMIN_LEAVE_RESERVATION)) {
            AtomicReference<ReservationEntity> reservationFound = new AtomicReference<>();
            if (resService.findAllActive().size() > 0) {
                resService.findAllActive().forEach(reservation -> {
                    if (reservation.getName().equals(message.getText().split(" ")[0]) &&
                            reservation.getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")).equals(message.getText().substring(message.getText().indexOf(" ") + 1))) {
                        reservation.setEnd(true);
                        reservationFound.set(reservation);
                    }
                });
                if (reservationFound.get().isEnd()) {
                    for (UserEntity admin: userService.findAllAdmins()){
                        reply.add(messageService.getReplyMessage(admin.getUserID(), "Бронь на имя " + reservationFound.get().getName() + " на время " + reservationFound.get().getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm")) + " уолучила сообщение об обратной связи." , AdminKeyboards.adminMainPanel()));
                    }
                    ReservationEntity reservation = reservationFound.get();
                    userService.setUserCurrentState(reservation.getUserID(), BotState.COLLECT_FEEDBACK_SCORE);
                    reply.add(messageService.getReplyMessage(reservation.getUserID(), "Спасибо за посещение Amigo lounge. Пожалуйста оцените уровеь сервиса в нашем заведении.", UserKeyboards.userFeedbackPanel()));
                    resService.save(reservation);
                    reply.add(messageService.getReplyMessage(chatId,
                            "Чем ещё могу помочь?", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                } else{
                    reply.add(messageService.getReplyMessage(chatId, "Нет ближайших броней с такими параметрами", AdminKeyboards.adminMainPanel()));
                    userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
                }
            }else {
                reply.add(messageService.getReplyMessage(chatId, "Нет ближайших броней", AdminKeyboards.adminMainPanel()));
                userService.setUserCurrentState(userId, BotState.COLLECT_ADMIN_TO_DO);
            }
        }

        return reply;
    }

    @Override
    public BotState getHandlerName() {
        return BotState.MAIN_ADMIN;
    }
}
