package com.code.musicapp.controller;

import com.code.musicapp.entity.Song;
import com.code.musicapp.entity.User;
import com.code.musicapp.exception.ResourceNotFoundException;
import com.code.musicapp.repository.CategoryRepository;
import com.code.musicapp.repository.SongRepository;
import com.code.musicapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongRepository songRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Chi chap nhan cac dinh dang nay khi upload, tranh nguoi dung day file .exe/.php doi ten thanh .mp3
    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of("audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg");
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @GetMapping
    public String listSongs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Model model
    ) {
        List<Song> songs;

        if (keyword != null && !keyword.trim().isEmpty()) {
            songs = songRepository
                    .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(keyword, keyword);
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
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai hat id=" + id));

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
            @Valid @ModelAttribute Song song,
            BindingResult bindingResult,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("coverFile") MultipartFile coverFile,
            Model model
    ) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "songs/upload";
        }

        if (audioFile.isEmpty() || !ALLOWED_AUDIO_TYPES.contains(audioFile.getContentType())) {
            bindingResult.reject("invalidAudio", "File nhac phai la mp3/wav/ogg");
            model.addAttribute("categories", categoryRepository.findAll());
            return "songs/upload";
        }
        if (!coverFile.isEmpty() && !ALLOWED_IMAGE_TYPES.contains(coverFile.getContentType())) {
            bindingResult.reject("invalidCover", "Anh bia phai la jpg/png/webp");
            model.addAttribute("categories", categoryRepository.findAll());
            return "songs/upload";
        }

        File audioDir = new File(System.getProperty("user.dir") + "/uploads/audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        File imageDir = new File(System.getProperty("user.dir") + "/uploads/images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        String audioName = UUID.randomUUID() + "_" + audioFile.getOriginalFilename();
        audioFile.transferTo(new File(audioDir, audioName));
        song.setFileUrl("/uploads/audio/" + audioName);

        if (!coverFile.isEmpty()) {
            String imageName = UUID.randomUUID() + "_" + coverFile.getOriginalFilename();
            coverFile.transferTo(new File(imageDir, imageName));
            song.setCoverUrl("/uploads/images/" + imageName);
        }

        // Gan nguoi dang upload - truoc day field nay luon bi bo trong (null)
        song.setUploader(getCurrentUser());

        songRepository.save(song);
        return "redirect:/songs";
    }

    @PostMapping("/delete/{id}")
    public String deleteSong(@PathVariable Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai hat id=" + id));

        if (song.getFileUrl() != null) {
            new File(System.getProperty("user.dir") + song.getFileUrl()).delete();
        }
        if (song.getCoverUrl() != null) {
            new File(System.getProperty("user.dir") + song.getCoverUrl()).delete();
        }

        songRepository.delete(song);
        return "redirect:/songs";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai hat id=" + id));

        model.addAttribute("song", song);
        model.addAttribute("categories", categoryRepository.findAll());
        return "songs/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSong(
            @PathVariable Long id,
            @Valid @ModelAttribute("song") Song updatedSong,
            BindingResult bindingResult,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("coverFile") MultipartFile coverFile,
            Model model
    ) throws IOException {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai hat id=" + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "songs/edit";
        }

        song.setTitle(updatedSong.getTitle());
        song.setArtist(updatedSong.getArtist());
        song.setCategory(updatedSong.getCategory());

        if (!audioFile.isEmpty()) {
            if (!ALLOWED_AUDIO_TYPES.contains(audioFile.getContentType())) {
                bindingResult.reject("invalidAudio", "File nhac phai la mp3/wav/ogg");
                model.addAttribute("categories", categoryRepository.findAll());
                return "songs/edit";
            }
            if (song.getFileUrl() != null) {
                new File(System.getProperty("user.dir") + song.getFileUrl()).delete();
            }
            String audioName = UUID.randomUUID() + "_" + audioFile.getOriginalFilename();
            File audioDir = new File(System.getProperty("user.dir") + "/uploads/audio");
            if (!audioDir.exists()) audioDir.mkdirs();
            audioFile.transferTo(new File(audioDir, audioName));
            song.setFileUrl("/uploads/audio/" + audioName);
        }

        if (!coverFile.isEmpty()) {
            if (!ALLOWED_IMAGE_TYPES.contains(coverFile.getContentType())) {
                bindingResult.reject("invalidCover", "Anh bia phai la jpg/png/webp");
                model.addAttribute("categories", categoryRepository.findAll());
                return "songs/edit";
            }
            if (song.getCoverUrl() != null) {
                new File(System.getProperty("user.dir") + song.getCoverUrl()).delete();
            }
            String imageName = UUID.randomUUID() + "_" + coverFile.getOriginalFilename();
            File imageDir = new File(System.getProperty("user.dir") + "/uploads/images");
            if (!imageDir.exists()) imageDir.mkdirs();
            coverFile.transferTo(new File(imageDir, imageName));
            song.setCoverUrl("/uploads/images/" + imageName);
        }

        songRepository.save(song);
        return "redirect:/songs";
    }

    // Lay User dang dang nhap tu SecurityContext (dua tren username trong JWT)
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user dang dang nhap: " + username));
    }
}