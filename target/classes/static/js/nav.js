/**
 * nav.js - AJAX Navigation (SPA-lite)
 * This script intercepts link clicks to prevent full-page reloads,
 * allowing the audio player to continue playing music across pages.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Intercept all internal link clicks
    document.addEventListener('click', (e) => {
        const link = e.target.closest('a');
        if (!link) return;

        const url = link.getAttribute('href');

        // Skip if not an internal link or has target="_blank"
        if (!url || url.startsWith('http') || url.startsWith('javascript:') ||
            url.startsWith('#') || link.getAttribute('target') === '_blank' ||
            url === '/logout') {
            return;
        }

        e.preventDefault();
        navigateTo(url);
    });

    // Intercept search form submissions
    document.addEventListener('submit', (e) => {
        const form = e.target;
        if (form.getAttribute('method')?.toLowerCase() === 'get' && form.getAttribute('action') === '/search') {
            e.preventDefault();
            const query = new FormData(form).get('query');
            navigateTo('/search?query=' + encodeURIComponent(query));
        }
    });

    // Initial sidebar highlight
    updateSidebarActive(window.location.pathname);

    // Handle browser back/forward buttons
    window.addEventListener('popstate', (e) => {
        const url = (e.state && e.state.url) ? e.state.url : window.location.pathname;
        loadPageContent(url, false);
    });
});

/**
 * Navigates to a new URL using AJAX and pushState.
 */
function navigateTo(url) {
    loadPageContent(url, true);
}

/**
 * Updates the 'active' class on sidebar links based on the current URL.
 */
function updateSidebarActive(url) {
    document.querySelectorAll('.nav-item').forEach(link => {
        const href = link.getAttribute('href');
        if (href && (url === href || url.startsWith(href + '/'))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

/**
 * Fetches page content and swaps the main container.
 */
async function loadPageContent(url, push = true) {
    // Show a simple loading indicator if desired
    const mainArea = document.getElementById('main-content-area');
    if (mainArea) mainArea.style.opacity = '0.6';

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Network response was not ok');

        const html = await response.text();
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        const newContent = doc.getElementById('main-content-area');
        const currentContent = document.getElementById('main-content-area');

        if (newContent && currentContent) {
            // Swap content
            currentContent.innerHTML = newContent.innerHTML;
            currentContent.style.opacity = '1';

            // Update page title
            document.title = doc.title;

            // Update URL
            if (push) {
                window.history.pushState({ url }, doc.title, url);
            }

            // Update sidebar
            updateSidebarActive(url);

            // Re-run any scripts found in the new content
            const scripts = currentContent.querySelectorAll('script');
            scripts.forEach(script => {
                const newScript = document.createElement('script');
                Array.from(script.attributes).forEach(attr => {
                    newScript.setAttribute(attr.name, attr.value);
                });
                newScript.appendChild(document.createTextNode(script.innerHTML));
                script.parentNode.replaceChild(newScript, script);
            });

            // Dispatch a custom event to notify other scripts
            document.dispatchEvent(new CustomEvent('page-changed', { detail: { url } }));

            window.scrollTo(0, 0);
        } else {
            window.location.href = url;
        }
    } catch (error) {
        console.error('Navigation error:', error);
        window.location.href = url;
    }
}
