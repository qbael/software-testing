import http from 'k6/http';
import {group, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';
import {textSummary} from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const errorRate = new Rate('errors');
const productCreationTime = new Trend('product_creation_duration');
const productUpdateTime = new Trend('product_update_duration');
const productDeleteTime = new Trend('product_delete_duration');
const productGetTime = new Trend('product_get_duration');

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '30s', target: 400 },
        { duration: '30s', target: 600 },
        { duration: '30s', target: 800 },
        { duration: '30s', target: 1000 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_waiting: ['p(95)<900'],
        http_req_failed: ['rate<0.05'],
        errors: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8080';

const categories = ['SMARTPHONE', 'LAPTOPS', 'HEADPHONES', 'CAMERAS'];

export function setup() {
    const user = {
        username: `mindang1`,
        password: `mindang1`,
        verifyPassword: `mindang1`
    }

    http.post(`${BASE_URL}/api/auth/register`, JSON.stringify(user), {
        headers: { 'Content-Type': 'application/json' },
        timeout: '30s',
    })
}

function generateRandomProduct() {
    const random = Math.floor(Math.random() * 100000);
    return {
        productName: `Test Product ${random}`,
        price: Math.floor(Math.random() * 10000) + 100,
        quantity: Math.floor(Math.random() * 100) + 1,
        description: `Description for product ${random}`,
        category: categories[Math.floor(Math.random() * categories.length)]
    };
}

function login() {

    const loginPayload = JSON.stringify({
        username: `mindang1`,
        password: `mindang1`,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '30s',
    };

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

    if (loginRes.status === 200) {
        const cookies = loginRes.cookies;
        if (cookies.token && cookies.token.length > 0) {
            return cookies.token[0].value;
        }
    }

    return null;
}

function getAuthHeaders(token) {
    return {
        'Content-Type': 'application/json',
        'Cookie': `token=${token}`
    };
}

export default function(data) {
    const token = login();

    if (!token) {
        errorRate.add(1);
        console.error('Failed to login and get JWT token');
        return;
    }

    const authHeaders = getAuthHeaders(token);

    group('Get All Products', () => {
        const page = Math.floor(Math.random() * 5);
        const limit = [10, 20, 50][Math.floor(Math.random() * 3)];
        const sortBy = ['id', 'productName', 'price'][Math.floor(Math.random() * 3)];
        const sortDir = Math.random() > 0.5 ? 'asc' : 'desc';

        const res = http.get(
            `${BASE_URL}/api/products?page=${page}&limit=${limit}&sortBy=${sortBy}&sortDir=${sortDir}`,
            { headers: authHeaders }
        );

        const success = res.status === 200

        if (!success) {
            errorRate.add(1);
        }

        productGetTime.add(res.timings.duration);
    });

    sleep(1);

    let createdProductId = null;
    group('Create Product', () => {
        const newProduct = generateRandomProduct();
        const payload = JSON.stringify(newProduct);

        const res = http.post(
            `${BASE_URL}/api/products`,
            payload,
            { headers: authHeaders }
        );

        const success = res.status === 200

        if (!success) {
            errorRate.add(1);
        } else {
            try {
                const body = JSON.parse(res.body);
                if (body.id) {
                    createdProductId = body.id;
                }
            } catch (e) {
                errorRate.add(1);
            }
        }

        productCreationTime.add(res.timings.duration);
    });

    sleep(1);

    if (createdProductId) {
        group('Get Product By ID', () => {
            const res = http.get(
                `${BASE_URL}/api/products/${createdProductId}`,
                { headers: authHeaders }
            );

            const success = res.status === 200

            if (!success) {
                errorRate.add(1);
            }

            productGetTime.add(res.timings.duration);
        });

        sleep(1);

        group('Update Product', () => {
            const updatedProduct = generateRandomProduct();
            const payload = JSON.stringify(updatedProduct);

            const res = http.put(
                `${BASE_URL}/api/products/${createdProductId}`,
                payload,
                { headers: authHeaders }
            );

            const success = res.status === 200

            if (!success) {
                errorRate.add(1);
            }

            productUpdateTime.add(res.timings.duration);
        });

        sleep(1);

        group('Delete Product', () => {
            const res = http.del(
                `${BASE_URL}/api/products/${createdProductId}`,
                null,
                { headers: authHeaders }
            );

            const success = res.status === 200

            if (!success) {
                errorRate.add(1);
            }

            productDeleteTime.add(res.timings.duration);
        });
    }

    sleep(1);
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    };
}