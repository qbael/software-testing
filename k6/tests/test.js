import http from 'k6/http';
import { sleep, check } from 'k6';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

export const options = {
    stages: [
        { duration: '30s', target: 20 },  // Tăng dần lên 20 users
        { duration: '1m', target: 20 },   // Giữ ở 20 users
        { duration: '20s', target: 0 },   // Giảm về 0
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% request < 500ms
        http_req_failed: ['rate<0.1'],    // Error rate < 10%
    },
};

export default function () {
    const res = http.get('https://test.k6.io');

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}

export function handleSummary(data) {
    return {
        "../summary.html": htmlReport(data),
        "../summary.json": JSON.stringify(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}