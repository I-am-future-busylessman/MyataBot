package com.myata_bot.core.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "user")
public class UserEntity {
    @Id
    String id;
    int userID;
    String name;
    String role;
    String userState;
    String previousState;

    public void setState(String state) {
        previousState = userState;
        userState = state;
    }

    public void stepBack() {
        userState = previousState;
    }
}
