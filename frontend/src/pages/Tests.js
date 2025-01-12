import React, {useEffect, useState} from 'react';
import axios from '../utils/axiosConfig';
import {Button, Card, Col, Collapse, Container, Form, ListGroup, Row} from 'react-bootstrap';
import TextareaAutosize from 'react-textarea-autosize';
import '../styles/Tests.css';
import TestSettingsModal from "./TestSettingsModal";
import LoadingModal from "./LoadingWindow";

const Tests = () => {
    const [tests, setTests] = useState([]);
    const [showCreateTest, setShowCreateTest] = useState(false);
    const [expandedTests, setExpandedTests] = useState([]);
    const [newTest, setNewTest] = useState({
        name: '', questions: [{text: '', answers: [{text: '', correct: false}]}]
    });
    const [editTest, setEditTest] = useState(null);
    const [edit, setEdit] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [testId, setTestId] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleShow = (id) => {
        setTestId(id);
        setShowModal(true);
    };

    const handleClose = () => setShowModal(false);
    useEffect(() => {
        setLoading(true);
        const fetchTests = () => {
            return axios.get('/tests/getAll');
        };

        fetchTests()
            .then(response => {
                setTests(response.data);
            })
            .catch(error => {
                console.error('Error fetching tests', error);
            }).finally(() => setLoading(false));
    }, []);

    const handleCreateTestToggle = () => {
        setShowCreateTest(!showCreateTest);
    };

    const handleTestChange = (e) => {
        setNewTest({...newTest, [e.target.name]: e.target.value});
    };

    const handleQuestionChange = (index, e) => {
        const updatedQuestions = [...newTest.questions];
        updatedQuestions[index].text = e.target.value;
        setNewTest({...newTest, questions: updatedQuestions});
    };

    const handleAnswerChange = (qIndex, aIndex, e) => {
        const updatedQuestions = [...newTest.questions];
        updatedQuestions[qIndex].answers[aIndex][e.target.name] = e.target.value;
        setNewTest({...newTest, questions: updatedQuestions});
    };

    const handleAnswerCorrectChange = (qIndex, aIndex) => {
        const updatedQuestions = [...newTest.questions];
        updatedQuestions[qIndex].answers[aIndex].correct = !updatedQuestions[qIndex].answers[aIndex].correct;
        setNewTest({...newTest, questions: updatedQuestions});
    };

    const addQuestion = () => {
        setNewTest({
            ...newTest, questions: [...newTest.questions, {text: '', answers: [{text: '', correct: false}]}]
        });
    };

    const addAnswer = (index) => {
        const updatedQuestions = [...newTest.questions];
        updatedQuestions[index].answers.push({text: '', correct: false});
        setNewTest({...newTest, questions: updatedQuestions});
    };

    const handleCreateTest = async () => {
        try {
            setLoading(true);
            const userId = localStorage.getItem('user');
            const newTestWithUser = {
                ...newTest,
                userId: userId
            };

            await axios.post('/tests/create', newTestWithUser, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('jwt')}`
                }
            });
            alert('Test created successfully');
            window.location.reload();
            setShowCreateTest(false);
            setNewTest({
                name: '',
                questions: [{text: '', answers: [{text: '', correct: false}]}]
            });

            const response = await axios.get('/tests');
            setTests(response.data);
        } catch (error) {
            console.error('Error creating test', error);
        } finally {
            setLoading(false);
        }
    };

    const delay = ms => new Promise(
        resolve => setTimeout(resolve, ms)
    );
    const deleteTest = async (testId) => {
        try {
            const confirmDelete = window.confirm("Are you sure you want to delete this test?");
            if (confirmDelete) {
            setLoading(true);
            await delay(2000);
            await axios.delete(`/tests/delete/${testId}`);

            alert('Test deleted successfully');
            window.location.reload();

            const response = await axios.get('/tests');
            setTests(response.data);
        }
        } catch (error) {
            console.error('Error deleting test', error);
        } finally {
            setLoading(false);
        }
    };
    const toggleTestExpansion = (test) => {
        if (expandedTests.includes(test.id)) {
            setExpandedTests(expandedTests.filter((id) => id !== test.id));
            setEditTest(null);
        } else {
            setExpandedTests([...expandedTests, test.id]);
            setEditTest(test);
        }
    };

    const handleEditTestChange = (e) => {
        setEditTest({...editTest, [e.target.name]: e.target.value});
    };

    const handleEditQuestionChange = (index, e) => {
        const updatedQuestions = [...editTest.questions];
        updatedQuestions[index].text = e.target.value;
        setEditTest({...editTest, questions: updatedQuestions});
    };
    const handleEditAnswerChange = (qIndex, aIndex, e) => {
        const updatedQuestions = [...editTest.questions];
        updatedQuestions[qIndex].answers[aIndex][e.target.name] = e.target.value;
        setEditTest({...editTest, questions: updatedQuestions});
    };

    const handleEditAnswerCorrectChange = (qIndex, aIndex) => {
        const updatedQuestions = [...editTest.questions];
        updatedQuestions[qIndex].answers[aIndex].correct = !updatedQuestions[qIndex].answers[aIndex].correct;
        setEditTest({...editTest, questions: updatedQuestions});
    };

    const addEditQuestion = () => {
        setEditTest({
            ...editTest, questions: [...editTest.questions, {text: '', answers: [{text: '', correct: false}]}]
        });
    };

    const addEditAnswer = (index) => {
        const updatedQuestions = [...editTest.questions];
        updatedQuestions[index].answers.push({text: '', correct: false});
        setEditTest({...editTest, questions: updatedQuestions});
    };
    const handleEditTest = () => {
        if (!edit){
        setEdit(true);
        } else {
            setEdit(false);
        }
    };

    const generateLink = async (testId) => {
        try {
            const response = await axios.post(`/tests/generate-invite-link/${testId}`);
            const token = response.data.inviteLink;
            return `${token}`;
        } catch (error) {
            console.error('Error generating link', error);
            return null;
        }
    };

    const copyToClipboard = async (text) => {
        try {
            await navigator.clipboard.writeText(text);
            alert('Link copied to clipboard');
        } catch (err) {
            console.error('Failed to copy: ', err);
        }
    };

    const handleCopyLink = async (testId) => {
        const link = await generateLink(testId);
        if (link) {
            await copyToClipboard(link);
        } else {
            alert('Failed to generate link');
        }
    };

    const handleSaveEditTest = async () => {
        try {
            setLoading(true);
            await axios.put(`/tests/update/${editTest.id}`, editTest);
            alert('Test updated successfully');
            window.location.reload();
            setEditTest(null);

            const response = await axios.get('/tests');
            setTests(response.data);
        } catch (error) {
            console.error('Error updating test', error);
        } finally {
            setLoading(false);
        }
    };

    return (<Container className="tests mt-5">
            <h2>Tests</h2>
            <Button variant="primary" onClick={handleCreateTestToggle}>
                {showCreateTest ? 'Cancel' : 'Create Test'}
            </Button>
            <Collapse in={showCreateTest}>
                <div className="create-test mt-4">
                    <Card>
                        <Card.Header>Create New Test</Card.Header>
                        <Card.Body>
                            <Form>
                                <Form.Group controlId="formTestName">
                                    <Form.Label>Test Name</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="name"
                                        placeholder="Enter test name"
                                        value={newTest.name}
                                        onChange={handleTestChange}
                                    />
                                </Form.Group>
                                {newTest.questions.map((question, qIndex) => (
                                    <div key={qIndex} className="question mt-3">
                                        <Form.Group controlId={`formQuestionText${qIndex}`}>
                                            <Form.Label>Question {qIndex + 1}</Form.Label>
                                            <TextareaAutosize
                                                className="form-control"
                                                minRows={2}
                                                placeholder="Enter question text"
                                                value={question.text}
                                                onChange={(e) => handleQuestionChange(qIndex, e)}
                                            />
                                        </Form.Group>
                                        {question.answers.map((answer, aIndex) => (
                                            <Row key={aIndex} className="answer mt-2">
                                                <Col>
                                                    <Form.Control
                                                        type="text"
                                                        name="text"
                                                        placeholder="Enter answer text"
                                                        value={answer.text}
                                                        onChange={(e) => handleAnswerChange(qIndex, aIndex, e)}
                                                    />
                                                </Col>
                                                <Col xs="auto">
                                                    <Form.Check
                                                        type="checkbox"
                                                        label="Correct"
                                                        checked={answer.correct}
                                                        onChange={() => handleAnswerCorrectChange(qIndex, aIndex)}
                                                    />
                                                </Col>
                                            </Row>))}
                                        <Button variant="secondary" className="mt-2 add-answer-button"
                                                onClick={() => addAnswer(qIndex)}>
                                            Add Answer
                                        </Button>
                                    </div>))}
                                <Button variant="secondary" className="mt-3 add-question-button" onClick={addQuestion}>
                                    Add Question
                                </Button>
                                <Button variant="primary" className="mt-3 save-test" onClick={handleCreateTest}>
                                    Save Test
                                </Button>
                            </Form>
                        </Card.Body>
                    </Card>
                    <LoadingModal show={loading} />
                </div>
            </Collapse>
        <div className="test-list mt-4">
            {tests.map((test) => (
                <Card key={test.id} className="test mb-3">
                    <Card.Header style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ cursor: 'pointer' }} onClick={() => toggleTestExpansion(test)}>
                    {test.name} {expandedTests.includes(test.id) ? '▼' : '►'}
                </span>
                        <div style={{ display: 'flex', gap: '0.2rem' }}>
                            <Button variant="outline-secondary" className="edit-button" size="sm" onClick={() => handleEditTest(test)}>
                                {!edit ? 'Edit' : 'Cancel'}
                            </Button>
                            <Button variant="outline-secondary" className="link-button" size="sm" onClick={() => handleCopyLink(test.id)}>
                                Generate and Copy Invite Link
                            </Button>
                            <Button variant="outline-secondary" className="settings-button" size="sm" onClick={() => handleShow(test.id)}>
                                Settings
                            </Button>
                            <TestSettingsModal show={showModal} handleClose={handleClose} testId={testId} />
                            <Button variant="outline-secondary" className="delete-button" size="sm" onClick={() => deleteTest(test.id)}>
                                Delete
                            </Button>
                            <LoadingModal show={loading} />
                        </div>
                    </Card.Header>
                    <Collapse in={expandedTests.includes(test.id)}>
                        <div className="questions">
                            {!edit && (
                            <ListGroup>
                                {test.questions.map((question) => (
                                    <ListGroup.Item key={question.id} className="question">
                                        <strong>{question.text}</strong>
                                        <ul>
                                            {question.answers.map((answer) => (
                                                <li key={answer.id} className={answer.correct ? 'correct' : ''}>
                                                    {answer.text}
                                                </li>
                                            ))}
                                        </ul>
                                    </ListGroup.Item>
                                ))}
                            </ListGroup>)}
                            {edit && editTest.id === test.id && (
                                <Form>
                                    <Form.Group controlId="formEditTestName">
                                        <Form.Label>Test Name</Form.Label>
                                        <Form.Control
                                            type="text"
                                            name="name"
                                            placeholder="Enter test name"
                                            value={editTest.name}
                                            onChange={(e) => handleEditTestChange(e)}
                                        />
                                    </Form.Group>
                                    {editTest.questions.map((question, qIndex) => (
                                        <div key={qIndex} className="question mt-3">
                                            <Form.Group controlId={`formEditQuestionText${qIndex}`}>
                                                <Form.Label>Question {qIndex + 1}</Form.Label>
                                                <TextareaAutosize
                                                    className="form-control"
                                                    minRows={2}
                                                    placeholder="Enter question text"
                                                    value={question.text}
                                                    onChange={(e) => handleEditQuestionChange(qIndex, e)}
                                                />
                                            </Form.Group>
                                            {question.answers.map((answer, aIndex) => (
                                                <Row key={aIndex} className="answer mt-2">
                                                    <Col>
                                                        <Form.Control
                                                            type="text"
                                                            name="text"
                                                            placeholder="Enter answer text"
                                                            value={answer.text}
                                                            onChange={(e) => handleEditAnswerChange(qIndex, aIndex, e)}
                                                        />
                                                    </Col>
                                                    <Col xs="auto">
                                                        <Form.Check
                                                            type="checkbox"
                                                            label="Correct"
                                                            checked={answer.correct}
                                                            onChange={() => handleEditAnswerCorrectChange(qIndex, aIndex)}
                                                        />
                                                    </Col>
                                                </Row>
                                            ))}
                                            <Button variant="secondary" className="mt-2 add-answer-button" onClick={() => addEditAnswer(qIndex)}>
                                                Add Answer
                                            </Button>
                                        </div>
                                    ))}
                                    <Button variant="secondary" className="mt-3 add-question-button" onClick={addEditQuestion}>
                                        Add Question
                                    </Button>
                                    <Button variant="primary" className="mt-3 save-test" onClick={handleSaveEditTest}>
                                        Save Test
                                    </Button>
                                </Form>
                            )}
                            <LoadingModal show={loading} />
                        </div>
                    </Collapse>
                </Card>
            ))}
        </div>
    </Container>);
};

export default Tests;