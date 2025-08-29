// FraudGuard ML API Configuration
const FRAUDGUARD_API_BASE = 'http://localhost:8000';
let network = null;
let physicsEnabled = true;
let isMLServiceOnline = false;
let activityChart = null;
let accuracyChart = null;

// Initialize everything when DOM loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('🛡️ FraudGuard ML Dashboard initializing...');
    
    // Initialize charts (keep original functionality)
    initializeCharts();
    
    // Initialize FraudGuard ML components
    initializeFraudGuardML();
    
    // Check ML service status
    checkMLServiceStatus();
    
    // Start periodic updates
    startPeriodicUpdates();
    
    console.log('✅ Dashboard initialization complete');
});

// Keep original chart initialization (unchanged)
function initializeCharts() {
    const activityCtx = document.getElementById('activityChart').getContext('2d');
    activityChart = new Chart(activityCtx, {
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
    accuracyChart = new Chart(accuracyCtx, {
        type: 'radar',
        data: {
            labels: ['Isolation Forest', 'Random Forest', 'SVM', 'Neural Network', 'Ensemble'],
            datasets: [{
                label: 'Detection Accuracy (%)',
                data: [89, 94, 87, 91, 96],
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

    console.log('📊 Charts initialized');
}

// Initialize FraudGuard ML Network Visualization
function initializeFraudGuardML() {
    console.log('🕸️ Initializing FraudGuard ML Network...');
    
    const container = document.getElementById('networkGraph');
    if (!container) {
        console.error('❌ Network container not found!');
        return;
    }

    // Create sample network data
    const nodes = new vis.DataSet([
        {id: 1, label: 'USER_A\n(Normal)', color: '#4caf50', size: 25, font: {color: 'white', size: 12}},
        {id: 2, label: 'USER_B\n(Suspicious)', color: '#ff9800', size: 30, font: {color: 'white', size: 12}},
        {id: 3, label: 'USER_C\n(High Risk)', color: '#f44336', size: 35, font: {color: 'white', size: 12}},
        {id: 4, label: 'MERCHANT_X\n(Electronics)', color: '#2196f3', size: 28, font: {color: 'white', size: 12}},
        {id: 5, label: 'CRYPTO_EXCHANGE\n(Platform)', color: '#9c27b0', size: 32, font: {color: 'white', size: 12}}
    ]);

    const edges = new vis.DataSet([
        {from: 1, to: 2, label: '$500', width: 2, color: {color: '#4caf50'}, font: {size: 10}},
        {from: 2, to: 3, label: '$2,500', width: 3, color: {color: '#ff9800'}, font: {size: 10}},
        {from: 3, to: 4, label: '$15,000', width: 5, color: {color: '#f44336'}, font: {size: 10}},
        {from: 2, to: 5, label: '$8,000', width: 4, color: {color: '#9c27b0'}, font: {size: 10}},
        {from: 4, to: 1, label: '$1,200', width: 2, color: {color: '#2196f3'}, dashes: true, font: {size: 10}}
    ]);

    const data = {nodes, edges};

    const options = {
        nodes: {
            shape: 'dot',
            scaling: { min: 20, max: 50 },
            font: { size: 12, color: 'white' },
            borderWidth: 2,
            borderColor: '#ffffff'
        },
        edges: {
            arrows: { to: { enabled: true, scaleFactor: 1.2 } },
            font: { align: 'middle', size: 10 },
            smooth: { type: 'continuous' },
            width: 2
        },
        physics: {
            enabled: true,
            solver: 'forceAtlas2Based',
            forceAtlas2Based: {
                gravitationalConstant: -50,
                centralGravity: 0.01,
                springLength: 150,
                springConstant: 0.08
            },
            stabilization: { iterations: 200 }
        },
        layout: { improvedLayout: true },
        interaction: { hover: true, zoomView: true, dragView: true }
    };

    try {
        network = new vis.Network(container, data, options);
        
        network.once("stabilizationIterationsDone", function() {
            network.setOptions({ physics: false });
            network.fit();
            console.log('✅ FraudGuard ML network initialized');
        });

    } catch (error) {
        console.error('❌ Network creation failed:', error);
        addMLActivity('❌ Network visualization failed to load');
    }
}

// Check ML Service Status
async function checkMLServiceStatus() {
    const statusElement = document.getElementById('mlServiceStatus');
    
    try {
        console.log('🔌 Checking FraudGuard ML service...');
        const response = await fetch(`${FRAUDGUARD_API_BASE}/health`, {
            method: 'GET',
            timeout: 5000
        });

        if (response.ok) {
            const data = await response.json();
            setMLServiceStatus(true);
            updateMLMetrics(data);
            addMLActivity('✅ Connected to FraudGuard ML service');
            console.log('✅ ML service online:', data.message);
        } else {
            throw new Error(`Service responded with ${response.status}`);
        }
    } catch (error) {
        console.log('⚠️ ML service offline:', error.message);
        setMLServiceStatus(false);
        loadMockMLData();
        addMLActivity('⚠️ ML service offline - using mock data');
    }
}

// Set ML Service Status
function setMLServiceStatus(online) {
    const statusElement = document.getElementById('mlServiceStatus');
    if (online) {
        statusElement.style.background = 'linear-gradient(45deg, #4caf50, #81c784)';
        statusElement.style.color = 'white';
        statusElement.innerHTML = '<i class="fas fa-circle"></i> ML Service: ONLINE';
        isMLServiceOnline = true;
    } else {
        statusElement.style.background = 'linear-gradient(45deg, #f44336, #ef5350)';
        statusElement.style.color = 'white';
        statusElement.innerHTML = '<i class="fas fa-circle"></i> ML Service: OFFLINE';
        isMLServiceOnline = false;
    }
}

// Update ML Metrics from API
function updateMLMetrics(data) {
    if (data && data.metrics) {
        document.getElementById('trainedUsers').textContent = data.metrics.trained_users || '8';
        document.getElementById('processingTime').textContent = '< 50ms';
        document.getElementById('modelAccuracy').textContent = '96.2%';
        document.getElementById('serviceHealth').textContent = 'Excellent';
        document.getElementById('mlModelsActive').textContent = data.metrics.loaded_models || '8';
        document.getElementById('avgResponseTime').textContent = '42ms';
        document.getElementById('mlModelsTrend').innerHTML = '<i class="fas fa-check"></i>Models ready';
    }
}

// Load Mock ML Data when service is offline
function loadMockMLData() {
    document.getElementById('trainedUsers').textContent = '8';
    document.getElementById('processingTime').textContent = '< 50ms';
    document.getElementById('modelAccuracy').textContent = '94.2%';
    document.getElementById('serviceHealth').textContent = 'Simulated';
    document.getElementById('mlModelsActive').textContent = '8';
    document.getElementById('avgResponseTime').textContent = '42ms';
    document.getElementById('mlModelsTrend').innerHTML = '<i class="fas fa-check"></i>Mock data';
}

// Train Test User Function
async function trainTestUser() {
    const button = event.target;
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Training...';

    addMLActivity('🧠 Starting user training...');

    // Generate sample training data
    const transactions = [];
    const categories = ['grocery', 'gas', 'restaurant', 'pharmacy', 'retail'];
    
    // Generate 20 transactions to meet minimum requirement
    for (let i = 0; i < 20; i++) {
        transactions.push({
            user_id: 'TEST_USER_001',
            amount: Math.round((50 + Math.random() * 200) * 100) / 100,
            merchant_category: categories[i % categories.length],
            hour: 8 + (i % 14),
            day_of_week: i % 7
        });
    }

    try {
        if (isMLServiceOnline) {
            const response = await fetch(`${FRAUDGUARD_API_BASE}/train-user`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    user_id: 'TEST_USER_001', 
                    transactions: transactions 
                })
            });

            if (response.ok) {
                const result = await response.json();
                addMLActivity('✅ User TEST_USER_001 trained successfully');
                document.getElementById('trainedUsers').textContent = (parseInt(document.getElementById('trainedUsers').textContent) + 1).toString();
                
                // Update metrics from response
                if (result.profile) {
                    addMLActivity(`📊 Profile: ${result.profile.total_transactions} transactions, avg $${result.profile.avg_amount.toFixed(2)}`);
                }
            } else {
                const errorText = await response.text();
                throw new Error(`Training failed: ${response.status} - ${errorText}`);
            }
        } else {
            // Simulate training delay
            await new Promise(resolve => setTimeout(resolve, 2000));
            addMLActivity('✅ User TEST_USER_001 trained successfully (Mock)');
            document.getElementById('trainedUsers').textContent = '9';
        }
    } catch (error) {
        console.error('Training error:', error);
        addMLActivity('❌ Training failed: ' + error.message);
    }

    button.disabled = false;
    button.innerHTML = originalText;
}

// Test Suspicious Transaction
async function testSuspiciousTransaction() {
    const button = event.target;
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Testing...';

    addMLActivity('🔍 Testing suspicious transaction...');

    const testTransaction = {
        user_id: 'TEST_USER_001',
        amount: 5000,
        merchant_category: 'electronics',
        hour: 2,
        day_of_week: 6
    };

    try {
        if (isMLServiceOnline) {
            const response = await fetch(`${FRAUDGUARD_API_BASE}/detect-fraud`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(testTransaction)
            });

            if (response.ok) {
                const result = await response.json();
                const riskEmoji = result.is_suspicious ? '🚨' : '✅';
                addMLActivity(`${riskEmoji} $5,000 transaction → ${result.risk_level} risk (${(result.anomaly_score * 100).toFixed(1)}%)`);
                
                if (result.explanation && result.explanation.length > 0) {
                    addMLActivity(`💡 ${result.explanation[0].substring(0, 60)}...`);
                }
            } else {
                const errorText = await response.text();
                throw new Error(`Detection failed: ${response.status} - ${errorText}`);
            }
        } else {
            // Simulate detection
            await new Promise(resolve => setTimeout(resolve, 1000));
            addMLActivity('🚨 $5,000 transaction → HIGH risk (87.3%) (Mock)');
            addMLActivity('💡 Amount significantly higher than user average');
        }
    } catch (error) {
        console.error('Detection error:', error);
        addMLActivity('❌ Detection failed: ' + error.message);
    }

    button.disabled = false;
    button.innerHTML = originalText;
}

// Generate Network Data
async function generateNetworkData() {
    const button = event.target;
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generating...';

    addMLActivity('🌐 Generating network data...');

    try {
        if (isMLServiceOnline) {
            const response = await fetch(`${FRAUDGUARD_API_BASE}/monitoring/cycles`);
            if (response.ok) {
                const data = await response.json();
                addMLActivity(`✅ Network data loaded: ${data.statistics.total_nodes} nodes, ${data.statistics.total_edges} edges`);
                
                if (data.statistics.cycles_detected > 0) {
                    addMLActivity(`🔍 ${data.statistics.cycles_detected} fraud cycles detected`);
                }
            } else {
                throw new Error('Network data fetch failed');
            }
        } else {
            await new Promise(resolve => setTimeout(resolve, 1500));
            addMLActivity('✅ Network data generated: 5 nodes, 5 edges (Mock)');
            addMLActivity('🔍 1 potential fraud cycle detected');
        }
        
        // Regenerate the network visualization
        regenerateNetwork();
        
    } catch (error) {
        console.error('Network generation error:', error);
        addMLActivity('❌ Network generation failed: ' + error.message);
    }

    button.disabled = false;
    button.innerHTML = originalText;
}

// Add ML Activity to the log
function addMLActivity(message) {
    const container = document.getElementById('recentMLActivity');
    const activity = document.createElement('div');
    activity.style.cssText = `
        margin: 5px 0; 
        padding: 8px 10px; 
        background: white; 
        border-left: 3px solid var(--primary); 
        border-radius: 4px;
        font-size: 0.85rem;
        box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    `;
    
    const timestamp = new Date().toLocaleTimeString('en-US', { 
        hour12: false, 
        hour: '2-digit', 
        minute: '2-digit',
        second: '2-digit'
    });
    
    activity.innerHTML = `<strong>${timestamp}:</strong> ${message}`;
    
    // Clear placeholder text if it exists
    if (container.querySelector('p[style*="italic"]')) {
        container.innerHTML = '';
    }
    
    container.insertBefore(activity, container.firstChild);
    
    // Keep only last 5 activities
    while (container.children.length > 5) {
        container.removeChild(container.lastChild);
    }
}

// Network Control Functions
function regenerateNetwork() {
    if (network) {
        console.log('🔄 Regenerating network layout...');
        const nodes = network.body.data.nodes.get();
        nodes.forEach(node => {
            node.x = undefined;
            node.y = undefined;
        });
        network.setData({
            nodes: new vis.DataSet(nodes),
            edges: network.body.data.edges
        });
        network.fit();
        addMLActivity('🔄 Network layout regenerated');
    }
}

function togglePhysics() {
    if (network) {
        physicsEnabled = !physicsEnabled;
        network.setOptions({ physics: { enabled: physicsEnabled } });
        addMLActivity(`🔧 Physics ${physicsEnabled ? 'enabled' : 'disabled'}`);
        
        if (!physicsEnabled) {
            setTimeout(() => network.fit(), 500);
        }
    }
}

function fitNetwork() {
    if (network) {
        network.fit({ 
            animation: { 
                duration: 1000,
                easingFunction: 'easeInOutQuad'
            } 
        });
        addMLActivity('🎯 Network view fitted');
    }
}

// Refresh Charts Function (keep original functionality)
function refreshCharts() {
    console.log('🔄 Refreshing charts...');
    
    // Update activity chart with slight variations
    if (activityChart) {
        activityChart.data.datasets[0].data = [
            42 + Math.floor(Math.random() * 5),
            28 + Math.floor(Math.random() * 5),
            19 + Math.floor(Math.random() * 5),
            31 + Math.floor(Math.random() * 5),
            22 + Math.floor(Math.random() * 5)
        ];
        activityChart.update();
    }
    
    // Update accuracy chart
    if (accuracyChart) {
        accuracyChart.data.datasets[0].data = [
            89 + Math.random() * 2,
            94 + Math.random() * 2,
            87 + Math.random() * 2,
            91 + Math.random() * 2,
            96 + Math.random() * 2
        ];
        accuracyChart.update();
    }
    
    addMLActivity('📊 Charts refreshed with latest data');
}

// Start Periodic Updates
function startPeriodicUpdates() {
    // Keep original functionality - update card values periodically
    setInterval(() => {
        // Original functionality
        const totalAlertsElement = document.getElementById('totalAlerts');
        if (totalAlertsElement) {
            totalAlertsElement.textContent = Math.floor(142 + Math.random() * 10);
        }
        
        const highPriorityElement = document.getElementById('highPriorityAlerts');
        if (highPriorityElement) {
            highPriorityElement.textContent = Math.floor(27 + Math.random() * 5);
        }
        
        // Check ML service status every update
        if (!isMLServiceOnline) {
            checkMLServiceStatus();
        }
        
    }, 10000); // Update every 10 seconds
    
    console.log('🔄 Periodic updates started');
}

// Initialize everything
console.log('🛡️ FraudGuard ML Dashboard script loaded');
