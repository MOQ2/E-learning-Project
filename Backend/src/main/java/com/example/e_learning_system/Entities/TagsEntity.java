package com.example.e_learning_system.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import com.example.e_learning_system.Config.TageColor;
import com.example.e_learning_system.Config.Tags;

@Getter
@Setter
public class TagsEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private Tags name;

    @Column(name = "description")
    private String description;

    @Column(name = "color", nullable = false)
    @Enumerated(EnumType.STRING)
    private TageColor color;


    @Override
    public String getEntityType() {
        return "TagsEntity";
    }
    @Override
    public boolean equals (Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TagsEntity)) return false;
        TagsEntity other = (TagsEntity) obj;
        return  this.getId() == other.getId() ;
    }


    @ManyToMany(mappedBy = "tags")
    private Set<Course> courses = new HashSet<>();
}
