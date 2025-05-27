// DOM elements
const tbody = document.querySelector('#txTable tbody');
const showAllToggle = document.getElementById('showAllToggle');
const userNameFilter = document.getElementById('userNameFilter');
const userEmailFilter = document.getElementById('userEmailFilter');
const statusFilter = document.getElementById('statusFilter');
const typeFilter = document.getElementById('typeFilter');
const applyFiltersBtn = document.getElementById('applyFiltersBtn');
const resetFiltersBtn = document.getElementById('resetFiltersBtn');
const loadingIndicator = document.getElementById('loadingIndicator');
const noDataMessage = document.getElementById('noDataMessage');
const txTable = document.getElementById('txTable');

// User cache to avoid repeated API calls
const userCache = {};

// Initialize data loading
document.addEventListener('DOMContentLoaded', loadData);

// Add event listeners
showAllToggle.addEventListener('change', loadData);
applyFiltersBtn.addEventListener('click', loadData);
resetFiltersBtn.addEventListener('click', resetFilters);

// Filter reset function
function resetFilters() {
    userNameFilter.value = '';
    userEmailFilter.value = '';
    statusFilter.value = '';
    typeFilter.value = '';
    showAllToggle.checked = false;
    loadData();
}

// Main data loading function
async function loadData() {
    showLoading(true);
    
    // Build query parameters
    const params = new URLSearchParams();
    
    if (showAllToggle.checked) {
        params.append('all', 'true');
    }
    
    if (userNameFilter.value.trim()) {
        params.append('userName', userNameFilter.value.trim());
    }
    
    if (userEmailFilter.value.trim()) {
        params.append('userEmail', userEmailFilter.value.trim());
    }
    
    if (statusFilter.value) {
        params.append('status', statusFilter.value);
    }
    
    if (typeFilter.value) {
        params.append('type', typeFilter.value);
    }
    
    try {
        const url = `${base}?${params.toString()}`;
        const res = await fetch(url, {
            headers: { 'Accept': 'application/json' }
        });
        
        if (!res.ok) {
            console.error('Failed to fetch transactions', res.status);
            showError('Failed to load transactions');
            return;
        }
        
        const transactions = await res.json();
        displayTransactions(transactions);
    } catch (error) {
        console.error('Error loading transaction data:', error);
        showError('An error occurred while loading the data');
    } finally {
        showLoading(false);
    }
}

// Display transactions in the table
function displayTransactions(transactions) {
    tbody.innerHTML = '';
    
    if (transactions.length === 0) {
        noDataMessage.style.display = 'block';
        txTable.style.display = 'none';
        return;
    }
    
    noDataMessage.style.display = 'none';
    txTable.style.display = 'table';
    
    transactions.forEach(tx => {
        const { id, userId, amount, type, status, createdAt } = tx;
        
        const tr = document.createElement('tr');
        
        // Shorten the UUID for display
        const shortId = id.substring(0, 8);
        
        // Format the date
        const date = new Date(createdAt);
        const formattedDate = date.toLocaleDateString() + ' ' + 
                             date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        
        // Set the row HTML with data attribute for userId
        tr.innerHTML = `
            <td title="${id}">${shortId}...</td>
            <td>
                <div class="user-info" data-user-id="${userId}" onclick="showUserDetail('${userId}')" style="cursor: pointer">
                    <span class="user-id">Loading...</span>
                </div>
            </td>
            <td>${amount.toFixed(2)}</td>
            <td>
                <span class="type-badge ${type.toLowerCase() === 'topup' ? 'type-topup' : 'type-purchase'}">
                    ${type}
                </span>
            </td>
            <td>
                <span class="status-badge ${getStatusClass(status)}">
                    ${status}
                </span>
            </td>
            <td>${formattedDate}</td>
            <td>${getActionButtons(id, status, type)}</td>
        `;
        
        tbody.appendChild(tr);
        
        // Try to load user details asynchronously
        loadUserDetails(userId);
    });
}

// Get appropriate status CSS class
function getStatusClass(status) {
    switch(status) {
        case 'SUCCESS': return 'status-success';
        case 'FAILED': return 'status-failed';
        case 'SOFT_DELETED': return 'status-deleted';
        default: return '';
    }
}

