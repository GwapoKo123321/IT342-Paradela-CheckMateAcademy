import apiClient from '../core/apiClient';

const loginUser = async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    if (response.data.success) {
        localStorage.setItem('token', response.data.data.accessToken);
        localStorage.setItem('user', JSON.stringify(response.data.data.user));
        return response.data.data;
    }
    throw new Error("Login failed");
};

const registerUser = async (userData) => {
    return await apiClient.post('/auth/register', userData);
};

const updateProfile = async (userId, profileData) => {
    const response = await apiClient.put(`/users/profile/update/${userId}`, profileData);
    return response.data;
};

export default { loginUser, registerUser, updateProfile };