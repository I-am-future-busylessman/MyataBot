package com.myata_bot.core.keyboards;

import com.myata_bot.core.models.ReservationEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminKeyboards {

    public static ReplyKeyboardMarkup adminMainPanel() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("Список броней"));
        keyboardFirstRow.add(new KeyboardButton("Бронь пришла"));
        keyboardFirstRow.add(new KeyboardButton("Столик ушёл"));
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton("Подтвердить бронь"));
        keyboardSecondRow.add(new KeyboardButton("Отменить бронь"));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup adminReservationsPanel(List<ReservationEntity> reservationEntities) {
        System.out.println(reservationEntities.size());
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        for (int i = 0; i < reservationEntities.size() && i < 6; i++) {
            if (i < reservationEntities.size()%3) {
                keyboardFirstRow.add(new KeyboardButton(reservationEntities.get(i).getName() + " " + reservationEntities.get(i).getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm"))));
            }
            if (i >= reservationEntities.size()%3 && i < (reservationEntities.size()%3)*2){
                keyboardSecondRow.add(new KeyboardButton(reservationEntities.get(i).getName() + " " + reservationEntities.get(i).getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm"))));
            }
            else {
                keyboardThirdRow.add(new KeyboardButton(reservationEntities.get(i).getName() + " " + reservationEntities.get(i).getDateTime().format(DateTimeFormatter.ofPattern("dd HH:mm"))));
            }
        }
        keyboard.add(keyboardFirstRow);
        if (reservationEntities.size() > 1) {
            keyboard.add(keyboardSecondRow);
            if (reservationEntities.size() > 2) {
                keyboard.add(keyboardThirdRow);
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
}
