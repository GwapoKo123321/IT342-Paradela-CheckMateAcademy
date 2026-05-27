import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from './authService';

import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
    setErrorMsg('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setIsLoading(true);

    try {

      await authService.loginUser(credentials);
      navigate('/dashboard');
    } catch (error) {
      setErrorMsg("Invalid email or password. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <nav className="login-navbar">
        <div className="login-brand">CheckMateAcademy</div>
        <div className="login-nav-actions">
          <button className="login-nav-btn" onClick={() => navigate('/login')}>Login</button>
          <button className="login-nav-btn" onClick={() => navigate('/register')}>Register</button>
        </div>
      </nav>

      <div className="login-container">
        <div className="login-card">
          <h2 className="login-title">Welcome Future<br/>Grand Master</h2>

          <div className="login-white-box">
            {errorMsg && (
              <div style={{ color: '#D8000C', backgroundColor: '#FFD2D2', padding: '10px', borderRadius: '10px', marginBottom: '1.5rem', fontWeight: 'bold' }}>
                ⚠️ {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="login-form">
              <input name="email" type="email" placeholder="Account Email" className="login-input" onChange={handleChange} required />
              <input name="password" type="password" placeholder="Password" className="login-input" onChange={handleChange} required />

              <button type="submit" className="login-submit-btn" disabled={isLoading}>
                {isLoading ? 'Authenticating...' : 'Login '}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
