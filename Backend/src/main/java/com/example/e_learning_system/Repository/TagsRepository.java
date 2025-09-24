package com.example.e_learning_system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_learning_system.Entities.TagsEntity;

@Repository
public interface TagsRepository extends JpaRepository<TagsEntity, Integer> {
    TagsEntity findByName(String name);
    
}
