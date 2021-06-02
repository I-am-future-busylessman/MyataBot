package com.myata_bot.core.services;

import com.myata_bot.core.botapi.BotState;
import com.myata_bot.core.models.UserEntity;
import com.myata_bot.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    public void updateUser(UserEntity userEntity) {
        UserEntity userDB = userRepository.findByUserID(userEntity.getUserID());
        if (userDB == null)
            save(userEntity);
        else {
            userEntity.setId(userDB.getId());
            userRepository.save(userEntity);
        }
    }

    public UserEntity findUserByID(long userID) {
        return userRepository.findByUserID(userID);
    }

    public List<UserEntity> findAllAdmins(){
        return userRepository.findAllByRoleEquals("Admin");
    }

    public BotState getUserCurrentState(long userID) {
        return BotState.valueOf(userRepository.findByUserID(userID).getUserState());
    }

    public void setUserCurrentState(int userID, BotState userState) {
        UserEntity user =  userRepository.findByUserID(userID);
        if (user == null) {
            user = new UserEntity();
            user.setUserID(userID);
        }
        user.setState(userState.name());
        updateUser(user);
    }

    public void stepBack(int userID) {
        UserEntity user =  userRepository.findByUserID(userID);
        if (user == null) {
            user = new UserEntity();
            user.setUserID(userID);
        }
        user.stepBack();
        updateUser(user);
    }
}
