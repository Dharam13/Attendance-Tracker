// attendance-chart.js
document.addEventListener('DOMContentLoaded', function() {
    // Wait a moment to ensure window variables are properly set
    setTimeout(() => {
        // Get chart data from window variables or use defaults
        const subjects = window.subjects || [];
        const attendanceThreshold = window.attendanceThreshold || 75;

        // Get the canvas context
        const ctx = document.getElementById('attendanceChart');

        if (!ctx) {
            console.error('Chart canvas element not found');
            return;
        }

        // Check if subjects array exists and has items
        if (subjects && subjects.length > 0) {
            console.log('Loading chart with subjects:', subjects);

            const subjectNames = subjects.map(subject => subject.name);
            const attendanceValues = subjects.map(subject => subject.currentAttendance);

            const attendanceChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: subjectNames,
                    datasets: [{
                        label: 'Attendance %',
                        data: attendanceValues,
                        backgroundColor: attendanceValues.map(value =>
                            value < attendanceThreshold ? 'rgba(255, 99, 132, 0.7)' : 'rgba(0, 243, 255, 0.7)'
                        ),
                        borderColor: attendanceValues.map(value =>
                            value < attendanceThreshold ? 'rgb(255, 99, 132)' : 'rgb(0, 243, 255)'
                        ),
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                            grid: {
                                color: 'rgba(255, 255, 255, 0.1)'
                            },
                            ticks: {
                                color: 'rgba(255, 255, 255, 0.7)'
                            }
                        },
                        x: {
                            grid: {
                                color: 'rgba(255, 255, 255, 0.1)'
                            },
                            ticks: {
                                color: 'rgba(255, 255, 255, 0.7)'
                            }
                        }
                    },
                    plugins: {
                        tooltip: {
                            backgroundColor: 'rgba(16, 16, 39, 0.9)',
                            titleColor: '#fff',
                            bodyColor: '#fff',
                            borderColor: 'rgba(0, 243, 255, 0.5)',
                            borderWidth: 1
                        },
                        legend: {
                            labels: {
                                color: 'rgba(255, 255, 255, 0.7)'
                            }
                        }
                    }
                }
            });
        } else {
            console.error('No subject data available for chart');
            // Display no-data message
            const chartContainer = document.querySelector('.chart-container');
            if (chartContainer) {
                const noDataMessage = document.createElement('div');
                noDataMessage.className = 'no-data';
                noDataMessage.innerHTML = '<i class="fas fa-chart-bar"></i><p>No attendance data available to display chart.</p>';
                chartContainer.appendChild(noDataMessage);
            }
        }
    }, 100); // Small delay to ensure variables are set
});