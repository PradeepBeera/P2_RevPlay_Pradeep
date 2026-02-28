package com.rev.app.service.interfaces;

import com.rev.app.dto.request.SongUploadRequest;
import com.rev.app.dto.response.SongResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SongService {

    SongResponse uploadSong(String email, SongUploadRequest request, MultipartFile audioFile, MultipartFile coverImage);

    SongResponse getSongById(Long id);

    Page<SongResponse> getPublicSongs(int page, int size);

    Page<SongResponse> searchSongs(String query, int page, int size);

    Page<SongResponse> getSongsByGenre(String genre, int page, int size);

    List<SongResponse> getSongsByArtist(Long artistId);

    List<SongResponse> getSongsByAlbum(Long albumId);

    void incrementPlayCount(Long songId);

    void deleteSong(Long songId, String email);
}
