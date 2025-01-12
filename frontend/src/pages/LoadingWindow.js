import React from 'react';
import { Modal, Spinner } from 'react-bootstrap';

function LoadingModal({ show }) {
    return (
        <Modal show={show} centered>
            <Modal.Body className="text-center">
                <Spinner animation="border" />
                <p>Loading...</p>
            </Modal.Body>
        </Modal>
    );
}

export default LoadingModal;