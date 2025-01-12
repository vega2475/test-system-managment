import React, {useState} from 'react';
import axios from '../utils/axiosConfig';
import '../styles/Register.css';
import {Link, useNavigate} from 'react-router-dom';
import {Button} from "react-bootstrap";

const Register = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const handleRegister = async () => {
        if (password !== confirmPassword) {
            alert('Passwords do not match');
            return;
        }

        try {
            setIsLoading(true);
            const response = await axios.post('/auth/register', {
                email,
                password
            }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.status === 200) {
                setIsLoading(false);
                alert('Registered successfully\n' +
                    'Please check your email for confirm.');
                navigate('/login')
            } else {
                alert('Registration failed');
            }
        } catch (error) {
            if (error.response) {
                const {status, data} = error.response;

                if (status === 400 && typeof data === 'object') {
                    const errorMessages = Object.values(data).join('\n');
                    alert(`Validation errors:\n${errorMessages}`);
                } else {
                    alert(`Error: ${data}`);
                }
            } else if (error.request) {
                alert('No response received from server');
            } else {
                alert('Error setting up request');
            }
        }
    };

    return (
        <div className="register">
            <div className="register-container">
                <h2>Register</h2>
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
                <input
                    type="password"
                    placeholder="Confirm Password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />
                <Button onClick={handleRegister}>
                    {!isLoading ? 'Register' : 'Loading...'}
                </Button>
                <p className="login-link">
                    Already have an account? <Link to="/login">Login</Link>
                </p>
            </div>
        </div>
    );
};

export default Register;