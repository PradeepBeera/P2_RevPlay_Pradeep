package com.rev.app.controller;

import com.rev.app.dto.response.SongResponse;
import com.rev.app.entity.User;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.interfaces.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * PageController handles all the main page navigation for the RevPlay
 * application.
 * It connects the backend services with the frontend Thymeleaf templates.
 */
@Controller
public class PageController {

    // Services used to fetch data from the database
    private final SongService songService;
    private final AlbumService albumService;
    private final ArtistService artistService;
    private final PlaylistService playlistService;
    private final FavoriteService favoriteService;
    private final HistoryService historyService;
    private final AnalyticsService analyticsService;
    private final UserService userService;
    private final PodcastService podcastService;
    private final UserRepository userRepository;

    // Constructor-based dependency injection (The standard Spring way)
    public PageController(SongService songService, AlbumService albumService,
            ArtistService artistService, PlaylistService playlistService,
            FavoriteService favoriteService, HistoryService historyService,
            AnalyticsService analyticsService, UserService userService,
            PodcastService podcastService, UserRepository userRepository) {
        this.songService = songService;
        this.albumService = albumService;
        this.artistService = artistService;
        this.playlistService = playlistService;
        this.favoriteService = favoriteService;
        this.historyService = historyService;
        this.analyticsService = analyticsService;
        this.userService = userService;
        this.podcastService = podcastService;
        this.userRepository = userRepository;
    }

    /**
     * Home page: Shows featured songs and artists.
     */
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Fetch the first 12 public songs
        Page<SongResponse> songs = songService.getPublicSongs(0, 12);
        model.addAttribute("songs", songs.getContent());
        model.addAttribute("artists", artistService.getAllArtists());

        // Helper method to add user info (for sidebar/header)
        addUserToModel(model, userDetails);
        return "index";
    }

    /**
     * Auth pages: Simple redirects to templates
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Browse page: Shows all public songs with pagination support.
     */
    @GetMapping("/browse")
    public String browsePage(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<SongResponse> songs = songService.getPublicSongs(page, size);
        model.addAttribute("songs", songs);
        addUserToModel(model, userDetails);
        return "browse";
    }

    /**
     * Search page: Filters songs and artists based on user query.
     */
    @GetMapping("/search")
    public String searchPage(Model model,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (query != null && !query.isBlank()) {
            model.addAttribute("songs", songService.searchSongs(query, page, 20));
            model.addAttribute("artists", artistService.searchArtists(query));
            model.addAttribute("query", query);
        }
        addUserToModel(model, userDetails);
        return "search";
    }

    /**
     * Detail pages: Show specific information for a song, album, or artist.
     */
    @GetMapping("/song/{id}")
    public String songDetail(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("song", songService.getSongById(id));
        addUserToModel(model, userDetails);
        return "song-detail";
    }

    @GetMapping("/album/{id}")
    public String albumDetail(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("album", albumService.getAlbumById(id));
        addUserToModel(model, userDetails);
        return "album-detail";
    }

    @GetMapping("/artist/{id}")
    public String artistDetail(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("artist", artistService.getProfileById(id));
        model.addAttribute("songs", songService.getSongsByArtist(id));
        model.addAttribute("albums", albumService.getAlbumsByArtist(id));
        addUserToModel(model, userDetails);
        return "artist-detail";
    }

    /**
     * Podcasts: Discovery for spoken word content.
     */
    @GetMapping("/podcasts")
    public String podcastsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("podcasts", podcastService.getAllPodcasts());
        addUserToModel(model, userDetails);
        return "podcasts";
    }

    @GetMapping("/podcast/{id}")
    public String podcastDetail(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("podcast", podcastService.getPodcastById(id));
        addUserToModel(model, userDetails);
        return "podcast-detail";
    }

    /**
     * User Library: Authenticated-only sections for personal content.
     */
    @GetMapping("/my/playlists")
    public String myPlaylists(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("playlists", playlistService.getUserPlaylists(userDetails.getUsername()));
        addUserToModel(model, userDetails);
        return "playlists";
    }

    @GetMapping("/my/playlist/{id}")
    public String playlistDetail(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("playlist", playlistService.getPlaylistById(id));
        addUserToModel(model, userDetails);
        return "playlist-detail";
    }

    @GetMapping("/my/favorites")
    public String myFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("favorites", favoriteService.getUserFavorites(userDetails.getUsername()));
        addUserToModel(model, userDetails);
        return "favorites";
    }

    @GetMapping("/my/history")
    public String myHistory(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("history", historyService.getRecentlyPlayed(userDetails.getUsername()));
        addUserToModel(model, userDetails);
        return "history";
    }

    @GetMapping("/my/profile")
    public String myProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("profile", userService.getCurrentUser(userDetails.getUsername()));
        addUserToModel(model, userDetails);
        return "profile";
    }

    /**
     * Artist Dashboard: Strictly for users with the ARTIST role.
     */
    @GetMapping("/dashboard")
    public String artistDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("analytics", analyticsService.getArtistAnalytics(userDetails.getUsername()));
        addUserToModel(model, userDetails);
        return "dashboard";
    }

    @GetMapping("/dashboard/upload")
    public String uploadSong(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user != null && user.getArtistProfile() != null) {
            model.addAttribute("albums", albumService.getAlbumsByArtist(user.getArtistProfile().getId()));
        }
        addUserToModel(model, userDetails);
        return "upload";
    }

    @GetMapping("/dashboard/albums")
    public String manageAlbums(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user != null && user.getArtistProfile() != null) {
            model.addAttribute("albums", albumService.getAlbumsByArtist(user.getArtistProfile().getId()));
        }
        addUserToModel(model, userDetails);
        return "albums-manage";
    }

    /**
     * Utility method to inject the 'currentUser' and 'isArtist' status into the
     * view.
     * This is picked up by layout.html to conditionally render sidebar items.
     */
    private void addUserToModel(Model model, UserDetails userDetails) {
        if (userDetails != null) {
            Optional<User> user = userRepository.findByEmail(userDetails.getUsername());
            user.ifPresent(u -> {
                model.addAttribute("currentUser", u);
                model.addAttribute("isArtist", u.getArtistProfile() != null);
            });
        }
    }
}
