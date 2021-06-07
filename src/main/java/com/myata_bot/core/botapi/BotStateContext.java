package com.myata_bot.core.botapi;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotStateContext {
    private final Map<BotState, InputMessageHandler> messageHandlers = new HashMap<>();

    public BotStateContext(List<InputMessageHandler> messageHandlers) {
        messageHandlers.forEach(handler -> this.messageHandlers.put(handler.getHandlerName(), handler));
    }

    public List<BotApiMethod<?>> processInputMessage(BotState currentState, Message message) {
        System.out.println("here");
        InputMessageHandler currentMessageHandler = findMessageHandler(currentState);
        System.out.println(currentMessageHandler);
        return currentMessageHandler.handle(message);
    }

    private InputMessageHandler findMessageHandler(BotState currentState) {
        BotState currentBotState;
        currentBotState = getBotStateByProfileState(currentState);
        System.out.println();
        if(currentBotState.equals(BotState.FILLING_PROFILE)) {
            return messageHandlers.get(BotState.FILLING_PROFILE);
        }else if(currentBotState.equals(BotState.FILLING_RESERVATION)) {
            return messageHandlers.get(BotState.FILLING_RESERVATION);
        }else if(currentBotState.equals(BotState.MAIN_ADMIN)) {
            return messageHandlers.get(BotState.MAIN_ADMIN);
        }
        else {
            return messageHandlers.get(BotState.MAIN);
        }
    }

    private BotState getBotStateByProfileState(BotState currentState) {
        System.out.println(currentState);
        return switch (currentState) {
            case ASK_NAME -> BotState.FILLING_PROFILE;
            case COLLECT_DATE_ASK_TIME,
                    COLLECT_TIME_ASK_AMOUNT_OF_PEOPLE,
                    COLLECT_AMOUNT_OF_PEOPLE_ASK_PHONE_NUMBER,
                    COLLECT_COMMENT_AND_COMPLETE,
                    COLLECT_PHONE_NUMBER_ASK_COMMENT -> BotState.FILLING_RESERVATION;
            case ASK_ADMIN_NAME,
                    ASK_ADMIN_TO_DO,
                    COLLECT_ADMIN_TO_DO,
                    COLLECT_ADMIN_NAME,
                    ASK_ADMIN_CONFIRM_RESERVATION,
                    COLLECT_ADMIN_CONFIRM_RESERVATION,
                    COLLECT_ADMIN_ARRIVED_RESERVATION,
                    COLLECT_ADMIN_DELETE_RESERVATION -> BotState.MAIN_ADMIN;
            case COLLECT_FEEDBACK_SCORE, COLLECT_FEEDBACK_COMMENT -> BotState.COLLECTING_FEEDBACK;
            default -> BotState.MAIN;
        };
    }
}
