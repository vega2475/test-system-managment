import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import Header from './components/Header';
import Register from './pages/Register';
import Login from './pages/Login';
import Tests from './pages/Tests';
import Home from './pages/Home';
import Profile from './pages/Profile';
import './styles/App.css';
import InviteTestPage from "./pages/InviteTestPage";
import TestResultsPage from "./pages/TestResultPage";

const App = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem('jwt');
        if (token) {
            setIsLoggedIn(true);
        }
    }, []);

    const handleLogin = () => {
        setIsLoggedIn(true);
    };

    const handleLogout = () => {
        localStorage.removeItem('jwt');
        window.location.href = '/login'
        setIsLoggedIn(false);
    };

    return (
        <Router>
            <Header isLoggedIn={isLoggedIn} handleLogout={handleLogout}/>
            <Routes>
                <Route path="/" element={<Home isLoggedIn={isLoggedIn}/>}/>
                <Route path="/register" element={<Register onRegister={handleLogin}/>}/>
                <Route path="/login" element={<Login onLogin={handleLogin}/>}/>
                {isLoggedIn && <Route path="/tests" element={<Tests/>}/>}
                <Route path="/tests/results" element={<TestResultsPage/>}/>
                <Route path="/profile" element={<Profile/>}/> {/* TODO */}
                <Route path="/invite/register/:token" element={<InviteTestPage/>}/>
            </Routes>
        </Router>
    );
};

export default App;