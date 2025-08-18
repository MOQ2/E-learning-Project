package com.example.e_learning_system.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends BaseEntity {

    //! add the relation uploaded by from the user
    //! alter the database using migration to have a file attribut to sote the file directly in the database
    //! add the relationships from the database(check out erd )
    @Column(name = "title" )
    private String title;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata;
    @Column(name = "file_url")
    private String url;
    @Column(name = "file_size_bytes")
    private long size;
    @Column(name = "file_type")
    private String fileType;
    @Column(name = "is_active")
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserEntity uploadedBy;


    @OneToMany(
            mappedBy = "attachment",
            fetch = FetchType.LAZY
    )
    private Set<VideoAttachments> videoAttachments = new HashSet<>();


}
