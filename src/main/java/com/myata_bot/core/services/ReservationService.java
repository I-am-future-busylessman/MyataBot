package com.myata_bot.core.services;

import com.myata_bot.core.models.ReservationEntity;
import com.myata_bot.core.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository resRepo;

    public ReservationService(ReservationRepository resRepo) {
        this.resRepo = resRepo;
    }

    public void save(ReservationEntity reservation) {
        resRepo.save(reservation);
    }

    public void delete(int userID) {
        resRepo.deleteByUserID(userID);
    }

    public ReservationEntity findByUserID(int userID){
        return resRepo.findByUserIDAndDoneIsFalse(userID);
    }

    public List<ReservationEntity> findAll() {
        return resRepo.findAll();
    }

    public List<ReservationEntity> findAllUnconfirmed () {
        return resRepo.findAllByConfirmedIsFalse();
    }

    public List<ReservationEntity> findAllUndone () {return resRepo.findAllByDoneIsFalse();}

    public List<ReservationEntity> findAllNearest() {
        return resRepo.findAllByDateTimeBetween(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(1));
    }

    public String getReservationInfo(int userId){
        ReservationEntity reservationEntity = findByUserID(userId);
        String result = "";
        result += "Бронь на имя: " + reservationEntity.getName() + "\n";
        result += "Дата: " + reservationEntity.getDateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) + "\n";
        result += "Номер телефона: " + reservationEntity.getPhoneNumber() + "\n";
        result += "Кол-во гостей: " + reservationEntity.getAmountOfPeople() + "\n";
        result += "Комментарий: " + reservationEntity.getComment() + "\n";
        return result;
    }
}
