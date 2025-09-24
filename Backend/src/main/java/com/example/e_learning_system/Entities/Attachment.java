package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "attachments")
@Getter // ADD THIS
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends BaseEntity {

    //! done :add the relation uploaded by from the user
    //! done :alter the database using migration to have a file attribut to sote the file directly in the database
    //! done :add the relationships from the database(check out erd )
    @Column(name = "title" )
    private String title;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "data",columnDefinition = "BYTEA")
    private byte[] fileData;

    @Column(name = "is_active")
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserEntity uploadedBy;



    @OneToMany(
            mappedBy = "attachment",
            fetch = FetchType.LAZY
    )
    private Set<VideoAttachments> videoAttachments = new HashSet<>();

    // TODO change this when finish the controller
    public String getFileDownloadUrl() {
        return " ";
    }


    public long getSize() {
        Object sizeValue = this.metadata.get("size");
        if (sizeValue instanceof Number) {
            return ((Number) sizeValue).longValue();
        }
        return 0L;
    }



}
