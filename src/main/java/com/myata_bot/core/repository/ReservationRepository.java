package com.myata_bot.core.repository;

import com.myata_bot.core.models.ReservationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository  extends MongoRepository<ReservationEntity, String> {
    List<ReservationEntity> findAllByConfirmedIsFalse();
    ReservationEntity findByUserIDAndDoneIsFalse(int userID);
    void deleteByUserID(int userID);
    List<ReservationEntity> findAllByDoneIsFalse();
    List<ReservationEntity> findAllByDateTimeBetween(LocalDateTime first, LocalDateTime second);
}
