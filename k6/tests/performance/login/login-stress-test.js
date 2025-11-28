import http from 'k6/http';
import {sleep} from 'k6';
import {Counter, Gauge, Rate, Trend} from 'k6/metrics';
import {textSummary} from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const loginSuccessRate = new Rate('login_success_rate');
const loginDuration = new Trend('login_duration');
const loginErrors = new Counter('login_errors');
const activeUsers = new Gauge('active_users');

const totalRequests = new Counter('total_requests');
const scenarioRequests = new Counter('scenario_requests');

export const options = {
    scenarios: {
        stress_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 1000 },
                { duration: '1m', target: 1500 },
                { duration: '1m', target: 2000 },
                { duration: '1m', target: 2500 },
                { duration: '1m', target: 0 },
            ],
            startTime: '0s',
            tags: { test_type: 'stress', scenario_name: 'stress_test' },
            gracefulRampDown: '30s',
        },
    },

    thresholds: {
        'http_req_duration{test_type:stress}': ['p(95)<5000'],
        'http_req_failed{test_type:stress}': ['rate<0.05'],
        'http_req_waiting{test_type:stress}': ['p(95)<4000'],
        'total_requests': ['count>0'],
        'scenario_requests{scenario_name:stress_test}': ['count>0'],
    },

    setupTimeout: '3m',
};

const BASE_URL = 'http://localhost:8080';

export function setup() {
    const users = [];
    const userCount = 2000;

    for (let i = 1; i <= userCount; i++) {
        const user = {
            username: `stresstest${i}`,
            password: `Password${i}`,
        };

        const regResponse = http.post(
            `${BASE_URL}/api/auth/register`,
            JSON.stringify({
                username: user.username,
                password: user.password,
                verifyPassword: user.password,
            }),
            {
                headers: { 'Content-Type': 'application/json' },
                timeout: '30s',
            }
        );

        if (regResponse.status === 201 || regResponse.status === 409) {
            users.push(user);
        } else {
            console.warn(`Failed to register user: ${user.username}, Status: ${regResponse.status}`);
        }
    }

    console.log(`Registered ${users.length} users for stress testing`);

    return {
        users,
        startTime: Date.now(),
        testId: `stress-test-${Date.now()}`,
    };
}

export default function (data) {
    const user = data.users[Math.floor(Math.random() * data.users.length)];

    activeUsers.add(1);

    const payload = JSON.stringify({
        username: user.username,
        password: user.password,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'User-Agent': 'k6-stress-test',
        },
        tags: {
            name: 'POST /api/auth/login',
            endpoint: 'login',
        },
        timeout: '15s',
    };

    const startTime = Date.now();
    const response = http.post(`${BASE_URL}/api/auth/login`, payload, params);
    const duration = Date.now() - startTime;

    totalRequests.add(1);
    scenarioRequests.add(1);
    loginDuration.add(duration);

    const success = response.status === 200;
    loginSuccessRate.add(success);

    if (!success) {
        loginErrors.add(1);
        if (duration > 5000) {
            console.error(
                `Login failed | Status: ${response.status} | ` +
                `User: ${user.username} | ` +
                `Duration: ${duration}ms | ` +
                `Error: ${response.error}`
            );
        }
    }

    const thinkTime = Math.random() * 2 + 0.5;
    sleep(thinkTime);

    activeUsers.add(-1);
}

export function teardown(data) {
    const endTime = Date.now();
    const totalDuration = ((endTime - data.startTime) / 1000).toFixed(2);

    console.log('Stress Test Completed!');
    console.log(`Test ID: ${data.testId}`);
    console.log(`Total Duration: ${totalDuration}s`);
    console.log(`Test Users: ${data.users.length}`);
}

export function handleSummary(data) {
    const totalReqs = data.metrics.http_reqs?.values.count || 0;
    const failedReqs = data.metrics.http_req_failed?.values.rate || 0;
    const avgDuration = data.metrics.http_req_duration?.values.avg || 0;

    console.log("=".repeat(60));
    console.log("🔥 STRESS TEST EXECUTION COMPLETED");
    console.log("=".repeat(60));
    console.log(`📊 TỔNG SỐ REQUEST: ${totalReqs}`);
    console.log(`❌ TỶ LỆ FAILED: ${(failedReqs * 100).toFixed(2)}%`);
    console.log(`⏱️ THỜI GIAN TRUNG BÌNH: ${avgDuration.toFixed(2)}ms`);
    console.log("=".repeat(60));

    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    };
}