import axios from 'axios';

// The Singleton Instance
const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Adding a request interceptor (acts as a Singleton helper for Auth tokens)
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`; // Required by SDD 5.1 [cite: 191]
    }
    return config;
});

export default apiClient;