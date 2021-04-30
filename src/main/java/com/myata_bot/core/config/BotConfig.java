package com.myata_bot.core.config;

import com.myata_bot.core.MyataBot;
import com.myata_bot.core.botapi.TelegramFacade;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "telegrambot")
public class BotConfig {
    private String botUserName;
    private String botToken;

    @Bean
    public MyataBot myataBot(TelegramFacade telegramFacade) {
        DefaultBotOptions options = ApiContext.getInstance(DefaultBotOptions.class);

        MyataBot telgramBot = new MyataBot(options, telegramFacade);
        telgramBot.setBotUserName(botUserName);
        telgramBot.setBotToken(botToken);

        return telgramBot;
    }
}
