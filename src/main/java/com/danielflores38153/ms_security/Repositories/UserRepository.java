package com.danielflores38153.ms_security.Repositories;
import com.danielflores38153.ms_security.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

}
