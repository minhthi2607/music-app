package com.code.musicapp.controller;

import com.code.musicapp.entity.Playlist;
import com.code.musicapp.entity.Song;
import com.code.musicapp.entity.SongStatus;
import com.code.musicapp.repository.PlaylistRepository;
import com.code.musicapp.repository.SongRepository;
import com.code.musicapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    // So bai hat hien thi tren moi trang cua trang chu
    private static final int PAGE_SIZE = 10;

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(@RequestParam(name = "page", defaultValue = "1") int page, Authentication authentication, Model model) {
        // "page" tren URL la 1-based cho de doc (?page=1, page=2...), Spring Data PageRequest can 0-based
        int safePage = Math.max(page, 1);
        PageRequest pageRequest = PageRequest.of(safePage - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Song> songPage = songRepository.findByStatus(SongStatus.APPROVED, pageRequest);

        model.addAttribute("songs", songPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", songPage.getTotalPages());
        model.addAttribute("hasPrevious", songPage.hasPrevious());
        model.addAttribute("hasNext", songPage.hasNext());

        // Chi nap danh sach playlist cua user khi da dang nhap, de hien dropdown
        // "them vao playlist" ngay tren trang chu (vi user thuong khong duoc vao /songs).
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                List<Playlist> userPlaylists = playlistRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId());
                model.addAttribute("userPlaylists", userPlaylists);
            });
        }

        return "index";
    }
}

