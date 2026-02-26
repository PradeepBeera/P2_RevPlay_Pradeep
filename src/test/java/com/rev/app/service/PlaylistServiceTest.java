package com.rev.app.service;

import com.rev.app.dto.request.PlaylistRequest;
import com.rev.app.dto.response.PlaylistResponse;
import com.rev.app.entity.Playlist;
import com.rev.app.entity.PlaylistSong;
import com.rev.app.entity.Song;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.PlaylistPrivacy;
import com.rev.app.entity.enums.Role;
import com.rev.app.entity.enums.Visibility;
import com.rev.app.exception.CustomException;
import com.rev.app.exception.UnauthorizedException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.PlaylistRepository;
import com.rev.app.repository.PlaylistSongRepository;
import com.rev.app.repository.SongRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.impl.PlaylistServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private PlaylistSongRepository playlistSongRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityDtoMapper mapper;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    private User testUser;
    private Playlist testPlaylist;

    @Before
    public void setUp() {
        testUser = User.builder().id(1L).email("user@email.com").username("testuser").role(Role.LISTENER).build();
        testPlaylist = Playlist.builder()
                .id(1L).name("My Playlist").privacy(PlaylistPrivacy.PRIVATE)
                .user(testUser).createdAt(LocalDateTime.now())
                .playlistSongs(new ArrayList<>())
                .build();
    }

    @Test
    public void createPlaylist_WithValidData_ReturnsPlaylistResponse() {
        PlaylistRequest request = new PlaylistRequest("My Playlist", "Cool songs", "PRIVATE");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(testUser));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(testPlaylist);

        PlaylistResponse expectedResponse = PlaylistResponse.builder().id(1L).name("My Playlist").build();
        when(mapper.toPlaylistResponseSummary(any(Playlist.class))).thenReturn(expectedResponse);

        PlaylistResponse response = playlistService.createPlaylist("user@email.com", request);

        assertNotNull(response);
        assertEquals("My Playlist", response.getName());
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test(expected = CustomException.class)
    public void addSongToPlaylist_WhenAlreadyExists_ThrowsConflict() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(testUser));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(1L, 1L)).thenReturn(true);

        playlistService.addSongToPlaylist(1L, 1L, "user@email.com");
    }

    @Test(expected = UnauthorizedException.class)
    public void deletePlaylist_ByNonOwner_ThrowsUnauthorized() {
        User otherUser = User.builder().id(2L).email("other@email.com").build();
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(testPlaylist));
        when(userRepository.findByEmail("other@email.com")).thenReturn(Optional.of(otherUser));

        playlistService.deletePlaylist(1L, "other@email.com");
    }

    @Test
    public void getUserPlaylists_ReturnsUserPlaylists() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(testUser));
        when(playlistRepository.findByUserId(1L)).thenReturn(List.of(testPlaylist));

        PlaylistResponse expectedResponse = PlaylistResponse.builder().id(1L).name("My Playlist").build();
        when(mapper.toPlaylistResponseSummary(any(Playlist.class))).thenReturn(expectedResponse);

        List<PlaylistResponse> result = playlistService.getUserPlaylists("user@email.com");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
