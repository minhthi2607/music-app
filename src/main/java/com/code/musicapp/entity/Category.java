package com.code.musicapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vd: Pop, Rock, Ballad, EDM, Rap, Lo-fi...
    @NotBlank(message = "Ten the loai khong duoc de trong")
    @Size(max = 50)
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    // 1 category co nhieu song. mappedBy tro toi field "category" ben Song.
    // @JsonIgnore de tranh vong lap vo han neu sau nay serialize sang JSON (REST API).
    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Song> songs = new ArrayList<>();
}
