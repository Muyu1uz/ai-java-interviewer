import axios from 'axios';

const API_BASE_URL = 'http://localhost:8123/api'; // Replace with your actual API base URL

export const uploadResume = async (formData) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/resume/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    } catch (error) {
        throw new Error(error.response.data.message || 'Error uploading resume');
    }
};

export const getResume = async (userId) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/resume/${userId}`);
        return response.data;
    } catch (error) {
        throw new Error(error.response.data.message || 'Error fetching resume');
    }
};