// Dashboard JavaScript functionality

// Event card hover effects
document.addEventListener('DOMContentLoaded', function() {
    // Event cards hover animation
    const eventCards = document.querySelectorAll('.event-card');
    eventCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.boxShadow = '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)';
            this.style.transform = 'translateY(-2px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.boxShadow = 'none';
            this.style.transform = 'translateY(0)';
        });
    });
    
    // Jumbotron button hover effects
    const jumbotronBtnPrimary = document.querySelector('.jumbotron-btn-primary');
    if (jumbotronBtnPrimary) {
        jumbotronBtnPrimary.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.05)';
            this.style.boxShadow = '0 8px 30px rgba(0, 0, 0, 0.4)';
        });
        
        jumbotronBtnPrimary.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.3)';
        });
    }
    
    const jumbotronBtnSecondary = document.querySelector('.jumbotron-btn-secondary');
    if (jumbotronBtnSecondary) {
        jumbotronBtnSecondary.addEventListener('mouseenter', function() {
            this.style.backgroundColor = 'rgba(255,255,255,0.2)';
            this.style.transform = 'translateY(-3px) scale(1.05)';
            this.style.borderColor = 'white';
        });
        
        jumbotronBtnSecondary.addEventListener('mouseleave', function() {
            this.style.backgroundColor = 'rgba(255,255,255,0.1)';
            this.style.transform = 'translateY(0) scale(1)';
            this.style.borderColor = 'rgba(255, 255, 255, 0.9)';
        });
    }
});