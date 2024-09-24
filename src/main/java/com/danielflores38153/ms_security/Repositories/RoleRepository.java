package com.danielflores38153.ms_security.Repositories;

import com.danielflores38153.ms_security.Models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {
}
