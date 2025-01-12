import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {Button, Container, Form, Row, Col} from 'react-bootstrap';
import axios from '../utils/axiosConfig';
import LoadingModal from "./LoadingWindow";

const TestComponent = () => {
    const [isStarted, setIsStarted] = useState(false);
    const {token} = useParams();
    const [test, setTest] = useState(null);
    const [isTokenValid, setIsTokenValid] = useState(false);
    const navigate = useNavigate();
    const [userDetails, setUserDetails] = useState({
        name: '',
        surname: '',
        email: '',
        age: ''
    });
    const [answers, setAnswers] = useState({});
    const [error, setError] = useState(null);
    const [, setTestResultId] = useState(null);
    const [testResult, setTestResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

    useEffect(() => {
        const validateToken = async () => {
            try {
                const response = await axios.get(`/invite/register/${token}`);
                if (response.status === 200) {
                    localStorage.setItem('invite', 'true');
                    setIsTokenValid(true);
                    if (response.data.result != null) {
                        setTestResult(response.data.result);
                    } else if (response.data.id) {
                        setTestResultId(response.data.id);
                        setTest(response.data);

                        const initialAnswers = {};
                        response.data.questions.forEach(question => {
                            if (question.selectedAnswer) {
                                initialAnswers[question.id] = question.selectedAnswer;
                            }
                        });
                        setAnswers(initialAnswers);
                    }
                }
            } catch (error) {
                alert('Token is incorrect')
                console.error('Error validating token:', error);
                setError('Invalid or expired invite link');
            }
        };

        validateToken();
    }, [token, navigate]);

    const handleInputChange = (e) => {
        const {name, value} = e.target;
        setUserDetails((prevDetails) => ({
            ...prevDetails,
            [name]: value,
        }));
    };

    const handleAnswerChange = (questionId, answer) => {
        setAnswers((prevAnswers) => ({
            ...prevAnswers,
            [questionId]: answer,
        }));
    };

    const handleStartTest = async () => {
        try {
            const response = await axios.post(`/invite/start-test/${token}`, userDetails);
            setTest(response.data);
            setIsStarted(true);
            setTestResultId(response.data.id);
        } catch (err) {
            console.error('Error starting test:', err);
            setError('Failed to start the test. Please try again.');
        }
    };

    const handleSaveCurrentAnswer = async () => {
        if (!test) return;
        const currentQuestion = test.questions[currentQuestionIndex];
        const selectedAnswer = answers[currentQuestion.id]?.id || answers[currentQuestion.id];

        if (!selectedAnswer) {
            return;
        }

        try {
            await axios.post(`/invite/partial-save/question/${currentQuestion.id}`, {
                answer: selectedAnswer,
                token
            });
        } catch (err) {
            console.error('Error saving answer:', err);
            setError('Failed to save the answer. Please try again.');
        }
    };

    const handleNextQuestion = async () => {
        await handleSaveCurrentAnswer();
        if (currentQuestionIndex < test.questions.length - 1) {
            setCurrentQuestionIndex((prevIndex) => prevIndex + 1);
        } else {
            handleSubmitTest();
        }
    };

    const handleQuestionChange = async (index) => {
        if (index === currentQuestionIndex) return;
        await handleSaveCurrentAnswer();
        setCurrentQuestionIndex(index);
    };

    const handleSubmitTest = async () => {
        try {
            setLoading(true);
            const formattedAnswers = {};
            Object.keys(answers).forEach(questionId => {
                formattedAnswers[questionId] = answers[questionId].id || answers[questionId];
            });

            await axios.post(`/invite/submit-test/${token}`, {
                userDetails,
                answers: formattedAnswers,
            });
            alert('Test submitted successfully');
            window.location.reload();
        } catch (err) {
            console.error('Error submitting test:', err);
            setError('Failed to submit the test. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const renderQuestion = () => {
        const question = test.questions[currentQuestionIndex];
        return (
            <div key={question.id} className="mb-3">
                <h4>{question.text}</h4>
                {question.answers.map((answer) => (
                    <Form.Check
                        key={answer.id}
                        type="radio"
                        label={answer.text}
                        name={`question-${question.id}`}
                        checked={answers[question.id]?.id === answer.id || answers[question.id] === answer.id}
                        onChange={() => handleAnswerChange(question.id, answer)}
                    />
                ))}
            </div>
        );
    };

    return (
        <Container className="invite-test-page mt-5">
            {testResult !== null ? (
                <div className="test-result">
                    <h2>Test Completed</h2>
                    <p>Your result: {testResult.toFixed(2)}%</p>
                </div>
            ) : (
                !isStarted && isTokenValid ? (
                    <Form>
                        <h2>Enter your details to start the test</h2>
                        <Form.Group>
                            <Form.Label>First Name</Form.Label>
                            <Form.Control
                                type="text"
                                name="name"
                                value={userDetails.name}
                                onChange={handleInputChange}
                            />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Last Name</Form.Label>
                            <Form.Control
                                type="text"
                                name="surname"
                                value={userDetails.surname}
                                onChange={handleInputChange}
                            />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Email</Form.Label>
                            <Form.Control
                                type="email"
                                name="email"
                                value={userDetails.email}
                                onChange={handleInputChange}
                            />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Age</Form.Label>
                            <Form.Control
                                type="number"
                                name="age"
                                value={userDetails.age}
                                onChange={handleInputChange}
                            />
                        </Form.Group>
                        <Button onClick={handleStartTest} className="mt-3">
                            Start Test
                        </Button>
                        {error && <p className="text-danger">{error}</p>}
                    </Form>
                ) : (
                    <div className="test-content">
                        {test && (
                            <>
                                <Row className="mb-3">
                                    <Col>
                                        <h2>{test.name}</h2>
                                    </Col>
                                </Row>
                                <Row>
                                    <Col md={2}>
                                        {/* Навигация по вопросам */}
                                        <div style={{marginBottom: '20px'}}>
                                            <strong>Questions:</strong>
                                            <div style={{display: 'flex', flexDirection: 'column', gap: '5px', marginTop: '10px'}}>
                                                {test.questions.map((q, i) => (
                                                    <Button
                                                        key={q.id}
                                                        variant={i === currentQuestionIndex ? 'primary' : 'outline-primary'}
                                                        onClick={() => handleQuestionChange(i)}
                                                    >
                                                        {i + 1}
                                                    </Button>
                                                ))}
                                            </div>
                                        </div>
                                    </Col>
                                    <Col md={10}>
                                        <Form>
                                            {renderQuestion()}
                                            <div className="mt-3">
                                                <Button
                                                    onClick={handleNextQuestion}
                                                    variant="primary"
                                                >
                                                    {currentQuestionIndex < test.questions.length - 1
                                                        ? 'Next Question'
                                                        : 'Submit Test'}
                                                </Button>
                                            </div>
                                        </Form>
                                        <LoadingModal show={loading} />
                                        {error && <p className="text-danger">{error}</p>}
                                    </Col>
                                </Row>
                            </>
                        )}
                    </div>
                )
            )}
        </Container>
    );
};

export default TestComponent;
