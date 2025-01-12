import React, { useState } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import axios from '../utils/axiosConfig';
import LoadingModal from "./LoadingWindow";

const TestSettingsModal = ({ show, handleClose, testId }) => {
    const [expirationDate, setExpirationDate] = useState('');
    const [expirationTime, setExpirationTime] = useState('');
    const [loading, setLoading] = useState(false);

    const handleDateChange = (e) => {
        setExpirationDate(e.target.value);
    };

    const handleTimeChange = (e) => {
        setExpirationTime(e.target.value);
    };

    const handleSaveSettings = async () => {
        if (!expirationDate || !expirationTime) {
            alert('Please select both date and time');
            return;
        }

        const expirationDateTime = new Date(`${expirationDate}T${expirationTime}`);

        try {
            setLoading(true);
            await axios.post(`/tests/update-expiration/${testId}`, {
                testId,
                expirationDate: expirationDateTime.toISOString(),
            });
            alert('Settings saved successfully');
            handleClose();
        } catch (error) {
            console.error('Error saving settings', error);
            alert('Failed to save settings');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>Test Settings</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form>
                    <Form.Group controlId="expirationDate">
                        <Form.Label>Expiration Link Date</Form.Label>
                        <Form.Control
                            type="date"
                            value={expirationDate}
                            onChange={handleDateChange}
                        />
                    </Form.Group>
                </Form>
                <Form>
                    <Form.Group controlId="expirationTime">
                        <Form.Label>Expiration Link Time</Form.Label>
                        <Form.Control
                            type="time"
                            value={expirationTime}
                            onChange={handleTimeChange}
                        />
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={handleClose}>
                    Cancel
                </Button>
                <Button variant="primary" onClick={handleSaveSettings}>
                    Save
                </Button>
            </Modal.Footer>
        </Modal>
            <LoadingModal show={loading} />
            </div>
    );
};

export default TestSettingsModal;