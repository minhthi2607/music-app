package com.code.musicapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Playlist ca nhan cua 1 USER. Moi playlist chi thuoc ve dung 1 chu so huu (owner),
 * khong chia se giua cac user, va ADMIN khong quan ly/can thiep vao playlist cua ai ca.
 */
@Entity
@Table(name = "playlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên Playlist không được để trống")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    // Chu so huu playlist
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
