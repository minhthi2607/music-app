package com.code.musicapp.controller;

import com.code.musicapp.dto.SampleSong;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    // Trang chu tam thoi - nguoi 4 (UI) se thay the/mo rong sau
    // Du lieu mau (hardcode) chi de co giao dien de nhin khi demo,
    // Nguoi 2 se thay bang du lieu that tu SongRepository khi xong module Song.
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("songs", sampleSongs());
        return "index";
    }

    private List<SampleSong> sampleSongs() {
        return List.of(
                new SampleSong("Chạy Ngay Đi", "Sơn Tùng M-TP", "V-Pop", "🎤"),
                new SampleSong("Có Chàng Trai Viết Lên Cây", "Phan Mạnh Quỳnh", "Ballad", "🎸"),
                new SampleSong("Blinding Lights", "The Weeknd", "Synth-Pop", "✨"),
                new SampleSong("Shape of You", "Ed Sheeran", "Pop", "🎧"),
                new SampleSong("Lạc Trôi", "Sơn Tùng M-TP", "V-Pop", "🌙"),
                new SampleSong("Levitating", "Dua Lipa", "Dance-Pop", "💃"),
                new SampleSong("Waiting For You", "MONO", "V-Pop", "🌊"),
                new SampleSong("Perfect", "Ed Sheeran", "Ballad", "💍")
        );
    }
}
