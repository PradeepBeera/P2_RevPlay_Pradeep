package com.rev.app.service.interfaces;

import com.rev.app.dto.request.PlaylistRequest;
import com.rev.app.dto.response.PlaylistResponse;

import java.util.List;

public interface PlaylistService {

    PlaylistResponse createPlaylist(String email, PlaylistRequest request);

    PlaylistResponse getPlaylistById(Long id);

    List<PlaylistResponse> getUserPlaylists(String email);

    PlaylistResponse addSongToPlaylist(Long playlistId, Long songId, String email);

    PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId, String email);

    PlaylistResponse reorderPlaylist(Long playlistId, List<Long> songIds, String email);

    void deletePlaylist(Long playlistId, String email);

    PlaylistResponse followPlaylist(Long playlistId, String email);

    void unfollowPlaylist(Long playlistId, String email);

    List<PlaylistResponse> getFollowedPlaylists(String email);

    boolean isFollowing(Long playlistId, String email);
}
