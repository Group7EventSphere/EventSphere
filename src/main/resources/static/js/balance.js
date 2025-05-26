// Balance page functionality

document.addEventListener('DOMContentLoaded', function() {
  // Check which page we're on
  const isTopupPage = document.getElementById('topupForm') !== null;
  const isHistoryPage = document.querySelector('.data-table') !== null;
  
  // Initialize page-specific functionality
  if (isTopupPage) {
    initializeTopupPage();
  }
  
  if (isHistoryPage) {
    initializeHistoryPage();
  }
});

// Common functions
function updateBalance(newBalance) {
  const balanceElement = document.querySelector('.balance-amount');
  if (balanceElement) {
    // Format number with dot separators
    const formatted = new Intl.NumberFormat('id-ID').format(newBalance);
    balanceElement.textContent = formatted;
  }
}

async function refreshBalance() {
  try {
    const response = await fetch(authApiBase, {
      headers: { 'Accept': 'application/json' }
    });
    
    if (response.ok) {
      const userData = await response.json();
      updateBalance(userData.balance);
    }
  } catch (error) {
    console.error('Error fetching user data:', error);
  }
}

function showAlert(message, type) {
  const alertDiv = document.querySelector('.alert-message');
  if (alertDiv) {
    alertDiv.style.display = 'block';
    alertDiv.querySelector('p').textContent = message;
    alertDiv.className = 'alert-message alert-' + type;
    
    // Hide after 5 seconds
    setTimeout(() => {
      alertDiv.style.display = 'none';
    }, 5000);
  }
}

// Top-up page specific functions
function initializeTopupPage() {
  // Handle preset amount buttons
  Array.from(document.querySelectorAll('.preset-btn')).forEach(btn => {
    btn.addEventListener('click', () => {
      document.getElementById('amountInput').value = btn.dataset.value;
    });
  });
  
  // Handle radio option selection styling
  const radioOptions = document.querySelectorAll('.radio-option');
  const radioInputs = document.querySelectorAll('.radio-option input[type="radio"]');
  
  function updateSelection() {
    radioOptions.forEach(option => {
      const radio = option.querySelector('input[type="radio"]');
      if (radio.checked) {
        option.classList.add('selected');
      } else {
        option.classList.remove('selected');
      }
    });
  }
  
  // Initial selection state
  updateSelection();
  
  // Handle clicks on radio options
  radioOptions.forEach(option => {
    option.addEventListener('click', function(e) {
      if (e.target.tagName !== 'INPUT') {
        const radio = this.querySelector('input[type="radio"]');
        radio.checked = true;
        updateSelection();
      }
    });
  });
  
  // Handle radio input changes
  radioInputs.forEach(radio => {
    radio.addEventListener('change', updateSelection);
  });
  
  // Handle form submission with AJAX
  const topupForm = document.getElementById('topupForm');
  topupForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    // Get form data
    const formData = new FormData(this);
    const amount = formData.get('amount');
    const method = formData.get('method');
    
    // Validate amount
    if (!amount || amount <= 0) {
      showAlert('Please enter a valid amount', 'error');
      return;
    }
    
    // Show loading state
    const submitBtn = this.querySelector('.btn-payment');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Processing...';
    submitBtn.disabled = true;
    
    try {
      // Submit the form with redirect handling
      const response = await fetch(topupUrl, {
        method: 'POST',
        body: formData,
        redirect: 'manual'
      });
      
      // Check if it's a redirect or successful response
      if (response.type === 'opaqueredirect' || response.status === 0 || response.ok) {
        // Form submitted successfully, wait a bit then get updated balance
        setTimeout(async () => {
          try {
            const userResponse = await fetch(authApiBase, {
              headers: { 'Accept': 'application/json' }
            });
            
            if (userResponse.ok) {
              const userData = await userResponse.json();
              updateBalance(userData.balance);
              showAlert('Top-up successful!', 'success');
              
              // Clear the form
              document.getElementById('amountInput').value = '';
            } else {
              // If can't get user data, reload the page to get fresh data
              window.location.reload();
            }
          } catch (error) {
            console.error('Error fetching updated balance:', error);
            // Reload page as fallback
            window.location.reload();
          }
        }, 1000); // Wait 1 second for the transaction to complete
      } else {
        showAlert('Top-up failed. Please try again.', 'error');
      }
    } catch (error) {
      console.error('Error during top-up:', error);
      showAlert('An error occurred. Please try again.', 'error');
    } finally {
      // Reset button after a delay to ensure transaction completes
      setTimeout(() => {
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
      }, 2000);
    }
  });
}

// History page specific functions
function initializeHistoryPage() {
  // Refresh balance periodically (every 30 seconds)
  setInterval(refreshBalance, 30000);
}