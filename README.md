# RevPlay — Enterprise Music Streaming Platform

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A production-ready, monolithic music streaming web application built with **Spring Boot 3.x**, **Thymeleaf**, and **Spring Security (JWT)**. Features role-based access control (Listener / Artist / Admin), a custom HTML5 audio player with queue management, and a full artist analytics dashboard.

---

## Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database ERD](#database-erd)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Roles & Permissions](#roles--permissions)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Client (Browser)                 │
│          Thymeleaf SSR + HTML5 Audio Player          │
└────────────────────────┬────────────────────────────┘
                         │ HTTP / REST
┌────────────────────────▼────────────────────────────┐
│                   Controller Layer                   │
│   PageController (SSR) + REST Controllers (API)      │
├──────────────────────────────────────────────────────┤
│                    Service Layer                     │
│   AuthService, SongService, PlaylistService, etc.    │
├──────────────────────────────────────────────────────┤
│                   Repository Layer                   │
│              Spring Data JPA (Hibernate)             │
├──────────────────────────────────────────────────────┤
│                   Database Layer                     │
│             MySQL (prod) / H2 (dev/test)             │
└──────────────────────────────────────────────────────┘
```

**Cross-cutting concerns:**
- JWT Authentication Filter (stateless)
- Global Exception Handler (`@RestControllerAdvice`)
- Log4J2 (rolling file + console)
- ModelMapper (entity ↔ DTO)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.3 |
| Security | Spring Security 6 + JWT (jjwt 0.12.5) |
| ORM | Spring Data JPA (Hibernate 6) |
| Template Engine | Thymeleaf + Thymeleaf Extras Spring Security 6 |
| Database | Oracle, H2 (dev/test) |
| Build | Maven |
| Logging | Log4J2 |
| Mapping | ModelMapper 3.2 |
| Testing | JUnit 4 + Mockito |

---

## Project Structure

```
src/main/java/com/rev/app/
├── RevPlayApplication.java
├── config/
│   ├── AppConfig.java
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtConfig.java
│   ├── CustomUserDetailsService.java
│   └── ModelMapperConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ArtistController.java
│   ├── SongController.java
│   ├── AlbumController.java
│   ├── PlaylistController.java
│   ├── PlayerController.java
│   ├── AnalyticsController.java
│   └── PageController.java
├── dto/
│   ├── request/  (8 DTOs)
│   └── response/ (8 DTOs)
├── entity/
│   ├── enums/ (Role, Visibility, PlaylistPrivacy)
│   ├── User.java, ArtistProfile.java, Song.java
│   ├── Album.java, Playlist.java, PlaylistSong.java
│   ├── Favorite.java, ListeningHistory.java
├── exception/
│   ├── CustomException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── GlobalExceptionHandler.java
├── mapper/
│   └── EntityDtoMapper.java
├── repository/  (8 repositories)
├── service/
│   ├── interfaces/  (9 interfaces)
│   └── impl/        (9 implementations)
└── util/
    ├── Constants.java
    ├── JwtUtil.java
    └── FileStorageUtil.java

src/main/resources/
├── application.yml, application-dev.yml
├── application-prod.yml, application-test.yml
├── log4j2.xml
├── templates/  (17 Thymeleaf templates)
└── static/
    ├── css/app.css
    └── js/player.js, app.js, auth.js
```

---

## Database ERD

```
┌──────────┐     ┌───────────────┐     ┌──────────┐
│  users   │1───1│ artist_profiles│1───*│  songs   │
│──────────│     │───────────────│     │──────────│
│ id       │     │ id            │     │ id       │
│ email    │     │ user_id (FK)  │     │ title    │
│ username │     │ artist_name   │     │ genre    │
│ password │     │ genre         │     │ duration │
│ role     │     │ banner_image  │     │ audio_url│
│ display  │     │ socials...    │     │ artist_id│
│ bio      │     └───────────────┘     │ album_id │
│ created  │                           │ play_cnt │
└──────────┘                           └──────────┘
     │1                                     │*
     │                                      │
     │*            ┌──────────┐        ┌────┴──────┐
