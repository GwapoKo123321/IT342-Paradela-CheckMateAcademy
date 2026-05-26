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

const getCoaches = async (filters = {}) => {
    const response = await apiClient.get('/users/coaches', { params: filters });
    return response.data;
};

const getAvailableSlots = async (filters = {}) => {
    const response = await apiClient.get('/users/coaches/available-slots', { params: filters });
    return response.data;
};

const getCoachProfile = async (coachId) => {
    const response = await apiClient.get(`/users/coaches/${coachId}/profile`);
    return response.data;
};

const saveCoachProfile = async (coachId, profile) => {
    const response = await apiClient.put(`/users/coaches/${coachId}/profile`, profile);
    return response.data;
};

const getLessonById = async (lessonId) => {
    const response = await apiClient.get(`/lessons/${lessonId}`);
    return response.data;
};

const saveLessonNotes = async (lessonId, notes) => {
    const response = await apiClient.put(`/lessons/${lessonId}/notes`, { notes: notes });
    return response.data;
};

const updateBoardState = async (lessonId, fen, pgn) => {
    const response = await apiClient.put(`/lessons/${lessonId}/board`, {
        boardState: fen,
        pgnHistory: pgn
    });
    return response.data;
};

const bookingService = {
    bookLesson, getCoachLessons, getStudentLessons, updateLessonStatus,
    getCoaches, getAvailableSlots, getCoachProfile, saveCoachProfile, getLessonById, saveLessonNotes, updateBoardState
};
export default bookingService;