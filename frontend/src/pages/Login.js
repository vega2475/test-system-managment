import React, {useState} from 'react';
import axios from '../utils/axiosConfig';
import '../styles/Login.css';
import {useNavigate} from 'react-router-dom';
import {Button} from "react-bootstrap";

const Login = ({onLogin}) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async () => {
        try {
            setIsLoading(true);
            const response = await axios.post('/auth/login', {
                email,
                password
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.status === 200) {
                setIsLoading(false);
                const token = response.data.jwt;
                localStorage.setItem('jwt', token);
                const userResponse = await axios.get('/auth/me', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (userResponse.status === 200) {
                    localStorage.setItem('user', userResponse.data.id)
                    setUser(userResponse.data);
                }
                alert('Logged in successfully');
                onLogin();
                navigate('/tests');
            } else {
                alert('Error logging in');
            }
        } catch (error) {
            if (error.response) {
                const {status} = error.response;
                if (status === 401) {
                    alert('Incorrect credentials');
                } else {
                    alert('Error logging in');
                }
            } else {
                alert('Error setting up request');
            }
        }
    };

    return (
        <div className="login">
            <div className="login-container">
                <h2>Login</h2>
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <Button onClick={handleLogin}>
                    {!isLoading ? 'Login' : 'Loading...'}
                </Button>
                <p>Don't have an account? <a href="/register">Register</a></p>
            </div>
        </div>
    );
};

export default Login;