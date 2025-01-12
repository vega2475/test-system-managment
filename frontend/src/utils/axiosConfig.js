import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8081/api',
});

instance.interceptors.request.use(
    config => {
        const token = localStorage.getItem('jwt');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

instance.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            alert("Session expired. Please log in again.")
            logoutUser();
        }
        return Promise.reject(error);
    }
);

function logoutUser() {
    localStorage.removeItem('jwt');
    window.location.href = '/login';
}

export default instance;