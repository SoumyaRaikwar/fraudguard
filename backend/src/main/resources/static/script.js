
        document.addEventListener('DOMContentLoaded', function() {
            
            const activityCtx = document.getElementById('activityChart').getContext('2d');
            const activityChart = new Chart(activityCtx, {
                type: 'bar',
                data: {
                    labels: ['Structuring', 'Layering', 'Integration', 'Trade-Based', 'Crypto'],
                    datasets: [{
                        label: 'Number of Cases',
                        data: [42, 28, 19, 31, 22],
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.7)',
                            'rgba(54, 162, 235, 0.7)',
                            'rgba(255, 206, 86, 0.7)',
                            'rgba(75, 192, 192, 0.7)',
                            'rgba(153, 102, 255, 0.7)'
                        ],
                        borderColor: [
                            'rgba(255, 99, 132, 1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });

            const accuracyCtx = document.getElementById('accuracyChart').getContext('2d');
            const accuracyChart = new Chart(accuracyCtx, {
                type: 'radar',
                data: {
                    labels: ['SVM', 'Neural Network', 'Random Forest', 'LSTM', 'GRU'],
                    datasets: [{
                        label: 'Detection Accuracy (%)',
                        data: [93.45, 92.14, 94.20, 91.80, 90.50],
                        backgroundColor: 'rgba(26, 35, 126, 0.2)',
                        borderColor: 'rgba(26, 35, 126, 1)',
                        pointBackgroundColor: 'rgba(26, 35, 126, 1)',
                        pointBorderColor: '#fff',
                        pointHoverBackgroundColor: '#fff',
                        pointHoverBorderColor: 'rgba(26, 35, 126, 1)'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        r: {
                            angleLines: {
                                display: true
                            },
                            suggestedMin: 85,
                            suggestedMax: 100
                        }
                    }
                }
            });

            
            setInterval(() => {
              
                document.querySelectorAll('.card-value')[0].textContent = 
                    Math.floor(142 + Math.random() * 10);
                
                document.querySelectorAll('.card-value')[1].textContent = 
                    Math.floor(27 + Math.random() * 5);
            }, 10000);
        });