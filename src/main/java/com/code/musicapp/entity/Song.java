package com.code.musicapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ten bai hat khong duoc de trong")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @Size(max = 100)
    @Column(length = 100)
    private String artist;

    // ManyToOne: nhieu bai hat thuoc 1 category. Cho phep null (bai hat co the chua phan loai).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Duong dan file nhac tren server (vd /uploads/songs/xxx.mp3) - Nguoi 2 tu quyet dinh
    // co luu local hay cloud storage, chi can dam bao gia tri nay la URL/path truy cap duoc.
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    // Anh bia, co the null -> hien anh mac dinh ngoai UI
    @Column(name = "cover_url")
    private String coverUrl;

    // Nguoi upload bai hat. NULL neu ADMIN them truc tiep (khong gan cho user cu the).
    // Neu USER tu upload, gan uploader = user dang dang nhap luc tao Song.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    // Mac dinh APPROVED vi chua co workflow duyet - xem ghi chu trong SongStatus.java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SongStatus status = SongStatus.APPROVED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SongStatus.APPROVED;
        }
    }
}
