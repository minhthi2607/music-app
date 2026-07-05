package com.code.musicapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bang trung gian Playlist <-> Song (nhieu-nhieu). Dung entity rieng (thay vi @ManyToMany
 * thuan) de co them cot addedAt va de xoa/kiem tra tung dong don gian bang repository method.
 * Rang buoc unique (playlist_id, song_id) de 1 bai hat khong bi them trung vao cung 1 playlist.
 */
@Entity
@Table(name = "playlist_songs", uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "song_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}
