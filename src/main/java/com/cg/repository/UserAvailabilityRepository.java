package com.cg.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cg.user.bo.UserAvailability;

@Repository
public interface UserAvailabilityRepository extends CrudRepository<UserAvailability,Long>{

}
