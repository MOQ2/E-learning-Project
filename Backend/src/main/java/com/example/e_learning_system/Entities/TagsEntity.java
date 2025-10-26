package com.example.e_learning_system.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;



@Getter
@Setter
@Entity(name = "tags")
public class TagsEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "color", nullable = false)
    private String color;

    @ManyToMany(mappedBy = "tags")
    private Set<Course> courses = new HashSet<>();

    public TagsEntity() {
    }

    public TagsEntity(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }

    @Override
    public String getEntityType() {
        return "TagsEntity";
    }


    @Override
    public boolean equals (Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TagsEntity)) return false;
        TagsEntity other = (TagsEntity) obj;
        return  this.name.equals(other.getName());
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
