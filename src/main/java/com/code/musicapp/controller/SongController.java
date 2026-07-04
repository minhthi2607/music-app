package com.code.musicapp.controller;

import com.code.musicapp.entity.Song;
import com.code.musicapp.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.code.musicapp.repository.CategoryRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongRepository songRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String listSongs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Model model
    ) {
        List<Song> songs;

        if (keyword != null && !keyword.trim().isEmpty()) {
            songs = songRepository
                    .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                            keyword,
                            keyword
                    );
        } else if (categoryId != null) {
            songs = songRepository.findByCategoryId(categoryId);
        } else {
            songs = songRepository.findAll();
        }

        model.addAttribute("songs", songs);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        return "songs/list";
    }
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        model.addAttribute("song", song);

        return "songs/detail";
    }
    @GetMapping("/upload")
    public String uploadForm(Model model) {

        model.addAttribute("song", new Song());
        model.addAttribute("categories", categoryRepository.findAll());

        return "songs/upload";
    }
    @PostMapping("/upload")
    public String uploadSong(
            @ModelAttribute Song song,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("coverFile") MultipartFile coverFile
    ) throws IOException {

        File audioDir = new File(System.getProperty("user.dir") + "/uploads/audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        File imageDir = new File(System.getProperty("user.dir") + "/uploads/images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        if (!audioFile.isEmpty()) {
            String audioName = UUID.randomUUID() + "_" + audioFile.getOriginalFilename();

            File audioPath = new File(audioDir, audioName);
            audioFile.transferTo(audioPath);

            song.setFileUrl("/uploads/audio/" + audioName);
        }

        if (!coverFile.isEmpty()) {
            String imageName = UUID.randomUUID() + "_" + coverFile.getOriginalFilename();

            File imagePath = new File(imageDir, imageName);
            coverFile.transferTo(imagePath);

            song.setCoverUrl("/uploads/images/" + imageName);
        }

        songRepository.save(song);

        return "redirect:/songs";
    }
    @GetMapping("/delete/{id}")
    public String deleteSong(@PathVariable Long id) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (song.getFileUrl() != null) {
            File audioFile = new File(System.getProperty("user.dir") + song.getFileUrl());
            if (audioFile.exists()) {
                audioFile.delete();
            }
        }

        if (song.getCoverUrl() != null) {
            File coverFile = new File(System.getProperty("user.dir") + song.getCoverUrl());
            if (coverFile.exists()) {
                coverFile.delete();
            }
        }

        songRepository.delete(song);

        return "redirect:/songs";
    }
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        model.addAttribute("song", song);
        model.addAttribute("categories", categoryRepository.findAll());

        return "songs/edit";
    }
    @PostMapping("/update/{id}")
    public String updateSong(
            @PathVariable Long id,
            @ModelAttribute Song updatedSong,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("coverFile") MultipartFile coverFile
    ) throws IOException {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // Cập nhật thông tin cơ bản
        song.setTitle(updatedSong.getTitle());
        song.setArtist(updatedSong.getArtist());
        song.setCategory(updatedSong.getCategory());

        // ==========================
        // Cập nhật file MP3 nếu có
        // ==========================
        if (!audioFile.isEmpty()) {

            // Xóa file cũ
            if (song.getFileUrl() != null) {
                File oldAudio = new File(System.getProperty("user.dir") + song.getFileUrl());
                if (oldAudio.exists()) {
                    oldAudio.delete();
                }
            }

            String audioName = UUID.randomUUID() + "_" + audioFile.getOriginalFilename();

            File audioDir = new File(System.getProperty("user.dir") + "/uploads/audio");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }

            audioFile.transferTo(new File(audioDir, audioName));

            song.setFileUrl("/uploads/audio/" + audioName);
        }

        // ==========================
        // Cập nhật ảnh bìa nếu có
        // ==========================
        if (!coverFile.isEmpty()) {

            if (song.getCoverUrl() != null) {
                File oldCover = new File(System.getProperty("user.dir") + song.getCoverUrl());
                if (oldCover.exists()) {
                    oldCover.delete();
                }
            }

            String imageName = UUID.randomUUID() + "_" + coverFile.getOriginalFilename();

            File imageDir = new File(System.getProperty("user.dir") + "/uploads/images");
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            coverFile.transferTo(new File(imageDir, imageName));

            song.setCoverUrl("/uploads/images/" + imageName);
        }

        songRepository.save(song);

        return "redirect:/songs";
    }
}