// Get action buttons based on status and type
function getActionButtons(id, status, type) {
    let buttons = '';
    
    switch (status) {
        case 'FAILED':
        case 'SOFT_DELETED':
            // If FAILED or SOFT_DELETED: show Success (except for PURCHASE) and Hard Delete buttons
            if (type === 'PURCHASE') {
                buttons = `
                    <button class="btn-action btn-delete" onclick="hardDelete('${id}')">Hard Delete</button>
                `;
            } else {
                buttons = `
                    <button class="btn-action btn-success" onclick="markSuccess('${id}')">Success</button>
                    <button class="btn-action btn-delete" onclick="hardDelete('${id}')">Hard Delete</button>
                `;
            }
            break;
            
        case 'SUCCESS':
            // If SUCCESS: only show Hard Delete, Soft Delete, and Failed buttons
            buttons = `
                <button class="btn-action btn-delete" onclick="hardDelete('${id}')">Hard Delete</button>
                <button class="btn-action btn-delete" onclick="softDelete('${id}')">Soft Delete</button>
                <button class="btn-action btn-fail" onclick="markFailed('${id}')">Failed</button>
            `;
            break;
            
        default:
            // For any other status, show appropriate buttons based on type
            if (type === 'PURCHASE') {
                buttons = `
                    <button class="btn-action btn-fail" onclick="markFailed('${id}')">Failed</button>
                    <button class="btn-action btn-delete" onclick="softDelete('${id}')">Soft Delete</button>
                    <button class="btn-action btn-delete" onclick="hardDelete('${id}')">Hard Delete</button>
                `;
            } else {
                buttons = `
                    <button class="btn-action btn-success" onclick="markSuccess('${id}')">Success</button>
                    <button class="btn-action btn-fail" onclick="markFailed('${id}')">Failed</button>
                    <button class="btn-action btn-delete" onclick="softDelete('${id}')">Soft Delete</button>
                    <button class="btn-action btn-delete" onclick="hardDelete('${id}')">Hard Delete</button>
                `;
            }
    }
    
    return buttons;
}

// Load user details for display
async function loadUserDetails(userId) {
    if (userCache[userId]) {
        updateUserDisplay(userId, userCache[userId]);
        return;
    }
    
    try {
        const res = await fetch(`${userApiBase}/${userId}`, {
            headers: { 'Accept': 'application/json' }
        });
        
        if (!res.ok) {
            console.error(`Failed to fetch user ${userId}`, res.status);
            // Show fallback display when user not found
            updateUserDisplayFallback(userId);
            return;
        }
        
        const user = await res.json();
        userCache[userId] = user;
        updateUserDisplay(userId, user);
    } catch (error) {
        console.error(`Error loading user ${userId}:`, error);
        // Show fallback display on error
        updateUserDisplayFallback(userId);
    }
}

// Update the user display in the table
function updateUserDisplay(userId, user) {
    const userElements = document.querySelectorAll(`[data-user-id="${userId}"]`);
    
    userElements.forEach(el => {
        el.innerHTML = `
            <span class="user-name">${user.name}</span>
            <span class="user-email">${user.email}</span>
        `;
    });
}

// Update user display with fallback when user not found
function updateUserDisplayFallback(userId) {
    const userElements = document.querySelectorAll(`[data-user-id="${userId}"]`);
    
    userElements.forEach(el => {
        el.innerHTML = `<span class="user-id">User #${userId}</span>`;
    });
}

// Show user detail modal
function showUserDetail(userId) {
    const userDetailModal = document.getElementById('userDetailModal');
    const userDetailId = document.getElementById('userDetailId');
    const userDetailName = document.getElementById('userDetailName');
    const userDetailEmail = document.getElementById('userDetailEmail');
    const userDetailBalance = document.getElementById('userDetailBalance');
    
    // If user is cached, show details immediately
    if (userCache[userId]) {
        const user = userCache[userId];
        userDetailId.textContent = user.id;
        userDetailName.textContent = user.name;
        userDetailEmail.textContent = user.email;
        userDetailBalance.textContent = `$${user.balance.toFixed(2)}`;
        
        userDetailModal.classList.add('visible');
        return;
    }
    
    // Otherwise, load user details first
    loadUserDetails(userId).then(() => {
        if (userCache[userId]) {
            const user = userCache[userId];
            userDetailId.textContent = user.id;
            userDetailName.textContent = user.name;
            userDetailEmail.textContent = user.email;
            userDetailBalance.textContent = `$${user.balance.toFixed(2)}`;
            
            userDetailModal.classList.add('visible');
        }
    });
}

// Close user detail modal
function closeUserDetailModal() {
    const userDetailModal = document.getElementById('userDetailModal');
    userDetailModal.classList.remove('visible');
}

// REST helper functions
async function markFailed(id) {
    await fetch(`${base}/${id}/failed`, { method: 'PUT' });
    loadData();
}

async function unFail(id) {
    await fetch(`${base}/${id}/unfail`, { method: 'PUT' });
    loadData();
}

async function softDelete(id) {
    await fetch(`${base}/${id}`, { method: 'DELETE' });
    loadData();
}

async function unSoftDelete(id) {
    await fetch(`${base}/${id}/restore`, { method: 'PUT' });
    loadData();
}

async function hardDelete(id) {
    await fetch(`${base}/${id}/hard`, { method: 'DELETE' });
    loadData();
}

async function markSuccess(id) {
    await fetch(`${base}/${id}/success`, { method: 'PUT' });
    loadData();
}

// Toggle loading indicator
function showLoading(show) {
    loadingIndicator.style.display = show ? 'flex' : 'none';
    if (show) {
        noDataMessage.style.display = 'none';
    }
}

// Show an error message
function showError(message) {
    // You could implement a toast notification here if needed
    console.error(message);
}