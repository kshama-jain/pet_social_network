/**
 * PetConnect - Main JavaScript File
 * Contains functions for the social networking platform
 */

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('PetConnect JS loaded');
    
    // Initialize event listeners
    initializeEventListeners();
    
    // Initialize tooltips
    initializeTooltips();
    
    // Handle like/unlike functionality
    initializeLikeSystem();
    
    // Initialize comment system if on post page
    if(document.querySelector('.comment-form')) {
        initializeCommentSystem();
    }
    
    // Initialize image previews on upload forms
    initializeImagePreviews();
});

/**
 * Initialize various event listeners
 */
function initializeEventListeners() {
    // Toggle mobile navigation
    const navbarToggler = document.querySelector('.navbar-toggler');
    if(navbarToggler) {
        navbarToggler.addEventListener('click', function() {
            document.querySelector('.navbar-collapse').classList.toggle('show');
        });
    }
    
    // Confirm before form submissions for delete operations
    const deleteForms = document.querySelectorAll('.delete-form');
    if(deleteForms.length > 0) {
        deleteForms.forEach(form => {
            form.addEventListener('submit', function(e) {
                if(!confirm('Are you sure you want to delete this item? This action cannot be undone.')) {
                    e.preventDefault();
                }
            });
        });
    }
    
    // Password confirmation check
    const passwordForms = document.querySelectorAll('.password-form');
    if(passwordForms.length > 0) {
        passwordForms.forEach(form => {
            form.addEventListener('submit', function(e) {
                const password = form.querySelector('[name="password"]').value;
                const confirmPassword = form.querySelector('[name="confirmPassword"]').value;
                
                if(password !== confirmPassword) {
                    e.preventDefault();
                    alert('Passwords do not match!');
                }
            });
        });
    }
    
    // Handle modal closing and form reset
    const modals = document.querySelectorAll('.modal');
    if(modals.length > 0) {
        modals.forEach(modal => {
            modal.addEventListener('hidden.bs.modal', function() {
                const forms = this.querySelectorAll('form');
                if(forms.length > 0) {
                    forms.forEach(form => form.reset());
                }
            });
        });
    }
    
    // Auto-resize textareas to fit content
    const textareas = document.querySelectorAll('textarea.auto-resize');
    if(textareas.length > 0) {
        textareas.forEach(textarea => {
            textarea.addEventListener('input', function() {
                this.style.height = 'auto';
                this.style.height = (this.scrollHeight) + 'px';
            });
            
            // Trigger once to set initial height
            textarea.dispatchEvent(new Event('input'));
        });
    }
}

/**
 * Initialize Bootstrap tooltips
 */
function initializeTooltips() {
    // Check if Bootstrap's tooltip is available
    if(typeof bootstrap !== 'undefined' && typeof bootstrap.Tooltip !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function(tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
}

/**
 * Initialize post like/unlike functionality
 */
function initializeLikeSystem() {
    // Handle like button clicks
    const likeButtons = document.querySelectorAll('.like-btn');
    if(likeButtons.length > 0) {
        likeButtons.forEach(button => {
            button.addEventListener('click', function() {
                const postId = this.getAttribute('data-post-id');
                likePost(postId, this);
            });
        });
    }
    
    // Handle unlike button clicks
    const unlikeButtons = document.querySelectorAll('.unlike-btn');
    if(unlikeButtons.length > 0) {
        unlikeButtons.forEach(button => {
            button.addEventListener('click', function() {
                const postId = this.getAttribute('data-post-id');
                unlikePost(postId, this);
            });
        });
    }
}

/**
 * Like a post via AJAX
 * @param {string} postId - The ID of the post to like
 * @param {HTMLElement} button - The like button element
 */
function likePost(postId, button) {
    fetch(`/posts/${postId}/like`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if(data.success) {
            // Update the like count display
            const likeCountSpan = button.querySelector('span');
            likeCountSpan.textContent = data.likes;
            
            // Convert the like button to an unlike button
            button.classList.remove('btn-outline-primary', 'like-btn');
            button.classList.add('btn-primary', 'unlike-btn');
            
            // Update the button icon
            const icon = button.querySelector('i');
            icon.classList.remove('bi-heart');
            icon.classList.add('bi-heart-fill');
            
            // Update event listener
            button.removeEventListener('click', likePost);
            button.addEventListener('click', function() {
                const postId = this.getAttribute('data-post-id');
                unlikePost(postId, this);
            });
        } else {
            console.error('Failed to like post:', data.message);
        }
    })
    .catch(error => {
        console.error('Error liking post:', error);
    });
}

/**
 * Unlike a post via AJAX
 * @param {string} postId - The ID of the post to unlike
 * @param {HTMLElement} button - The unlike button element
 */
function unlikePost(postId, button) {
    fetch(`/posts/${postId}/unlike`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if(data.success) {
            // Update the like count display
            const likeCountSpan = button.querySelector('span');
            likeCountSpan.textContent = data.likes;
            
            // Convert the unlike button to a like button
            button.classList.remove('btn-primary', 'unlike-btn');
            button.classList.add('btn-outline-primary', 'like-btn');
            
            // Update the button icon
            const icon = button.querySelector('i');
            icon.classList.remove('bi-heart-fill');
            icon.classList.add('bi-heart');
            
            // Update event listener
            button.removeEventListener('click', unlikePost);
            button.addEventListener('click', function() {
                const postId = this.getAttribute('data-post-id');
                likePost(postId, this);
            });
        } else {
            console.error('Failed to unlike post:', data.message);
        }
    })
    .catch(error => {
        console.error('Error unliking post:', error);
    });
}

