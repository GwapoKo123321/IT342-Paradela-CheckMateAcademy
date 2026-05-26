import apiClient from '../core/apiClient';

const submitReport = async (reportData) => {
    const response = await apiClient.post('/reports', reportData);
    return response.data;
};

const getAllReports = async () => {
    const response = await apiClient.get('/reports');
    return response.data;
};

const resolveReport = async (reportId) => {
    const response = await apiClient.put(`/reports/${reportId}/resolve`);
    return response.data;
};

const reportService = { submitReport, getAllReports, resolveReport };
export default reportService;