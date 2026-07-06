package com.code.musicapp.controller;

import com.code.musicapp.entity.Playlist;
import com.code.musicapp.entity.PlaylistSong;
import com.code.musicapp.entity.Song;
import com.code.musicapp.entity.User;
import com.code.musicapp.exception.ResourceNotFoundException;
import com.code.musicapp.repository.PlaylistRepository;
import com.code.musicapp.repository.PlaylistSongRepository;
import com.code.musicapp.repository.SongRepository;
import com.code.musicapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Playlist ca nhan cua tung USER (nghe nhac). Moi nguoi chi xem/thao tac duoc tren
 * playlist cua chinh minh - KHONG co man hinh quan ly playlist danh cho ADMIN.
 * Da duoc bao ve o SecurityConfig: "/playlists/**" -> authenticated().
 */
@Controller
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    // ===== TRANG "PLAYLIST CUA TOI" =====
    @GetMapping
    public String myPlaylists(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Playlist> playlists = playlistRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId());

        // Dem so bai hat trong tung playlist de hien thi ngoai danh sach
        Map<Long, Long> songCounts = new HashMap<>();
        for (Playlist p : playlists) {
            songCounts.put(p.getId(), playlistSongRepository.countByPlaylistId(p.getId()));
        }

        model.addAttribute("playlists", playlists);
        model.addAttribute("songCounts", songCounts);
        return "playlists/list";
    }

    // ===== XEM CHI TIET 1 PLAYLIST + DANH SACH BAI HAT BEN TRONG =====
    @GetMapping("/{id}")
    public String viewPlaylist(@PathVariable Long id, Authentication authentication, Model model) {
        Playlist playlist = getOwnedPlaylistOrThrow(id, authentication);
        List<PlaylistSong> items = playlistSongRepository.findByPlaylistIdOrderByAddedAtDesc(playlist.getId());

        model.addAttribute("playlist", playlist);
        model.addAttribute("items", items);
        return "playlists/detail";
    }

    // ===== TAO PLAYLIST MOI (dung tu trang "Playlist cua toi") =====
    @PostMapping("/create")
    public String createPlaylist(@RequestParam String name, Authentication authentication) {
        if (name != null && !name.isBlank()) {
            createPlaylistForUser(getCurrentUser(authentication), name);
        }
        return "redirect:/playlists";
    }

    // ===== XOA CA PLAYLIST =====
    @Transactional
    @PostMapping("/{id}/delete")
    public String deletePlaylist(@PathVariable Long id, Authentication authentication) {
        Playlist playlist = getOwnedPlaylistOrThrow(id, authentication);
        playlistSongRepository.deleteByPlaylistId(playlist.getId());
        playlistRepository.delete(playlist);
        return "redirect:/playlists";
    }

    // ===== NUT "+" TREN BAI HAT: them vao playlist co san HOAC tao moi ngay tai cho =====
    // Nguoi dung hoac chon 1 playlist co san (playlistId), hoac go ten playlist moi (newPlaylistName).
    @Transactional
    @PostMapping("/add-song")
    public String addSongToPlaylist(
            @RequestParam Long songId,
            @RequestParam(required = false, defaultValue = "") String playlistId,
            @RequestParam(required = false, defaultValue = "") String newPlaylistName,
            Authentication authentication,
            HttpServletRequest request
    ) {
        User user = getCurrentUser(authentication);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay bai hat id=" + songId));

        Playlist playlist;
        if (!playlistId.isBlank()) {
            Long id;
            try {
                id = Long.parseLong(playlistId);
            } catch (NumberFormatException e) {
                return redirectBackWithParam(request, "/", "playlistError=invalid_playlist");
            }
            playlist = playlistRepository.findById(id)
                    .filter(p -> p.getOwner().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay playlist id=" + id));
        } else if (!newPlaylistName.isBlank()) {
            playlist = createPlaylistForUser(user, newPlaylistName);
        } else {
            return redirectBackWithParam(request, "/", "playlistError=empty_name");
        }

        if (!playlistSongRepository.existsByPlaylistIdAndSongId(playlist.getId(), song.getId())) {
            PlaylistSong ps = new PlaylistSong();
            ps.setPlaylist(playlist);
            ps.setSong(song);
            playlistSongRepository.save(ps);
        }

        return redirectBackWithParam(request, "/", "playlistAdded=" + playlist.getId());
    }

    // ===== XOA 1 BAI KHOI PLAYLIST (khong xoa bai hat that, chi go khoi playlist) =====
    @Transactional
    @PostMapping("/{playlistId}/songs/{songId}/remove")
    public String removeSongFromPlaylist(@PathVariable Long playlistId,
                                          @PathVariable Long songId,
                                          Authentication authentication) {
        getOwnedPlaylistOrThrow(playlistId, authentication); // chi de kiem tra quyen so huu
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
        return "redirect:/playlists/" + playlistId;
    }

    // ================= HELPER =================

    private static final int MAX_PLAYLIST_NAME_LENGTH = 100;

    private Playlist createPlaylistForUser(User user, String name) {
        String trimmed = name.trim();
        // @Size(max=100) tren entity khong tu kich hoat vi day la @RequestParam thuong,
        // khong qua @Valid -> tu cat bot de tranh DataIntegrityViolationException tu DB.
        if (trimmed.length() > MAX_PLAYLIST_NAME_LENGTH) {
            trimmed = trimmed.substring(0, MAX_PLAYLIST_NAME_LENGTH);
        }
        Playlist playlist = new Playlist();
        playlist.setName(trimmed);
        playlist.setOwner(user);
        return playlistRepository.save(playlist);
    }

    // Lay playlist theo id va kiem tra dung la cua user dang dang nhap, khong thi coi nhu khong ton tai
    private Playlist getOwnedPlaylistOrThrow(Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay playlist id=" + id));

        if (!playlist.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Khong tim thay playlist id=" + id);
        }
        return playlist;
    }

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay user dang dang nhap: " + username));
    }

    // Redirect nguoc lai trang truoc do (Referer), kem theo 1 param thong bao ket qua.
    // Xu ly dung truong hop URL truoc do da co "?" san (vd /songs?keyword=abc).
    private String redirectBackWithParam(HttpServletRequest request, String fallback, String param) {
        String referer = request.getHeader("Referer");
        String base = (referer != null && !referer.isBlank()) ? referer : fallback;
        String separator = base.contains("?") ? "&" : "?";
        return "redirect:" + base + separator + param;
    }
}