/**
 * Initialize comment system functionality
 */
function initializeCommentSystem() {
    const commentForm = document.querySelector('.comment-form');
    if(commentForm) {
        commentForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const postId = this.getAttribute('data-post-id');
            const contentInput = this.querySelector('textarea[name="content"]');
            const content = contentInput.value.trim();
            
            if(content !== '') {
                submitComment(postId, content, this);
            }
        });
    }
}

/**
 * Submit a comment via AJAX
 * @param {string} postId - The ID of the post to comment on
 * @param {string} content - The comment content
 * @param {HTMLElement} form - The comment form element
 */
function submitComment(postId, content, form) {
    fetch(`/posts/${postId}/comments/add`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({ content: content }),
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if(data.success) {
            // Clear the comment input
            form.querySelector('textarea[name="content"]').value = '';
            
            // Optionally reload the page to show the new comment
            // or dynamically add the comment to the DOM
            window.location.reload();
        } else {
            console.error('Failed to add comment:', data.message);
        }
    })
    .catch(error => {
        console.error('Error adding comment:', error);
    });
}

/**
 * Initialize image preview functionality for file uploads
 */
function initializeImagePreviews() {
    // Handle profile image uploads
    const profileImageInput = document.querySelector('#profileImage');
    if(profileImageInput) {
        profileImageInput.addEventListener('change', function() {
            displayImagePreview(this, '.profile-preview', 150, 150);
        });
    }
    
    // Handle pet image uploads
    const petImageInput = document.querySelector('#petImage');
    if(petImageInput) {
        petImageInput.addEventListener('change', function() {
            displayImagePreview(this, '.pet-preview', 200, 200);
        });
    }
    
    // Handle post media uploads
    const mediaFileInput = document.querySelector('#mediaFile');
    if(mediaFileInput) {
        mediaFileInput.addEventListener('change', function() {
            const previewContainer = document.querySelector('.media-preview');
            if(!previewContainer) return;
            
            previewContainer.innerHTML = '';
            
            if(this.files && this.files[0]) {
                const file = this.files[0];
                const reader = new FileReader();
                
                // Check if it's an image or video
                if(file.type.match('image.*')) {
                    reader.onload = function(e) {
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.classList.add('img-fluid', 'rounded', 'mb-3');
                        previewContainer.appendChild(img);
                    };
                    reader.readAsDataURL(file);
                } else if(file.type.match('video.*')) {
                    reader.onload = function(e) {
                        const video = document.createElement('video');
                        video.src = e.target.result;
                        video.classList.add('img-fluid', 'rounded', 'mb-3');
                        video.controls = true;
                        previewContainer.appendChild(video);
                    };
                    reader.readAsDataURL(file);
                }
            }
        });
    }
    
    // Handle group image uploads
    const groupImageInput = document.querySelector('#groupImage');
    if(groupImageInput) {
        groupImageInput.addEventListener('change', function() {
            displayImagePreview(this, '.group-preview', 300, 150);
        });
    }
    
    // Handle event image uploads
    const eventImageInput = document.querySelector('#eventImage');
    if(eventImageInput) {
        eventImageInput.addEventListener('change', function() {
            displayImagePreview(this, '.event-preview', 300, 150);
        });
    }
}

