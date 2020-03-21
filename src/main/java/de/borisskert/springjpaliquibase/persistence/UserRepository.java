package de.borisskert.springjpaliquibase.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {

    Optional<UserEntity> findOneByUsername(String username);
}
