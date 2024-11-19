package com.danielflores38153.ms_security.Repositories;
import com.danielflores38153.ms_security.Models.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SessionRepository extends MongoRepository<Session, String> {
    @Query("{'user.$id' : ObjectId(?0)}")
    public List<Session> getSessionsByUserId(String userId);
}
