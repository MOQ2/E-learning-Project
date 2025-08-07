package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.UserEntity;
import org.apache.catalina.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(long id);


    Page<UserEntity> findAll(Pageable pageable);
    Page<UserEntity> findByIsActiveTrue(Pageable pageable);



}
