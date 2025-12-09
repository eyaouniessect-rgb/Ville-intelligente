const passwordInput = document.getElementById('motDePasse');
const confirmInput = document.getElementById('confirmMotDePasse');
const form = document.getElementById('registerForm');

passwordInput.addEventListener('input', function () {
    const password = this.value;

    document.getElementById('req-length').className =
        password.length >= 12 ? 'requirement valid' : 'requirement invalid';

    document.getElementById('req-uppercase').className =
        /[A-Z]/.test(password) ? 'requirement valid' : 'requirement invalid';

    document.getElementById('req-lowercase').className =
        /[a-z]/.test(password) ? 'requirement valid' : 'requirement invalid';

    document.getElementById('req-number').className =
        /\d/.test(password) ? 'requirement valid' : 'requirement invalid';

    document.getElementById('req-special').className =
        /[@$!%*?&]/.test(password) ? 'requirement valid' : 'requirement invalid';

    if (confirmInput.value) checkPasswordMatch();
});

function checkPasswordMatch() {
    const password = passwordInput.value;
    const confirm = confirmInput.value;
    const errorDiv = document.getElementById('password-match-error');

    if (confirm && password !== confirm) {
        errorDiv.textContent = '❌ Les mots de passe ne correspondent pas';
        confirmInput.classList.add('error');
        return false;
    } else {
        errorDiv.textContent = '';
        confirmInput.classList.remove('error');
        return true;
    }
}

confirmInput.addEventListener('input', checkPasswordMatch);

form.addEventListener('submit', function (e) {
    if (!checkPasswordMatch()) {
        e.preventDefault();
        alert('⚠️ Les mots de passe ne correspondent pas !');
    }
});
