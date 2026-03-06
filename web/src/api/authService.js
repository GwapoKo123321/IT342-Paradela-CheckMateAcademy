import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

const registerUser = async (userData) => {
    return await axios.post(`${API_URL}/register`, userData);
};

const loginUser = async (credentials) => {
    return await axios.post(`${API_URL}/login`, credentials);
};


const authService = {
    registerUser,
    loginUser
};

export default authService;