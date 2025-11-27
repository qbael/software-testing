// ============================================
// stress-test.js
// Stress testing to find system limits
// ============================================

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');

export const options = {
    stages: [
        { duration: '2m', target: 100 },
        { duration: '5m', target: 100 },
        { duration: '2m', target: 200 },
        { duration: '5m', target: 200 },
        { duration: '2m', target: 300 },
        { duration: '5m', target: 300 },
        { duration: '2m', target: 400 },
        { duration: '5m', target: 400 },
        { duration: '5m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(99)<5000'],
        http_req_failed: ['rate<0.1'],
        errors: ['rate<0.15'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

function login() {
    const loginPayload = JSON.stringify({
        username: 'testuser',
        password: 'Password123'
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

    if (loginRes.status === 200 && loginRes.cookies.jwt) {
        return loginRes.cookies.jwt[0].value;
    }

    return null;
}

export default function() {
    const token = login();

    if (!token) {
        errorRate.add(1);
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        'Cookie': `jwt=${token}`
    };

    const res = http.get(
        `${BASE_URL}/api/products?page=0&limit=50&sortBy=id&sortDir=asc`,
        { headers }
    );

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
        'response time OK': (r) => r.timings.duration < 3000,
    });

    if (!success) {
        errorRate.add(1);
    }

    responseTime.add(res.timings.duration);
    sleep(0.5);
}