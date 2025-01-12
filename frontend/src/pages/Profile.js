import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Form, Button, Card, Alert } from 'react-bootstrap';
import axios from '../utils/axiosConfig';
import {useNavigate} from "react-router-dom";

const Profile = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [email, setEmail] = useState('');
    const [newEmail, setNewEmail] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        axios.get('/auth/me/')
            .then(response => {
                setUser(response.data);
                setEmail(response.data.email);
                setNewEmail(response.data.email);
                setLoading(false);
            })
            .catch(error => {
                console.error('Error fetching user data:', error);
                setLoading(false);
            });
    }, []);

    const delay = ms => new Promise(
        resolve => setTimeout(resolve, ms)
    );

    function logoutUser() {
        localStorage.removeItem('jwt');
        window.location.href = '/login';
    }

    const handleEditEmailChange = async (e) => {
        e.preventDefault();

        setError('');
        setSuccess('');

        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

        if (!emailRegex.test(newEmail)) {
            setError('Invalid email format.');
            return;
        }

        try {
            const response = await axios.post('/auth/changeEmail/', { newEmail }, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            setSuccess('Email updated successfully! ' +
                'Check your email for confirm. ' +
                'Now you will be logout.');
            await delay(3000);
            logoutUser()
        } catch (err) {
            if (err.response && err.response.data) {
                setError(err.response.data);
            } else {
                setError('An unexpected error occurred.');
            }
        }
    };

    if (loading) return <p>Loading...</p>;

    return (
        <Container>
            <h2 className="my-4">Profile</h2>
            {user ? (
                <Row>
                    <Col md={4}>
                        <Card>
                            <Card.Body>
                                <Card.Title>User Information</Card.Title>
                                <Card.Text>
                                    <strong>Email:</strong> {user.email}
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </Col>
                    <Col md={8}>
                        <Form onSubmit={handleEditEmailChange}>
                            <Form.Group controlId="formBasicEmail">
                                <Form.Label>Change your Email address</Form.Label>
                                <Form.Control
                                    type="email"
                                    placeholder="Enter new email"
                                    value={newEmail}
                                    onChange={(e) => setNewEmail(e.target.value)}
                                />
                            </Form.Group>
                            {error && <Alert variant="danger" className="mt-3">{error}</Alert>}
                            {success && <Alert variant="success" className="mt-3">{success}</Alert>}
                            <Button variant="primary" type="submit" className="mt-3">
                                Save Changes
                            </Button>
                        </Form>
                    </Col>
                </Row>
            ) : (
                <p>No user data available</p>
            )}
        </Container>
    );
};

export default Profile;