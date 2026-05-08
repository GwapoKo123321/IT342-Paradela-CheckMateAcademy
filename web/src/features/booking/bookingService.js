import apiClient from '../core/apiClient';

const bookLesson = async (bookingData) => {
    try {
        const response = await apiClient.post('/lessons', bookingData);
        return response.data;
    } catch (error) {
        if (error.response && error.response.data && error.response.data.error) {
            throw new Error(error.response.data.error);
        }
        throw new Error("Failed to connect to the server.");
    }
};

const getCoachLessons = async (coachId) => {
    const response = await apiClient.get(`/lessons/coach/${coachId}`);
    return response.data;
};

const getStudentLessons = async (studentId) => {
    const response = await apiClient.get(`/lessons/student/${studentId}`);
    return response.data;
};

const updateLessonStatus = async (lessonId, status) => {
    const response = await apiClient.put(`/lessons/${lessonId}/status?status=${status}`);
    return response.data;
};

const getCoaches = async () => {
    const response = await apiClient.get('/users/coaches');
    return response.data;
};

const getLessonById = async (lessonId) => {
    const response = await apiClient.get(`/lessons/${lessonId}`);
    return response.data;
};

const saveLessonNotes = async (lessonId, notes) => {
    const response = await apiClient.put(`/lessons/${lessonId}/notes`, notes, {
        headers: { 'Content-Type': 'text/plain' }
    });
    return response.data;
};


const updateBoardState = async (lessonId, boardState) => {
    const response = await apiClient.put(`/lessons/${lessonId}/board`, boardState, {
        headers: { 'Content-Type': 'text/plain' }
    });
    return response.data;
};

export default {
    bookLesson, getCoachLessons, getStudentLessons, updateLessonStatus,
    getCoaches, getLessonById, saveLessonNotes, updateBoardState
};