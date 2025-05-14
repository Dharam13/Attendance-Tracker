function testScheduledTask(taskType) {
    const url = '/admin/scheduling/test/' + taskType;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.text())
        .then(data => {
            // Show success notification
            showNotification(data, 'success');

            // Reload the page after a short delay to see the new log entry
            setTimeout(() => {
                location.reload();
            }, 2000);
        })
        .catch(error => {
            // Show error notification
            showNotification('Error: ' + error, 'error');
        });
}

function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    // Add to document
    document.body.appendChild(notification);

    // Remove after 5 seconds
    setTimeout(() => {
        notification.remove();
    }, 5000);
}