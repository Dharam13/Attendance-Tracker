document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('register-form');
    const steps = Array.from(document.querySelectorAll('.step'));
    const formSteps = Array.from(document.querySelectorAll('.form-step'));
    const nextButtons = Array.from(document.querySelectorAll('.next-step'));
    const prevButtons = Array.from(document.querySelectorAll('.prev-step'));
    const thresholdInput = document.getElementById('attendanceThreshold');
    const thresholdValue = document.getElementById('thresholdValue');

    let currentStep = 0;

    // Initialize threshold value display
    thresholdValue.textContent = thresholdInput.value;

    // Update threshold value display when slider moves
    thresholdInput.addEventListener('input', function() {
        thresholdValue.textContent = this.value;

        // Position the value indicator
        const percent = (this.value - this.min) / (this.max - this.min);
        const thumbWidth = 20; // Width of the slider thumb
        const trackWidth = this.offsetWidth - thumbWidth;
        const position = percent * trackWidth + thumbWidth/2;

        thresholdValue.parentElement.style.left = `${position}px`;
    });

    // Handle next button clicks
    nextButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Validate current form step
            const currentFormStep = formSteps[currentStep];
            const inputs = currentFormStep.querySelectorAll('input[required]');
            let isValid = true;

            inputs.forEach(input => {
                if (!input.value) {
                    isValid = false;
                    input.classList.add('invalid');

                    // Add shake animation
                    input.parentElement.classList.add('shake');
                    setTimeout(() => {
                        input.parentElement.classList.remove('shake');
                    }, 500);
                } else {
                    input.classList.remove('invalid');
                }
            });

            if (!isValid) return;

            // Move to next step
            moveToStep(currentStep + 1);
        });
    });

    // Handle previous button clicks
    prevButtons.forEach(button => {
        button.addEventListener('click', function() {
            moveToStep(currentStep - 1);
        });
    });

    // Function to move between steps
    function moveToStep(stepIndex) {
        if (stepIndex < 0 || stepIndex >= steps.length) return;

        // Hide current step
        formSteps[currentStep].classList.add('hidden');
        steps[currentStep].classList.remove('active');

        // If moving forward, mark the current step as completed
        if (stepIndex > currentStep) {
            steps[currentStep].classList.add('completed');
        }

        // Show new step
        currentStep = stepIndex;
        formSteps[currentStep].classList.remove('hidden');
        steps[currentStep].classList.add('active');

        // Animate new step
        formSteps[currentStep].style.animation = 'none';
        setTimeout(() => {
            formSteps[currentStep].style.animation = 'fadeIn 0.5s ease-out';
        }, 10);
    }

    // Add keypress event to move to next step when pressing Enter
    form.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && currentStep < steps.length - 1) {
            e.preventDefault();
            nextButtons[currentStep].click();
        }
    });

    // Add visual feedback for inputs
    const inputs = document.querySelectorAll('input[type="text"], input[type="email"], input[type="password"]');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });

        input.addEventListener('blur', function() {
            this.parentElement.classList.remove('focused');
        });
    });

    // Add animation to the particles
    const particlesContainer = document.querySelector('.particles-container');

    for (let i = 0; i < 20; i++) {
        const particle = document.createElement('div');
        particle.className = 'floating-particle';

        // Random properties
        const size = Math.random() * 3 + 1;
        const posX = Math.random() * 100;
        const posY = Math.random() * 100;
        const delay = Math.random() * 5;
        const duration = Math.random() * 10 + 10;
        const color = Math.random() > 0.5 ? 'var(--primary)' : 'var(--secondary)';

        // Apply styles
        particle.style.width = `${size}px`;
        particle.style.height = `${size}px`;
        particle.style.left = `${posX}%`;
        particle.style.top = `${posY}%`;
        particle.style.backgroundColor = color;
        particle.style.boxShadow = `0 0 ${size * 2}px ${color}`;
        particle.style.animationDelay = `${delay}s`;
        particle.style.animationDuration = `${duration}s`;

        particlesContainer.appendChild(particle);
    }
});

// Add shake animation keyframes
const style = document.createElement('style');
style.innerHTML = `
@keyframes shake {
    0%, 100% { transform: translateX(0); }
    10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
    20%, 40%, 60%, 80% { transform: translateX(5px); }
}

.shake {
    animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

.floating-particle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.5;
    animation: float linear infinite;
    z-index: 0;
}

@keyframes float {
    0% {
        transform: translate(0, 0);
    }
    25% {
        transform: translate(50px, -50px);
    }
    50% {
        transform: translate(100px, 0);
    }
    75% {
        transform: translate(50px, 50px);
    }
    100% {
        transform: translate(0, 0);
    }
}
`;
document.head.appendChild(style);