package com.code.musicapp.repository;
import com.code.musicapp.entity.Song;
import com.code.musicapp.entity.SongStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // Loc bai hat theo trang thai - vd chi hien APPROVED ngoai trang public
    List<Song> findByStatus(SongStatus status);

    // Loc theo the loai
    List<Song> findByCategoryId(Long categoryId);

    // Bai hat cua 1 user da upload (dung cho ownership check / "bai hat cua toi")
    List<Song> findByUploaderId(Long uploaderId);

    // Tim kiem theo ten bai hat, khong phan biet hoa/thuong, khop mot phan
    // Vd: findByTitleContainingIgnoreCase("son tung") -> ra tat ca bai co "son tung" trong ten
    List<Song> findByTitleContainingIgnoreCase(String keyword);

    // Tim theo ten nghe si, khong phan biet hoa/thuong
    List<Song> findByArtistContainingIgnoreCase(String keyword);
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
            String title,
            String artist
    );

}
