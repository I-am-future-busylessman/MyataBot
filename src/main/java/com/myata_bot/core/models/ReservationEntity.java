package com.myata_bot.core.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "reservation")
public class ReservationEntity {
    @Id
    String id;
    int userID;
    String name;
    LocalDateTime dateTime;
    String phoneNumber;
    String amountOfPeople;
    String comment;
    boolean completed = false;
    boolean confirmed = false;
    boolean done = false;
    String confirmedBy;
}