/**
 * Display an image preview for an input file
 * @param {HTMLElement} input - The file input element
 * @param {string} previewSelector - The CSS selector for the preview container
 * @param {number} width - The max width for the preview image
 * @param {number} height - The max height for the preview image
 */
function displayImagePreview(input, previewSelector, width, height) {
    const previewContainer = document.querySelector(previewSelector);
    if(!previewContainer) return;
    
    previewContainer.innerHTML = '';
    
    if(input.files && input.files[0]) {
        const reader = new FileReader();
        
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.style.maxWidth = width + 'px';
            img.style.maxHeight = height + 'px';
            img.classList.add('img-thumbnail');
            previewContainer.appendChild(img);
        };
        
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Convert a form to use AJAX submission
 * @param {string} formSelector - The CSS selector for the form
 * @param {Function} successCallback - Function to call on successful submission
 */
function enableAjaxForm(formSelector, successCallback) {
    const form = document.querySelector(formSelector);
    if(!form) return;
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(this);
        
        fetch(this.action, {
            method: this.method,
            body: formData,
            credentials: 'same-origin'
        })
        .then(response => response.json())
        .then(data => {
            if(data.success) {
                if(successCallback) {
                    successCallback(data);
                }
            } else {
                console.error('Form submission failed:', data.message);
                alert('Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error submitting form:', error);
            alert('An error occurred while submitting the form.');
        });
    });
}

/**
 * Initialize infinite scroll functionality for a content container
 * @param {string} containerSelector - The CSS selector for the content container
 * @param {string} apiEndpoint - The API endpoint for loading more content
 * @param {Function} renderCallback - Function to render each item
 */
function initializeInfiniteScroll(containerSelector, apiEndpoint, renderCallback) {
    const container = document.querySelector(containerSelector);
    if(!container) return;
    
    let page = 0;
    let loading = false;
    let noMoreContent = false;
    
    // Create loading indicator
    const loadingIndicator = document.createElement('div');
    loadingIndicator.classList.add('text-center', 'py-3', 'loading-indicator');
    loadingIndicator.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>';
    loadingIndicator.style.display = 'none';
    container.parentNode.insertBefore(loadingIndicator, container.nextSibling);
    
    // Function to load more content
    function loadMoreContent() {
        if(loading || noMoreContent) return;
        
        loading = true;
        loadingIndicator.style.display = 'block';
        
        fetch(`${apiEndpoint}?page=${page}`, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin'
        })
        .then(response => response.json())
        .then(data => {
            if(data.items && data.items.length > 0) {
                data.items.forEach(item => {
                    const itemElement = renderCallback(item);
                    container.appendChild(itemElement);
                });
                
                page++;
            } else {
                noMoreContent = true;
            }
            
            loading = false;
            loadingIndicator.style.display = 'none';
        })
        .catch(error => {
            console.error('Error loading more content:', error);
            loading = false;
            loadingIndicator.style.display = 'none';
        });
    }
    
    // Load more content when scrolling near the bottom
    window.addEventListener('scroll', function() {
        const scrollPosition = window.innerHeight + window.pageYOffset;
        const containerBottom = container.offsetTop + container.offsetHeight;
        
        if(scrollPosition >= containerBottom - 500) {
            loadMoreContent();
        }
    });
    
    // Initial load
    loadMoreContent();
}

/**
 * Format a date string in a user-friendly format
 * @param {string} dateStr - The date string to format
 * @returns {string} The formatted date string
 */
function formatDate(dateStr) {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);
    
    if(diffDay > 7) {
        return date.toLocaleDateString();
    } else if(diffDay > 1) {
        return `${diffDay} days ago`;
    } else if(diffDay === 1) {
        return 'Yesterday';
    } else if(diffHour >= 1) {
        return `${diffHour} ${diffHour === 1 ? 'hour' : 'hours'} ago`;
    } else if(diffMin >= 1) {
        return `${diffMin} ${diffMin === 1 ? 'minute' : 'minutes'} ago`;
    } else {
        return 'Just now';
    }
}

/**
 * Toggle dark mode
 */
function toggleDarkMode() {
    document.body.classList.toggle('dark-mode');
    
    // Save preference to localStorage
    const isDarkMode = document.body.classList.contains('dark-mode');
    localStorage.setItem('darkMode', isDarkMode ? 'enabled' : 'disabled');
}

// Check for dark mode preference on load
if(localStorage.getItem('darkMode') === 'enabled') {
    document.body.classList.add('dark-mode');
}
