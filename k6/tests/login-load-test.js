import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const loginSuccessRate = new Rate('login_success_rate');
const loginDuration = new Trend('login_duration');
const loginErrors = new Counter('login_errors');

export const totalAccounts = 1000;

export const options = {
    scenarios: {
        load_100_users: {
            executor: 'constant-vus',
            vus: 100,
            duration: '5m',
            startTime: '0s',
            tags: { test_type: 'load_100' },
        },

        load_500_users: {
            executor: 'constant-vus',
            vus: 500,
            duration: '5m',
            startTime: '6m',
            tags: { test_type: 'load_500' },
        },

        load_1000_users: {
            executor: 'constant-vus',
            vus: 1000,
            duration: '5m',
            startTime: '12m',
            tags: { test_type: 'load_1000' },
        },

        stress_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '2m', target: 500 },
                { duration: '3m', target: 500 },
                { duration: '2m', target: 1000 },
                { duration: '3m', target: 1000 },
                { duration: '2m', target: 2000 },
                { duration: '3m', target: 2000 },
                { duration: '2m', target: 3000 },
                { duration: '5m', target: 3000 },
                { duration: '2m', target: 0 },
            ],
            startTime: '18m',
            tags: { test_type: 'stress' },
        },

        spike_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 100 },
                { duration: '1m', target: 100 },
                { duration: '30s', target: 2000 },
                { duration: '3m', target: 2000 },
                { duration: '30s', target: 100 },
                { duration: '1m', target: 100 },
                { duration: '30s', target: 0 },
            ],
            startTime: '42m',
            tags: { test_type: 'spike' },
        },
    },

    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.05'],
        login_success_rate: ['rate>0.95'],
        login_duration: ['p(95)<600'],
    },
};

const BASE_URL = 'http://localhost:8080';

function generateTestUser(number) {
    const randomNum = Math.floor(Math.random() * 1000);
    return {
        username: `testuser${randomNum}`,
        password: `Password${randomNum}`,
    };
}

export function setup() {
    const users = [];
    for (let i = 0; i < totalAccounts; i++) {
        const user = generateTestUser();

        http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
            username: user.username,
            password: user.password,
            verifyPassword: user.password,
        }), {
            headers: { 'Content-Type': 'application/json' },
        });

        users.push(user);
    }

    return { users };
}

export default function (data) {
    const user = data.users[Math.floor(Math.random() * data.users.length)];

    const payload = JSON.stringify({
        username: user.username,
        password: user.password,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            name: 'LoginAPI',
        },
    };

    const startTime = Date.now();
    const response = http.post(`${BASE_URL}/api/auth/login`, payload, params);
    const duration = Date.now() - startTime;

    loginDuration.add(duration);

    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has body': (r) => r.body.length > 0,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
        'has username in response': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.username !== undefined;
            } catch (e) {
                return false;
            }
        },
    });

    loginSuccessRate.add(success);

    if (!success) {
        loginErrors.add(1);
        console.log(`Login failed: Status ${response.status}, Body: ${response.body}`);
    }
    //
    // if (response.status === 200) {
    //     const cookies = response.cookies;
    //     if (cookies.jwt && cookies.jwt.length > 0) {
    //         const jwtToken = cookies.jwt[0].value;
    //
    //     }
    // }

    sleep(Math.random() * 2 + 1);
}

export function teardown(data) {
    console.log(`Total users tested: ${data.users.length}`);
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        'summary.json': JSON.stringify(data),
    };
}

function textSummary(data, options) {
    const indent = options.indent || '';
    const enableColors = options.enableColors || false;

    let summary = '\n' + indent + '=== Performance Test Summary ===\n\n';

    summary += indent + 'Scenarios Executed:\n';
    for (const [name, scenario] of Object.entries(data.root_group.groups)) {
        summary += indent + `  - ${name}\n`;
    }

    summary += '\n' + indent + 'Key Metrics:\n';
    summary += indent + `  Total Requests: ${data.metrics.http_reqs.values.count}\n`;
    summary += indent + `  Failed Requests: ${data.metrics.http_req_failed.values.rate * 100}%\n`;
    summary += indent + `  Avg Response Time: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms\n`;
    summary += indent + `  P95 Response Time: ${data.metrics['http_req_duration{p(95)}']?.toFixed(2) || 'N/A'}ms\n`;
    summary += indent + `  P99 Response Time: ${data.metrics['http_req_duration{p(99)}']?.toFixed(2) || 'N/A'}ms\n`;

    if (data.metrics.login_success_rate) {
        summary += indent + `  Login Success Rate: ${(data.metrics.login_success_rate.values.rate * 100).toFixed(2)}%\n`;
    }

    summary += '\n' + indent + 'Thresholds:\n';
    for (const [name, threshold] of Object.entries(data.metrics)) {
        if (threshold.thresholds) {
            for (const [tname, tvalue] of Object.entries(threshold.thresholds)) {
                const status = tvalue.ok ? '✓ PASS' : '✗ FAIL';
                summary += indent + `  ${status}: ${name} - ${tname}\n`;
            }
        }
    }

    return summary;
}