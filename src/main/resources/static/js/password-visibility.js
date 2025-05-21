/**
 * Toggles the visibility of a password field
 * 
 * @param {string} inputId - The ID of the password input field
 */
function togglePasswordVisibility(inputId) {
    // Get the password input
    const passwordInput = document.getElementById(inputId);
    
    // Get the eye icons
    const showPasswordIcon = passwordInput.parentElement.querySelector('.show-password');
    const hidePasswordIcon = passwordInput.parentElement.querySelector('.hide-password');
    
    // Toggle the password field type
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        showPasswordIcon.style.display = 'none';
        hidePasswordIcon.style.display = 'block';
    } else {
        passwordInput.type = 'password';
        showPasswordIcon.style.display = 'block';
        hidePasswordIcon.style.display = 'none';
    }
}