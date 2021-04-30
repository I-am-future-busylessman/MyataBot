package com.myata_bot.core.repository;

import com.myata_bot.core.models.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {
    UserEntity findByUserID(long userID);
    List<UserEntity> findAllByRoleEquals(String role);
}
