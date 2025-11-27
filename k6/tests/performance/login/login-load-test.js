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
        load_100_users: {
            executor: 'constant-vus',
            vus: 100,
            duration: '1m',
            startTime: '0s',
            tags: { test_type: 'load_100', scenario_name: 'load_100_users' },
            gracefulStop: '30s',
        },

        load_500_users: {
            executor: 'constant-vus',
            vus: 500,
            duration: '1m',
            startTime: '1.5m',
            tags: { test_type: 'load_500', scenario_name: 'load_500_users' },
            gracefulStop: '30s',
        },

        load_1000_users: {
            executor: 'constant-vus',
            vus: 1000,
            duration: '1m',
            startTime: '3m',
            tags: { test_type: 'load_1000', scenario_name: 'load_1000_users' },
            gracefulStop: '30s',
        },
    },

    thresholds: {
        'http_req_duration{test_type:load_100}': ['p(95)<1000'],
        'http_req_duration{test_type:load_500}': ['p(95)<2000'],
        'http_req_duration{test_type:load_1000}': ['p(95)<3000'],

        'http_req_failed{test_type:load_100}': ['rate<0.01'],
        'http_req_failed{test_type:load_500}': ['rate<0.02'],
        'http_req_failed{test_type:load_1000}': ['rate<0.03'],

        'http_req_waiting{test_type:load_100}': ['p(95)<800'],
        'http_req_waiting{test_type:load_500}': ['p(95)<1500'],
        'http_req_waiting{test_type:load_1000}': ['p(95)<2500'],

        'total_requests': ['count>0'],
        'scenario_requests{scenario_name:load_100_users}': ['count>0'],
        'scenario_requests{scenario_name:load_500_users}': ['count>0'],
        'scenario_requests{scenario_name:load_1000_users}': ['count>0'],
    },

    setupTimeout: '2m',
};

const BASE_URL = 'http://localhost:8080';

export function setup() {
    const users = [];
    const userCount = 1000;

    for (let i = 1; i <= userCount; i++) {
        const user = {
            username: `usertest${i}`,
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
        }
    }

    return {
        users,
        startTime: Date.now(),
        testId: `load-test-${Date.now()}`,
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
            'User-Agent': 'k6-load-test',
        },
        tags: {
            name: 'POST /api/auth/login',
            endpoint: 'login',
        },
        timeout: '10s',
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
        console.error(
            `Login failed | Status: ${response.status} | ` +
            `User: ${user.username} | ` +
            `Duration: ${duration}ms | ` +
            `Error: ${response.error}`
        );
    }

    const thinkTime = Math.random() * 3 + 1;
    sleep(thinkTime);

    activeUsers.add(-1);
}

export function teardown(data) {
    const endTime = Date.now();
    const totalDuration = ((endTime - data.startTime) / 1000).toFixed(2);

    console.log('Load Test Completed!');
    console.log(`Test ID: ${data.testId}`);
    console.log(`Total Duration: ${totalDuration}s`);
    console.log(`Test Users: ${data.users.length}`);
}

export function handleSummary(data) {
    const totalReqs = data.metrics.http_reqs?.values.count || 0;

    console.log("=".repeat(60));
    console.log("🏁 LOAD TEST EXECUTION COMPLETED");
    console.log("=".repeat(60));
    console.log(`📊 TỔNG SỐ REQUEST: ${totalReqs}`);
    console.log("=".repeat(60));

    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    };
}