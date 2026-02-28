package com.rev.app.service.interfaces;

import com.rev.app.dto.request.AlbumRequest;
import com.rev.app.dto.response.AlbumResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AlbumService {

    AlbumResponse createAlbum(String email, AlbumRequest request, MultipartFile coverImage);

    AlbumResponse getAlbumById(Long id);

    List<AlbumResponse> getAlbumsByArtist(Long artistId);
}
