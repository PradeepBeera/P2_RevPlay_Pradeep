package com.rev.app.mapper;

import com.rev.app.dto.response.*;
import com.rev.app.entity.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    private final ModelMapper modelMapper;

    public EntityDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ArtistProfileResponse toArtistProfileResponse(ArtistProfile profile) {
        return ArtistProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .artistName(profile.getArtistName())
                .genre(profile.getGenre() != null ? profile.getGenre().getName() : null)
                .bannerImage(profile.getBannerImage())
                .instagramLink(profile.getInstagramLink())
                .twitterLink(profile.getTwitterLink())
                .youtubeLink(profile.getYoutubeLink())
                .spotifyLink(profile.getSpotifyLink())
                .websiteLink(profile.getWebsiteLink())
                .build();
    }

    public SongResponse toSongResponse(Song song) {
        SongResponse.SongResponseBuilder builder = SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .genre(song.getGenre() != null ? song.getGenre().getName() : null)
                .duration(song.getDuration())
                .releaseDate(song.getReleaseDate())
                .audioUrl(song.getAudioUrl())
                .coverImage(song.getCoverImage())
                .visibility(song.getVisibility().name())
                .status(song.getStatus())
                .fileSize(song.getFileSize())
                .playCount(song.getPlayCount());

        if (song.getArtistProfile() != null) {
            builder.artistName(song.getArtistProfile().getArtistName())
                    .artistId(song.getArtistProfile().getId());
        }

        if (song.getAlbum() != null) {
            builder.albumName(song.getAlbum().getName())
                    .albumId(song.getAlbum().getId());
        }

        return builder.build();
    }

    public List<SongResponse> toSongResponseList(List<Song> songs) {
        if (songs == null)
            return Collections.emptyList();
        return songs.stream().map(this::toSongResponse).collect(Collectors.toList());
    }

    public AlbumResponse toAlbumResponse(Album album) {
        return AlbumResponse.builder()
                .id(album.getId())
                .name(album.getName())
                .description(album.getDescription())
                .releaseDate(album.getReleaseDate())
                .coverImage(album.getCoverImage())
                .artistName(album.getArtistProfile().getArtistName())
                .artistId(album.getArtistProfile().getId())
                .songs(toSongResponseList(album.getSongs()))
                .build();
    }

    public PlaylistResponse toPlaylistResponse(Playlist playlist, List<Song> songs) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .privacy(playlist.getPrivacy().name())
                .userId(playlist.getUser().getId())
                .ownerName(playlist.getUser().getDisplayName() != null
                        ? playlist.getUser().getDisplayName()
                        : playlist.getUser().getUsername())
                .createdAt(playlist.getCreatedAt())
                .songCount(songs != null ? songs.size() : 0)
                .followerCount(playlist.getFollowers() != null ? (long) playlist.getFollowers().size() : 0L)
                .songs(toSongResponseList(songs))
                .build();
    }

    public PlaylistResponse toPlaylistResponseSummary(Playlist playlist) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .privacy(playlist.getPrivacy().name())
                .userId(playlist.getUser().getId())
                .ownerName(playlist.getUser().getDisplayName() != null
                        ? playlist.getUser().getDisplayName()
                        : playlist.getUser().getUsername())
                .createdAt(playlist.getCreatedAt())
                .songCount(playlist.getPlaylistSongs() != null ? playlist.getPlaylistSongs().size() : 0)
                .followerCount(playlist.getFollowers() != null ? (long) playlist.getFollowers().size() : 0L)
                .build();
    }

    public PodcastResponse toPodcastResponse(Podcast podcast) {
        return PodcastResponse.builder()
                .id(podcast.getId())
                .title(podcast.getTitle())
                .description(podcast.getDescription())
                .coverImage(podcast.getCoverImage())
                .artistName(podcast.getArtistProfile().getArtistName())
                .artistId(podcast.getArtistProfile().getId())
                .createdAt(podcast.getCreatedAt())
                .episodes(toPodcastEpisodeResponseList(podcast.getEpisodes()))
                .build();
    }

    public PodcastEpisodeResponse toPodcastEpisodeResponse(PodcastEpisode episode) {
        return PodcastEpisodeResponse.builder()
                .id(episode.getId())
                .podcastId(episode.getPodcast().getId())
                .title(episode.getTitle())
                .description(episode.getDescription())
                .audioUrl(episode.getAudioUrl())
                .duration(episode.getDuration())
                .releaseDate(episode.getReleaseDate())
                .createdAt(episode.getCreatedAt())
                .build();
    }

    public List<PodcastEpisodeResponse> toPodcastEpisodeResponseList(List<PodcastEpisode> episodes) {
        if (episodes == null)
            return Collections.emptyList();
        return episodes.stream().map(this::toPodcastEpisodeResponse).collect(Collectors.toList());
    }

    public List<PodcastResponse> toPodcastResponseList(List<Podcast> podcasts) {
        if (podcasts == null)
            return Collections.emptyList();
        return podcasts.stream().map(this::toPodcastResponse).collect(Collectors.toList());
    }
}
