import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import './UserRegistration.css'; // Reusing glassmorphism styles

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await api.post('/auth/login', { email, password });
            const { token, user } = response.data;
            login(user, token);
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid email or password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <div className="registration-card glass-card">
                <div className="brand">
                    <div className="logo-circle"></div>
                    <h2>PayStream</h2>
                </div>

                <h1>Welcome Back</h1>
                <p>Login to manage your distributed wallet.</p>

                {error && <div className="error-message" style={{ color: '#ff4d4d', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}

                <form onSubmit={handleSubmit} className="form">
                    <div className="input-group">
                        <label>Email Address</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            placeholder="john@example.com"
                            className="styled-input"
                        />
                    </div>

                    <div className="input-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            placeholder="********"
                            className="styled-input"
                        />
                    </div>

                    <button type="submit" disabled={loading} className="primary-btn full-width">
                        {loading ? 'Logging in...' : 'Login'}
                    </button>

                    <p className="auth-link" style={{ textAlign: 'center', marginTop: '1.5rem', color: 'rgba(255,255,255,0.7)' }}>
                        Don't have an account? <Link to="/register" style={{ color: '#00d2ff', textDecoration: 'none', fontWeight: 'bold' }}>Register here</Link>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;
