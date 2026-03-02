/**
 * This function handles the User Registration process.
 * It gathers data from the form and sends it to our Java backend.
 */
function handleRegister(event) {
    // 1. Prevent the page from refreshing (the default form behavior)
    event.preventDefault();

    // 2. Disable the button so the user doesn't click it twice
    const btn = document.getElementById('register-btn');
    btn.disabled = true;
    btn.textContent = 'Creating account...';

    // 3. Collect all the data from the input fields
    const displayNameElem = document.getElementById('displayName');
    const payload = {
        email: document.getElementById('email').value,
        username: document.getElementById('username').value,
        displayName: displayNameElem ? displayNameElem.value : '', // Handle missing element
        password: document.getElementById('password').value,
        securityQuestion: document.getElementById('securityQuestion').value,
        securityAnswer: document.getElementById('securityAnswer').value,
        role: document.getElementById('role').value || 'LISTENER'
    };

    console.log('Sending registration payload:', payload);

    // 4. Send a POST request to our Server API
    fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(response => response.json()) // Convert the response to JSON
        .then(data => {
            // 5. Check if the server says "success"
            if (data.success) {
                // Redirect to the login page with a success message
                window.location.href = '/login?registered=true';
            } else {
                // Show the error message returned by the server (e.g., "Email already exists")
                showAuthError(data.message || 'Registration failed');
            }

            // Always re-enable the button after the request finishes
            btn.disabled = false;
            btn.textContent = 'Create Account';
        })
        .catch(() => {
            // Fallback for connection errors
            showAuthError('Connection error. Please try again.');
            btn.disabled = false;
            btn.textContent = 'Create Account';
        });
}

/**
 * Utility function to show error messages on the screen
 */
function showAuthError(message) {
    const errorElement = document.getElementById('auth-error');
    errorElement.textContent = message;
    errorElement.style.display = 'block';
}
