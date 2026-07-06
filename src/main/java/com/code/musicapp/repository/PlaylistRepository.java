package com.code.musicapp.repository;

import com.code.musicapp.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // Danh sach playlist cua 1 user, moi tao truoc hien len dau ("Playlist cua toi")
    List<Playlist> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
