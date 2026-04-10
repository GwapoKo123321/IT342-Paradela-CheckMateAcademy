import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../api/authService';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({ email: '', password: '' });

  const handleChange = (e) => setCredentials({ ...credentials, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = await authService.loginUser(credentials);
      alert("Welcome back, " + data.user.fullName);
      navigate('/dashboard');
    } catch (error) {
      alert("Login failed: Invalid email or password.");
    }
  };

  return (
    <div className="login-wrapper">
      <nav className="login-navbar">
        <div className="login-brand font-serif">CheckMateAcademy</div>
        <div className="login-nav-actions">
          <button className="login-nav-btn font-serif" onClick={() => navigate('/login')}>Login</button>
          <button className="login-nav-btn font-serif" onClick={() => navigate('/register')}>Register</button>
        </div>
      </nav>

      <div className="login-container">
        <div className="login-card">
          <h2 className="login-title font-serif">Welcome Future<br/>Grand Master</h2>

          <form onSubmit={handleSubmit} className="login-form">
            <input name="email" type="email" placeholder="Email" className="login-input" onChange={handleChange} required />
            <input name="password" type="password" placeholder="Password" className="login-input" onChange={handleChange} required />
            <button type="submit" className="login-submit-btn font-serif">Login</button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;