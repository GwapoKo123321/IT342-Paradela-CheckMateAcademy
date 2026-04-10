import apiClient from './apiClient'; // Using the Singleton

const registerUser = async (userData) => {
    // Facade: Simplifies the call for the UI components
    return await apiClient.post('/auth/register', userData);
};

const loginUser = async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    if (response.data.accessToken) {
        localStorage.setItem('token', response.data.accessToken); // Aligns with SDD [cite: 198]
    }
    return response.data;
};

const authService = {
    registerUser,
    loginUser
};

export default authService;