import apiClient from '../core/apiClient';

const getAllUsers = async () => {
    const response = await apiClient.get('/admin/users');
    return response.data;
};

const updateUserRole = async (userId, role) => {
    const response = await apiClient.put(`/admin/users/${userId}/role`, { role });
    return response.data;
};

const deleteUser = async (userId) => {
    const response = await apiClient.delete(`/admin/users/${userId}`);
    return response.data;
};

const toggleUserFlag = async (userId) => {
    const response = await apiClient.put(`/admin/users/${userId}/flag`);
    return response.data;
};

const toggleEloVerification = async (userId) => {
    const response = await apiClient.put(`/admin/users/${userId}/verify-elo`);
    return response.data;
};

const adminService = { getAllUsers, updateUserRole, deleteUser, toggleUserFlag, toggleEloVerification };
export default adminService;