package com.rev.app.util;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String API_PREFIX = "/api";
    public static final String AUTH_PREFIX = API_PREFIX + "/auth";
    public static final String USERS_PREFIX = API_PREFIX + "/users";
    public static final String ARTISTS_PREFIX = API_PREFIX + "/artists";
    public static final String SONGS_PREFIX = API_PREFIX + "/songs";
    public static final String ALBUMS_PREFIX = API_PREFIX + "/albums";
    public static final String PLAYLISTS_PREFIX = API_PREFIX + "/playlists";
    public static final String PLAYER_PREFIX = API_PREFIX + "/player";
    public static final String ANALYTICS_PREFIX = API_PREFIX + "/analytics";

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_HISTORY_SIZE = 50;
    public static final int TOP_SONGS_LIMIT = 10;
    public static final int TOP_LISTENERS_LIMIT = 10;

    public static final String TOKEN_TYPE = "Bearer";
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String ROLE_LISTENER = "LISTENER";
    public static final String ROLE_ARTIST = "ARTIST";
    public static final String ROLE_ADMIN = "ADMIN";
}
