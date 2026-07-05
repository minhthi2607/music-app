package com.code.musicapp.controller;

import com.code.musicapp.entity.Song;
import com.code.musicapp.entity.SongStatus;
import com.code.musicapp.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    // So luong bai hat noi bat hien tren trang chu
    private static final int FEATURED_LIMIT = 8;

    private final SongRepository songRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("songs", featuredSongs());
        return "index";
    }

    // Lay bai hat da duyet (APPROVED), moi nhat truoc, gioi han so luong hien tren trang chu
    private List<Song> featuredSongs() {
        return songRepository.findByStatus(SongStatus.APPROVED).stream()
                .sorted(Comparator.comparing(Song::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(FEATURED_LIMIT)
                .toList();
    }
}

