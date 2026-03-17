import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import './UserRegistration.css';

const UserRegistration = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await api.post('/auth/register', { name, email, password });
            alert('Registration successful! Please login.');
            navigate('/login');
        } catch (error) {
            console.error('Error creating user:', error);
            setError(error.response?.data?.message || 'Failed to register. Please try again.');
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
                
                <h1>Create Account</h1>
                <p>Join the secure distributed payment network.</p>

                {error && <div className="error-message" style={{ color: '#ff4d4d', marginBottom: '1rem' }}>{error}</div>}

                <form onSubmit={handleRegister} className="form">
                    <div className="input-group">
                        <label>Full Name</label>
                        <input 
                            type="text" 
                            value={name} 
                            onChange={(e) => setName(e.target.value)} 
                            placeholder="John Doe"
                            required 
                            className="styled-input"
                        />
                    </div>
                    
                    <div className="input-group">
                        <label>Email Address</label>
                        <input 
                            type="email" 
                            value={email} 
                            onChange={(e) => setEmail(e.target.value)} 
                            placeholder="john@example.com"
                            required 
                            className="styled-input"
                        />
                    </div>

                    <div className="input-group">
                        <label>Password</label>
                        <input 
                            type="password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            placeholder="********"
                            required 
                            className="styled-input"
                        />
                    </div>

                    <button type="submit" disabled={loading} className="primary-btn full-width">
                        {loading ? 'Creating...' : 'Register'}
                    </button>
                    
                    <p className="auth-link" style={{ textAlign: 'center', marginTop: '1.5rem', color: 'rgba(255,255,255,0.7)' }}>
                        Already have an account? <Link to="/login" style={{ color: '#00d2ff', textDecoration: 'none', fontWeight: 'bold' }}>Login here</Link>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default UserRegistration;
