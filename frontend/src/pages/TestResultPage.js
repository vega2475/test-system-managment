import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import axios from '../utils/axiosConfig';
import {Container, Table, Button, Modal, Form, Row, Col} from 'react-bootstrap';

const TestResultsPage = () => {
    const {userId} = useParams();
    const [results, setResults] = useState([]);
    const [error, setError] = useState(null);

    const [showModal, setShowModal] = useState(false);
    const [selectedResult, setSelectedResult] = useState(null);

    const [searchQuery, setSearchQuery] = useState('');
    const [minResult, setMinResult] = useState('');

    const [sortField, setSortField] = useState('name');
    const [isAscending, setIsAscending] = useState(true);

    useEffect(() => {
        const fetchResults = async () => {
            try {
                let userId = localStorage.getItem('user');
                const response = await axios.get(`/tests/results/${userId}/`);
                setResults(response.data);
            } catch (err) {
                console.error('Error fetching test results:', err);
                setError('Failed to load test results.');
            }
        };

        fetchResults();
    }, [userId]);

    const handleShowModal = (result) => {
        setSelectedResult(result);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setSelectedResult(null);
        setShowModal(false);
    };

    const handleDelete = async () => {
        if (!selectedResult || !selectedResult.id) return;
        try {
            console.log('id', selectedResult.id)
            await axios.delete(`/tests/results/delete/${selectedResult.id}`);
            setResults((prevResults) => prevResults.filter((r) => r.id !== selectedResult.id));
        } catch (err) {
            console.error('Error deleting test result:', err);
            setError('Failed to delete the test result.');
        } finally {
            handleCloseModal();
        }
    };

    const filteredBySearch = results.filter(result => {
        const fullName = (result.name + ' ' + result.surname).toLowerCase();
        return fullName.includes(searchQuery.toLowerCase());
    });

    const filteredByResult = filteredBySearch.filter(result => {
        if (minResult === '') return true;
        return result.result >= parseFloat(minResult);
    });

    const sortedResults = [...filteredByResult].sort((a, b) => {
        let valA, valB;
        switch (sortField) {
            case 'name':
                valA = a.name.toLowerCase();
                valB = b.name.toLowerCase();
                break;
            case 'surname':
                valA = a.surname.toLowerCase();
                valB = b.surname.toLowerCase();
                break;
            case 'age':
                valA = a.age ?? 0;
                valB = b.age ?? 0;
                break;
            case 'result':
                valA = a.result;
                valB = b.result;
                break;
            default:
                valA = a.name.toLowerCase();
                valB = b.name.toLowerCase();
        }

        if (valA < valB) return isAscending ? -1 : 1;
        if (valA > valB) return isAscending ? 1 : -1;
        return 0;
    });

    const totalCount = sortedResults.length;
    const totalResult = sortedResults.reduce((sum, r) => sum + r.result, 0);
    const totalAge = sortedResults.reduce((sum, r) => sum + (r.age ?? 0), 0);
    const idealCount = sortedResults.filter(r => r.result === 100).length;

    const averageResult = totalCount > 0 ? (totalResult / totalCount).toFixed(2) : 0;
    const averageAge = totalCount > 0 ? (totalAge / totalCount).toFixed(2) : 0;

    return (
        <Container className="mt-5">
            <h2>Test Results</h2>
            {error && <p className="text-danger">{error}</p>}

            <Row className="mb-3">
                <Col md={3}>
                    <Form.Control
                        type="text"
                        placeholder="Search by name or surname..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </Col>
                <Col md={3}>
                    <Form.Control
                        type="number"
                        placeholder="Min result (%)"
                        value={minResult}
                        onChange={(e) => setMinResult(e.target.value)}
                    />
                </Col>
                <Col md={3}>
                    <Form.Select
                        value={sortField}
                        onChange={(e) => setSortField(e.target.value)}
                    >
                        <option value="name">Sort by Name</option>
                        <option value="surname">Sort by Surname</option>
                        <option value="age">Sort by Age</option>
                        <option value="result">Sort by Result</option>
                    </Form.Select>
                </Col>
                <Col md={3}>
                    <Button
                        variant="secondary"
                        onClick={() => setIsAscending(!isAscending)}
                    >
                        {isAscending ? 'Ascending' : 'Descending'}
                    </Button>
                </Col>
            </Row>

            {/* Блок статистики */}
            <Row className="mb-4">
                <Col md={3}>
                    <div className="p-2 border rounded">
                        <strong>Total Passed:</strong> {totalCount}
                    </div>
                </Col>
                <Col md={3}>
                    <div className="p-2 border rounded">
                        <strong>Average Result:</strong> {averageResult}%
                    </div>
                </Col>
                <Col md={3}>
                    <div className="p-2 border rounded">
                        <strong>Average Age:</strong> {averageAge}
                    </div>
                </Col>
                <Col md={3}>
                    <div className="p-2 border rounded">
                        <strong>100% Scored:</strong> {idealCount}
                    </div>
                </Col>
            </Row>

            <Table striped bordered hover>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Surname</th>
                    <th>Age</th>
                    <th>Email</th>
                    <th>Result (%)</th>
                    <th>Details</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {sortedResults.map((result, index) => (
                    <tr key={index}>
                        <td>{result.name}</td>
                        <td>{result.surname}</td>
                        <td>{result.age}</td>
                        <td>{result.email}</td>
                        <td>{result.result.toFixed(2)}</td>
                        <td>
                            {(() => {
                                const uniqueResults = [];
                                const seenQuestions = new Set();

                                for (const qr of result.questionResults) {
                                    if (!seenQuestions.has(qr.question)) {
                                        seenQuestions.add(qr.question);
                                        uniqueResults.push(qr);
                                    }
                                }

                                return (
                                    <ul>
                                        {uniqueResults.map((questionResult, idx) => (
                                            <li key={idx}>
                                                <strong>Question:</strong> {questionResult.question}<br/>
                                                <strong>Selected Answer:</strong> {questionResult.selectedAnswer}<br/>
                                                <strong>Correct:</strong>
                                                <span style={{color: questionResult.correct ? 'green' : 'red'}}>
                                                    {questionResult.correct ? ' Yes' : ' No'}
                                                </span>
                                            </li>
                                        ))}
                                    </ul>
                                );
                            })()}
                        </td>
                        <td>
                            <Button variant="danger" onClick={() => handleShowModal(result)}>Delete</Button>
                        </td>
                    </tr>
                ))}
                {sortedResults.length === 0 && (
                    <tr>
                        <td colSpan="7" className="text-center">No results found.</td>
                    </tr>
                )}
                </tbody>
            </Table>

            {/* Modal for confirmation */}
            <Modal show={showModal} onHide={handleCloseModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Are you sure?</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedResult && (
                        <p>
                            The record for {selectedResult.name} {selectedResult.surname} will be permanently deleted.
                            This action cannot be undone.
                        </p>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseModal}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={handleDelete}>
                        Confirm
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default TestResultsPage;
