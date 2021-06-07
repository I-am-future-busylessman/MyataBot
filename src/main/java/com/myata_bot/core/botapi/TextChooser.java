package com.myata_bot.core.botapi;

import org.springframework.stereotype.Component;

@Component
public class TextChooser {

    public String chooseForPreviousState(BotState state){
        return switch (state){
            case MAIN_ADMIN -> null;
            case ASK_ADMIN_NAME -> null;
            case ASK_ADMIN_TO_DO -> null;
            case COLLECT_ADMIN_TO_DO -> null;
            case COLLECT_ADMIN_NAME -> null;
            case ASK_ADMIN_CONFIRM_RESERVATION -> null;
            case COLLECT_ADMIN_CONFIRM_RESERVATION -> null;
            case COLLECT_ADMIN_DELETE_RESERVATION -> null;
            case COLLECT_ADMIN_ARRIVED_RESERVATION -> null;
            case COLLECT_ADMIN_LEAVE_RESERVATION -> null;
            case FILLING_PROFILE -> null;
            case FILLING_RESERVATION -> null;
            case ASK_NAME -> null;
            case COLLECT_DATE_ASK_TIME -> "Подскажите на какую дату вы хотели бы забронировать столик?" +
                    "\n формат: 22.09";
            case COLLECT_TIME_ASK_AMOUNT_OF_PEOPLE -> "В какое время вам было бы удобнее всего нас посетить? \n Формат: 17:05";
            case COLLECT_AMOUNT_OF_PEOPLE_ASK_PHONE_NUMBER -> "На какое количество человек забронировать столик?";
            case COLLECT_PHONE_NUMBER_ASK_COMMENT -> "По какому номеру с вами возможно связаться?";
            case COLLECT_COMMENT_AND_COMPLETE -> "Если есть какие-то дополнительные пожелания, напишите их ниже.";
            case MAIN -> null;
            case ASK_TO_DO_FROM_UNKNOWN -> null;
            case ASK_TO_DO -> null;
            case COLLECT_TO_DO -> null;
            case COLLECTING_FEEDBACK -> null;
            case COLLECT_FEEDBACK_SCORE -> null;
            case COLLECT_FEEDBACK_COMMENT -> null;
        };
    }

    public String chooseForState(BotState state){
        return switch (state){
            case MAIN_ADMIN -> null;
            case ASK_ADMIN_NAME -> null;
            case ASK_ADMIN_TO_DO -> null;
            case COLLECT_ADMIN_TO_DO -> null;
            case COLLECT_ADMIN_NAME -> null;
            case ASK_ADMIN_CONFIRM_RESERVATION -> null;
            case COLLECT_ADMIN_CONFIRM_RESERVATION -> null;
            case COLLECT_ADMIN_DELETE_RESERVATION -> null;
            case COLLECT_ADMIN_ARRIVED_RESERVATION -> null;
            case COLLECT_ADMIN_LEAVE_RESERVATION -> null;
            case FILLING_PROFILE -> null;
            case FILLING_RESERVATION -> null;
            case ASK_NAME -> null;
            case COLLECT_DATE_ASK_TIME -> "В какое время вам было бы удобнее всего нас посетить? \n Формат: 17:05";
            case COLLECT_TIME_ASK_AMOUNT_OF_PEOPLE -> "На какое количество человек забронировать столик?";
            case COLLECT_AMOUNT_OF_PEOPLE_ASK_PHONE_NUMBER -> "По какому номеру с вами возможно связаться?";
            case COLLECT_PHONE_NUMBER_ASK_COMMENT -> "Если есть какие-то дополнительные пожелания, напишите их ниже.";
            case COLLECT_COMMENT_AND_COMPLETE -> "Спасибо что забронировали у нас столик! \n Ожидайте подтверждения.";
            case MAIN -> null;
            case ASK_TO_DO_FROM_UNKNOWN -> null;
            case ASK_TO_DO -> null;
            case COLLECT_TO_DO -> null;
            case COLLECTING_FEEDBACK -> null;
            case COLLECT_FEEDBACK_SCORE -> null;
            case COLLECT_FEEDBACK_COMMENT -> null;
        };
    }
}
