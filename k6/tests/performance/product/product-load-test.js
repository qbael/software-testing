import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import {textSummary} from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const errorRate = new Rate('errors');
const productCreationTime = new Trend('product_creation_duration');
const productUpdateTime = new Trend('product_update_duration');
const productDeleteTime = new Trend('product_delete_duration');
const productGetTime = new Trend('product_get_duration');
const successfulRequests = new Counter('successful_requests');

export const options = {
    stages: [
        { duration: '1m', target: 100 },
        { duration: '1m', target: 500 },
        { duration: '1m', target: 1000 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.05'],
        errors: ['rate<0.1'],
        successful_requests: ['count>1000'],
    },
};

const BASE_URL = 'http://localhost:8080';

const categories = ['SMARTPHONE', 'LAPTOPS', 'HEADPHONES', 'CAMERAS'];

function generateRandomProduct() {
    const randomId = Math.floor(Math.random() * 100000);
    return {
        productName: `Test Product ${randomId}`,
        price: Math.floor(Math.random() * 10000) + 100,
        quantity: Math.floor(Math.random() * 100) + 1,
        description: `This is a test product description for product ${randomId}. It contains enough text to be valid.`,
        category: categories[Math.floor(Math.random() * categories.length)]
    };
}

function login() {

    const number = Math.floor(Math.random() * 2500);

    const loginPayload = JSON.stringify({
        username: `stresstest${number}`,
        password: `Password${number}`,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
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

    // Test 1: Get all products (paginated)
    group('Get All Products', () => {
        const page = Math.floor(Math.random() * 5);
        const limit = [10, 20, 50][Math.floor(Math.random() * 3)];
        const sortBy = ['id', 'productName', 'price'][Math.floor(Math.random() * 3)];
        const sortDir = Math.random() > 0.5 ? 'asc' : 'desc';

        const res = http.get(
            `${BASE_URL}/api/products?page=${page}&limit=${limit}&sortBy=${sortBy}&sortDir=${sortDir}`,
            { headers: authHeaders }
        );

        const success = check(res, {
            'get all products status is 200': (r) => r.status === 200,
            'get all products has content': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.content !== undefined;
                } catch (e) {
                    return false;
                }
            },
            'get all products response time < 500ms': (r) => r.timings.duration < 500,
        });

        if (success) {
            successfulRequests.add(1);
        } else {
            errorRate.add(1);
        }

        productGetTime.add(res.timings.duration);
    });

    sleep(1);

    // Test 2: Create a new product
    let createdProductId = null;
    group('Create Product', () => {
        const newProduct = generateRandomProduct();
        const payload = JSON.stringify(newProduct);

        const res = http.post(
            `${BASE_URL}/api/products`,
            payload,
            { headers: authHeaders }
        );

        const success = check(res, {
            'create product status is 200': (r) => r.status === 200,
            'create product has id': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    if (body.id) {
                        createdProductId = body.id;
                        return true;
                    }
                    return false;
                } catch (e) {
                    return false;
                }
            },
            'create product response time < 800ms': (r) => r.timings.duration < 800,
        });

        if (success) {
            successfulRequests.add(1);
        } else {
            errorRate.add(1);
        }

        productCreationTime.add(res.timings.duration);
    });

    sleep(1);

    // Test 3: Get product by ID
    if (createdProductId) {
        group('Get Product By ID', () => {
            const res = http.get(
                `${BASE_URL}/api/products/${createdProductId}`,
                { headers: authHeaders }
            );

            const success = check(res, {
                'get product by id status is 200': (r) => r.status === 200,
                'get product by id has correct data': (r) => {
                    try {
                        const body = JSON.parse(r.body);
                        return body.id === createdProductId;
                    } catch (e) {
                        return false;
                    }
                },
                'get product by id response time < 300ms': (r) => r.timings.duration < 300,
            });

            if (success) {
                successfulRequests.add(1);
            } else {
                errorRate.add(1);
            }

            productGetTime.add(res.timings.duration);
        });

        sleep(1);

        // Test 4: Update product
        group('Update Product', () => {
            const updatedProduct = generateRandomProduct();
            updatedProduct.productName = `Updated ${updatedProduct.productName}`;
            const payload = JSON.stringify(updatedProduct);

            const res = http.put(
                `${BASE_URL}/api/products/${createdProductId}`,
                payload,
                { headers: authHeaders }
            );

            const success = check(res, {
                'update product status is 200': (r) => r.status === 200,
                'update product has updated data': (r) => {
                    try {
                        const body = JSON.parse(r.body);
                        return body.productName.includes('Updated');
                    } catch (e) {
                        return false;
                    }
                },
                'update product response time < 600ms': (r) => r.timings.duration < 600,
            });

            if (success) {
                successfulRequests.add(1);
            } else {
                errorRate.add(1);
            }

            productUpdateTime.add(res.timings.duration);
        });

        sleep(1);

        // Test 5: Delete product
        group('Delete Product', () => {
            const res = http.del(
                `${BASE_URL}/api/products/${createdProductId}`,
                null,
                { headers: authHeaders }
            );

            const success = check(res, {
                'delete product status is 200': (r) => r.status === 200,
                'delete product response time < 500ms': (r) => r.timings.duration < 500,
            });

            if (success) {
                successfulRequests.add(1);
            } else {
                errorRate.add(1);
            }

            productDeleteTime.add(res.timings.duration);
        });
    }

    sleep(1);

    group('Search and Filter Products', () => {
        const scenarios = [
            { page: 0, limit: 10, sortBy: 'productName', sortDir: 'asc' },
            { page: 0, limit: 20, sortBy: 'price', sortDir: 'desc' },
            { page: 1, limit: 50, sortBy: 'quantity', sortDir: 'asc' },
        ];

        scenarios.forEach((scenario, index) => {
            const res = http.get(
                `${BASE_URL}/api/products?page=${scenario.page}&limit=${scenario.limit}&sortBy=${scenario.sortBy}&sortDir=${scenario.sortDir}`,
                { headers: authHeaders }
            );

            const success = check(res, {
                [`scenario ${index + 1} status is 200`]: (r) => r.status === 200,
                [`scenario ${index + 1} response time < 500ms`]: (r) => r.timings.duration < 500,
            });

            if (success) {
                successfulRequests.add(1);
            } else {
                errorRate.add(1);
            }
        });
    });

    sleep(1);
}

export function teardown(data) {
    console.log('Test completed at:', new Date().toISOString());
}

export function handleSummary(data) {
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    };
}