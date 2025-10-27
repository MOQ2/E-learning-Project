package com.example.e_learning_system.Repository;

import com.example.e_learning_system.Entities.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    public Optional<UserEntity> findByEmail(String email);
    public Optional<UserEntity> findById(Integer id);


    public Page<UserEntity> findAll(Pageable pageable);
    public Page<UserEntity> findByIsActiveTrue(Pageable pageable);



}
