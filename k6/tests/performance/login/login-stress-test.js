import http from 'k6/http';
import {sleep} from 'k6';
import {Counter, Gauge, Rate, Trend} from 'k6/metrics';
import {textSummary} from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const totalRequests = new Counter('total_requests');

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
            tags: { scenario_name: 'stress_test' },
            gracefulRampDown: '30s',
        },
    },

    thresholds: {
        'http_req_duration{scenario_name:stress_test}': ['p(95)<5000'],
        'http_req_failed{scenario_name:stress_test}': ['rate<0.05'],
        'http_req_waiting{scenario_name:stress_test}': ['p(95)<4000'],
        'total_requests': ['count>0'],
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

        const res = http.post(
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

        if (res.status === 201 || res.status === 409) {
            users.push(user);
        }
    }

    return {
        users,
        startTime: Date.now()
    };
}

export default function (data) {
    const user = data.users[Math.floor(Math.random() * data.users.length)];

    const payload = JSON.stringify({
        username: user.username,
        password: user.password,
    });

    const params = {
        headers: {'Content-Type': 'application/json'},
        timeout: '15s',
    };

    const startTime = Date.now();
    const response = http.post(`${BASE_URL}/api/auth/login`, payload, params);
    const duration = Date.now() - startTime;

    totalRequests.add(1);

    const success = response.status === 200;

    if (!success) {
        if (duration > 10000) {
            console.error(
                `Login failed | Status: ${response.status} | ` +
                `User: ${user.username} | ` +
                `Duration: ${duration}ms | ` +
                `Error: ${response.error}`
            );
        }
    }

    sleep(1);
}

export function teardown(data) {
    const endTime = Date.now();
    const totalDuration = ((endTime - data.startTime) / 1000).toFixed(2);

    console.log('Stress Test Completed!');
    console.log(`Total Duration: ${totalDuration}s`);
    console.log(`Test Users: ${data.users.length}`);
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    };
}