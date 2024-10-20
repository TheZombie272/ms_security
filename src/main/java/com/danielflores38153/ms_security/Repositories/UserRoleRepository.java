package com.danielflores38153.ms_security.Repositories;

import com.danielflores38153.ms_security.Models.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    @Query("{'user.$id' : ObjectId(?0)}")
    public List<UserRole> getUserRolesByUserId(String userId);

    @Query("{'role.$id' : ObjectId(?0)}")
    public List<UserRole> getUserRolesByRoleId(String roleId);
}
