import apiClient from './apiClient';

const bookLesson = async (bookingData) => {
    return await apiClient.post('/lessons', bookingData);
};

const getCoachLessons = async (coachId) => {
    const response = await apiClient.get(`/lessons/coach/${coachId}`);
    return response.data;
};

const updateLessonStatus = async (lessonId, status) => {
    const response = await apiClient.put(`/lessons/${lessonId}/status?status=${status}`);
    return response.data;
};

export default { bookLesson, getCoachLessons, updateLessonStatus };