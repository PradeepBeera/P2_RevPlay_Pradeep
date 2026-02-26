package com.rev.app.service;

import com.rev.app.dto.request.SongUploadRequest;
import com.rev.app.dto.response.SongResponse;
import com.rev.app.entity.ArtistProfile;
import com.rev.app.entity.Song;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.entity.enums.Visibility;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.EntityDtoMapper;
import com.rev.app.repository.AlbumRepository;
import com.rev.app.repository.ArtistRepository;
import com.rev.app.repository.SongRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.repository.GenreRepository;
import com.rev.app.entity.Genre;
import com.rev.app.service.impl.SongServiceImpl;
import com.rev.app.util.FileStorageUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SongServiceTest {

    @Mock
    private SongRepository songRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private FileStorageUtil fileStorageUtil;
    @Mock
    private EntityDtoMapper mapper;

    @InjectMocks
    private SongServiceImpl songService;

    private User artistUser;
    private ArtistProfile artistProfile;
    private Song testSong;

    @Before
    public void setUp() {
        artistUser = User.builder().id(1L).email("artist@email.com").role(Role.ARTIST).build();
        artistProfile = ArtistProfile.builder().id(1L).user(artistUser).artistName("TestArtist").build();
        Genre testGenre = Genre.builder().id(1L).name("Pop").build();
        testSong = Song.builder()
                .id(1L).title("Test Song").genre(testGenre).duration(240)
                .audioUrl("/uploads/audio/test.mp3").visibility(Visibility.PUBLIC)
                .playCount(100L).artistProfile(artistProfile).releaseDate(LocalDate.now())
                .build();
    }

    @Test
    public void uploadSong_WithValidData_ReturnsSongResponse() {
        MockMultipartFile audioFile = new MockMultipartFile("audioFile", "song.mp3", "audio/mpeg", "audio".getBytes());
        SongUploadRequest request = new SongUploadRequest("Test Song", "Pop", 240, null, "PUBLIC", null);

        when(userRepository.findByEmail("artist@email.com")).thenReturn(Optional.of(artistUser));
        when(artistRepository.findByUserId(1L)).thenReturn(Optional.of(artistProfile));
        when(genreRepository.findByName("Pop")).thenReturn(Optional.of(testSong.getGenre()));
        when(fileStorageUtil.storeAudioFile(any())).thenReturn("/uploads/audio/uuid.mp3");
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        SongResponse expectedResponse = SongResponse.builder().id(1L).title("Test Song").build();
        when(mapper.toSongResponse(any(Song.class))).thenReturn(expectedResponse);

        SongResponse response = songService.uploadSong("artist@email.com", request, audioFile, null);

        assertNotNull(response);
        assertEquals("Test Song", response.getTitle());
        verify(songRepository).save(any(Song.class));
    }

    @Test
    public void getSongById_WithValidId_ReturnsSong() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        SongResponse expectedResponse = SongResponse.builder().id(1L).title("Test Song").build();
        when(mapper.toSongResponse(testSong)).thenReturn(expectedResponse);

        SongResponse response = songService.getSongById(1L);

        assertNotNull(response);
        assertEquals(Long.valueOf(1L), response.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void getSongById_WithInvalidId_ThrowsException() {
        when(songRepository.findById(999L)).thenReturn(Optional.empty());
        songService.getSongById(999L);
    }

    @Test
    public void searchSongs_ReturnsPagedResults() {
        Page<Song> songPage = new PageImpl<>(List.of(testSong));
        when(songRepository.searchPublicSongs(eq("pop"), eq(Visibility.PUBLIC), any(Pageable.class)))
                .thenReturn(songPage);

        SongResponse expectedResponse = SongResponse.builder().id(1L).title("Test Song").build();
        when(mapper.toSongResponse(any(Song.class))).thenReturn(expectedResponse);

        Page<SongResponse> result = songService.searchSongs("pop", 0, 20);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    public void incrementPlayCount_CallsRepository() {
        songService.incrementPlayCount(1L);
        verify(songRepository).incrementPlayCount(1L);
    }
}
