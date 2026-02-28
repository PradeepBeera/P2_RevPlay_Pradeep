function apiHeaders() {
    return { 'Content-Type': 'application/json' };
}

function showCreatePlaylist() {
    document.getElementById('create-playlist-form').style.display = 'flex';
}

function hideCreatePlaylist() {
    document.getElementById('create-playlist-form').style.display = 'none';
}

function createPlaylist() {
    const name = document.getElementById('playlist-name').value.trim();
    if (!name) return;

    fetch('/api/playlists', {
        method: 'POST',
        headers: apiHeaders(),
        body: JSON.stringify({
            name: name,
            description: document.getElementById('playlist-desc').value,
            privacy: document.getElementById('playlist-privacy').value
        })
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) softReload();
        });
}

function removeFromPlaylist(playlistId, songId) {
    event.stopPropagation();
    fetch('/api/playlists/' + playlistId + '/songs/' + songId, {
        method: 'DELETE',
        headers: apiHeaders()
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) softReload();
        });
}

function showCreateAlbum() {
    document.getElementById('create-album-form').style.display = 'flex';
}

function hideCreateAlbum() {
    document.getElementById('create-album-form').style.display = 'none';
}

function createAlbum() {
    const name = document.getElementById('album-name').value.trim();
    if (!name) return;

    const formData = new FormData();
    formData.append('name', name);
    formData.append('description', document.getElementById('album-desc').value);

    const coverFile = document.getElementById('album-cover');
    if (coverFile.files[0]) formData.append('coverImage', coverFile.files[0]);

    fetch('/api/albums', {
        method: 'POST',
        body: formData
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) softReload();
        });
}

function handleUpload(event) {
    event.preventDefault();
    const btn = document.getElementById('upload-btn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';

    const formData = new FormData();
    formData.append('title', document.getElementById('title').value);
    formData.append('genre', document.getElementById('genre').value);
    formData.append('visibility', document.getElementById('visibility').value);

    const albumId = document.getElementById('albumId').value;
    if (albumId) formData.append('albumId', albumId);

    formData.append('audioFile', document.getElementById('audioFile').files[0]);

    const coverFile = document.getElementById('coverImage');
    if (coverFile.files[0]) formData.append('coverImage', coverFile.files[0]);

    fetch('/api/songs', {
        method: 'POST',
        body: formData
    })
        .then(r => r.json())
        .then(data => {
            const status = document.getElementById('upload-status');
            if (data.success) {
                status.className = 'alert alert-success';
                status.textContent = 'Song uploaded successfully!';
                status.style.display = 'block';
                document.getElementById('upload-form').reset();
            } else {
                status.className = 'alert alert-error';
                status.textContent = data.message || 'Upload failed';
                status.style.display = 'block';
            }
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-upload"></i> Upload Song';
        })
        .catch(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-upload"></i> Upload Song';
        });
}

function updateFileLabel(inputId, labelId) {
    const input = document.getElementById(inputId);
    const label = document.getElementById(labelId);
    if (input.files[0]) {
        label.textContent = input.files[0].name;
    }
}

function showBecomeArtist() {
    document.getElementById('artist-form').style.display = 'flex';
}

function createArtistProfile() {
    const artistName = document.getElementById('artistName').value.trim();
    if (!artistName) return;

    fetch('/api/artists/profile', {
        method: 'POST',
        headers: apiHeaders(),
        body: JSON.stringify({
            artistName: artistName,
            genre: document.getElementById('artistGenre').value,
            instagram: document.getElementById('artistInstagram').value,
            website: document.getElementById('artistWebsite').value
        })
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                navigateTo('/dashboard');
            }
        });
}
// Re-initialize any page-specific logic after an AJAX navigation
document.addEventListener('page-changed', function (e) {
    console.log('Page changed via AJAX:', e.detail.url);
    // Any global UI re-init (sidebar highlitght, etc.)
});

// Replace window.location.reload() with navigateTo() if available
function softReload() {
    if (typeof navigateTo === 'function') {
        navigateTo(window.location.pathname);
    } else {
        window.location.reload();
    }
}
