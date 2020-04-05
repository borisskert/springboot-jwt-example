package de.borisskert.springjwt.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {

    Optional<UserEntity> findOneByUsername(String username);

    List<UserEntity> findAll();

    @Query("select u.password from UserEntity u where u.username = :username")
    Optional<String> findPasswordFor(@Param("username") String username);
}
