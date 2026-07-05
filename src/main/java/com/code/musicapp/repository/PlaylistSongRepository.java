package com.code.musicapp.repository;

import com.code.musicapp.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    // Danh sach bai hat trong 1 playlist, moi them vao gan day nhat hien len dau
    List<PlaylistSong> findByPlaylistIdOrderByAddedAtDesc(Long playlistId);

    // Dem so bai hat trong 1 playlist - dung de hien thi ngoai trang "Playlist cua toi"
    long countByPlaylistId(Long playlistId);

    // Kiem tra 1 bai hat da co trong playlist chua, tranh them trung
    boolean existsByPlaylistIdAndSongId(Long playlistId, Long songId);

    // Xoa 1 bai khoi playlist (khong xoa bai hat that su, chi go lien ket)
    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    // Xoa toan bo lien ket khi playlist bi xoa
    void deleteByPlaylistId(Long playlistId);
}