┌────┴──────┐      │ albums   │1───*│(songs)       │
│ playlists │      │──────────│     └──────────────┘
│───────────│      │ id       │
│ id        │      │ name     │
│ name      │      │ cover    │
│ privacy   │      │ artist_id│
│ user_id   │      └──────────┘
└───────────┘
     │1
     │*
┌────┴──────────┐     ┌───────────┐     ┌─────────────────┐
│ playlist_songs│     │ favorites │     │ listening_history│
│───────────────│     │───────────│     │─────────────────│
│ playlist_id   │     │ user_id   │     │ user_id         │
│ song_id       │     │ song_id   │     │ song_id         │
│ order_index   │     │ (unique)  │     │ played_at       │
└───────────────┘     └───────────┘     └─────────────────┘
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (for production profile)

### Quick Start (Dev — H2 in-memory)
```bash
git clone <repository-url>
cd P2-RevPlay
mvn spring-boot:run
```

Application starts at: **http://localhost:8080**

### Production (MySQL)
```bash
# 1. Create MySQL database
mysql -u root -p -e "CREATE DATABASE revplay;"

# 2. Set environment variables
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=revplay
export DB_USERNAME=root
export DB_PASSWORD=yourpassword

# 3. Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## API Documentation

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login | No |
| POST | `/api/auth/refresh` | Refresh JWT token | No |

### Users
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/users/me` | Get current user | Yes |
| PUT | `/api/users/me` | Update profile | Yes |
| POST | `/api/users/me/picture` | Upload profile picture | Yes |

### Songs
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/songs` | List public songs (paginated) | No |
| GET | `/api/songs/{id}` | Get song details | No |
| GET | `/api/songs/search?query=` | Search songs | No |
| GET | `/api/songs/genre/{genre}` | Filter by genre | No |
| POST | `/api/songs` | Upload song (multipart) | ARTIST |
| PUT | `/api/songs/{id}/play` | Increment play count | No |
| DELETE | `/api/songs/{id}` | Delete song | ARTIST (owner) |

### Albums
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/albums` | Create album | ARTIST |
| GET | `/api/albums/{id}` | Get album | No |
| GET | `/api/albums/artist/{id}` | Get artist albums | No |

### Playlists
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/playlists` | Create playlist | Yes |
| GET | `/api/playlists/my` | Get user playlists | Yes |
| GET | `/api/playlists/{id}` | Get playlist detail | Yes |
| POST | `/api/playlists/{id}/songs?songId=` | Add song | Yes (owner) |
| DELETE | `/api/playlists/{id}/songs/{songId}` | Remove song | Yes (owner) |
| PUT | `/api/playlists/{id}/reorder` | Reorder songs | Yes (owner) |
| DELETE | `/api/playlists/{id}` | Delete playlist | Yes (owner) |

### Player
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/player/history?songId=` | Record play | Yes |
| GET | `/api/player/history` | Get recent (50) | Yes |
| POST | `/api/player/favorites?songId=` | Toggle favorite | Yes |
| GET | `/api/player/favorites` | Get favorites | Yes |
| GET | `/api/player/favorites/check/{id}` | Check if favorited | Yes |

### Analytics
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/analytics/dashboard` | Artist dashboard | ARTIST |

---

## Roles & Permissions

| Role | Capabilities |
|------|-------------|
| **LISTENER** | Browse, search, create playlists, favorite songs, view history, upgrade to Artist |
| **ARTIST** | All Listener features + upload songs, create albums, view analytics dashboard |
| **ADMIN** | Full system access (future implementation) |

A Listener can upgrade to Artist by creating an artist profile from their profile page.

---

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=SongServiceTest
mvn test -Dtest=PlaylistServiceTest
```

### Test Coverage
- **AuthServiceTest** — Register success, duplicate email/username, login success, bad credentials
- **SongServiceTest** — Upload, get by ID, not-found, search, play count increment
- **PlaylistServiceTest** — Create, duplicate song detection, non-owner delete, user playlist retrieval

---

## Future Enhancements

- Admin panel for content moderation
- OAuth2 (Google, Spotify) social login
- Real-time notifications via WebSocket
- Cloud storage integration (AWS S3)
- Advanced search with Elasticsearch
- Collaborative playlists
- Song recommendations using ML
- Podcast support
- Mobile-responsive PWA
- Rate limiting and API throttling
