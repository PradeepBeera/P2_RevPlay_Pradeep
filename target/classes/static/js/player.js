const audioElement = document.getElementById('audio-element');
let playerQueue = [];
let currentIndex = -1;
let isShuffled = false;
let repeatMode = 0; // 0=off, 1=all, 2=one
let isMuted = false;
let previousVolume = 80;

function playSong(id, title, artist, audioUrl, coverImage) {
    const track = { id, title, artist, audioUrl, coverImage };
    const existingIndex = playerQueue.findIndex(t => t.id === id);

    if (existingIndex >= 0) {
        currentIndex = existingIndex;
    } else {
        playerQueue.push(track);
        currentIndex = playerQueue.length - 1;
    }

    loadTrack(track);
    audioElement.play();
    recordPlay(id);
}

function addToQueue(id, title, artist, audioUrl, coverImage) {
    const track = { id, title, artist, audioUrl, coverImage };
    if (!playerQueue.find(t => t.id === id)) {
        playerQueue.push(track);
        renderQueue();
    }
}

function loadTrack(track) {
    const player = document.getElementById('music-player');
    player.style.display = 'flex';

    document.getElementById('player-title').textContent = track.title;
    document.getElementById('player-artist').textContent = track.artist;

    const cover = document.getElementById('player-cover');
    cover.src = track.coverImage || '';
    cover.style.display = track.coverImage ? 'block' : 'none';

    audioElement.src = track.audioUrl;
    document.getElementById('play-icon').className = 'fas fa-pause';
    renderQueue();
}

function togglePlay() {
    if (!audioElement.src) return;

    if (audioElement.paused) {
        audioElement.play();
        document.getElementById('play-icon').className = 'fas fa-pause';
    } else {
        audioElement.pause();
        document.getElementById('play-icon').className = 'fas fa-play';
    }
}

function playNext() {
    if (playerQueue.length === 0) return;

    if (isShuffled) {
        currentIndex = Math.floor(Math.random() * playerQueue.length);
    } else {
        currentIndex = (currentIndex + 1) % playerQueue.length;
    }

    loadTrack(playerQueue[currentIndex]);
    audioElement.play();
}

function playPrevious() {
    if (playerQueue.length === 0) return;

    if (audioElement.currentTime > 3) {
        audioElement.currentTime = 0;
        return;
    }

    currentIndex = (currentIndex - 1 + playerQueue.length) % playerQueue.length;
    loadTrack(playerQueue[currentIndex]);
    audioElement.play();
}

function toggleShuffle() {
    isShuffled = !isShuffled;
    document.getElementById('shuffle-icon').parentElement.classList.toggle('active', isShuffled);
}

function toggleRepeat() {
    repeatMode = (repeatMode + 1) % 3;
    const icon = document.getElementById('repeat-icon');
    icon.parentElement.classList.toggle('active', repeatMode > 0);

    if (repeatMode === 2) {
        icon.className = 'fas fa-repeat';
        icon.parentElement.setAttribute('title', 'Repeat one');
    } else {
        icon.className = 'fas fa-repeat';
        icon.parentElement.setAttribute('title', repeatMode === 1 ? 'Repeat all' : 'Repeat off');
    }
}

function seekTo(event) {
    if (!audioElement.duration) return;
    const bar = document.getElementById('progress-bar');
    const rect = bar.getBoundingClientRect();
    const fraction = (event.clientX - rect.left) / rect.width;
    audioElement.currentTime = fraction * audioElement.duration;
}

function setVolume(value) {
    audioElement.volume = value / 100;
    updateVolumeIcon(value);
}

function toggleMute() {
    if (isMuted) {
        const slider = document.getElementById('volume-slider');
        audioElement.volume = previousVolume / 100;
        slider.value = previousVolume;
        isMuted = false;
        updateVolumeIcon(previousVolume);
    } else {
        previousVolume = document.getElementById('volume-slider').value;
        audioElement.volume = 0;
        document.getElementById('volume-slider').value = 0;
        isMuted = true;
        updateVolumeIcon(0);
    }
}

function updateVolumeIcon(value) {
    const icon = document.getElementById('volume-icon');
    if (value == 0) icon.className = 'fas fa-volume-xmark';
    else if (value < 40) icon.className = 'fas fa-volume-low';
    else icon.className = 'fas fa-volume-high';
}

function toggleQueue() {
    const panel = document.getElementById('queue-panel');
    panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
    renderQueue();
}

function renderQueue() {
    const list = document.getElementById('queue-list');
    if (!list) return;

    list.innerHTML = playerQueue.map((track, i) => {
        const activeClass = i === currentIndex ? 'active' : '';
        return `<div class="queue-item ${activeClass}" onclick="playFromQueue(${i})">
            <div>
                <div class="queue-item-title">${track.title}</div>
                <div class="queue-item-artist">${track.artist}</div>
            </div>
        </div>`;
    }).join('');
}

function playFromQueue(index) {
    currentIndex = index;
    loadTrack(playerQueue[currentIndex]);
    audioElement.play();
}

function formatTime(seconds) {
    if (isNaN(seconds)) return '0:00';
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60);
    return min + ':' + (sec < 10 ? '0' : '') + sec;
}

audioElement.addEventListener('timeupdate', function () {
    const current = audioElement.currentTime;
    const duration = audioElement.duration;

    document.getElementById('current-time').textContent = formatTime(current);
    document.getElementById('total-time').textContent = formatTime(duration);

    if (duration > 0) {
        const pct = (current / duration) * 100;
        document.getElementById('progress-fill').style.width = pct + '%';
    }
});

audioElement.addEventListener('ended', function () {
    if (repeatMode === 2) {
        audioElement.currentTime = 0;
        audioElement.play();
    } else if (repeatMode === 1 || currentIndex < playerQueue.length - 1) {
        playNext();
    } else {
        document.getElementById('play-icon').className = 'fas fa-play';
    }
});

audioElement.volume = 0.8;

function togglePlayerFavorite() {
    if (currentIndex < 0 || !playerQueue[currentIndex]) return;
    const songId = playerQueue[currentIndex].id;

    fetch('/api/player/favorites?songId=' + songId, {
        method: 'POST'
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                const icon = document.querySelector('#player-fav-btn i');
                icon.className = data.data.favorited ? 'fas fa-heart' : 'far fa-heart';
                icon.style.color = data.data.favorited ? '#ef4444' : '';
            }
        })
        .catch(() => { });
}

function recordPlay(songId) {
    fetch('/api/player/history?songId=' + songId, {
        method: 'POST'
    }).catch(() => { });
}

function deleteSong(songId) {
    if (confirm("Are you sure you want to delete this song? This action cannot be undone.")) {
        fetch('/api/songs/' + songId, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    alert("Song deleted successfully!");
                    softReload();
                } else {
                    alert("Error deleting song. You might not have permission.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("A network error occurred.");
            });
    }
}
