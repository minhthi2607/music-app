package com.code.musicapp.dto;

/**
 * DTO TAM THOI chi de hien thi du lieu mau ngoai trang chu.
 * Khi Nguoi 2 hoan thanh entity Song va SongRepository that,
 * HomeController se doi sang lay du lieu tu DB thay vi list hardcode nay.
 */
public record SampleSong(
        String title,
        String artist,
        String genre,
        String coverEmoji
) {
}
