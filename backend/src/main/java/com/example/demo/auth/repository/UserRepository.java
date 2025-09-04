package com.example.demo.auth.repository;

import com.example.demo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    User findByUsername(String username);

    Optional<User> findByUsernameAndIsLockAndIsSocial(String username, Boolean isLock, Boolean isSocial);
}
