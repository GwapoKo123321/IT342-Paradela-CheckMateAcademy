import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../api/authService';
import '../App.css';

const Login = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({ email: '', password: '' });

  const handleChange = (e) => setCredentials({ ...credentials, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await authService.loginUser(credentials);
      alert("Login Successful! Welcome " + (response.data.full_name || "User"));
      navigate('/');
    } catch (error) {
      alert("Login failed: Invalid credentials");
    }
  };

  return (
    <div>
      <nav className="navbar">
        <div className="nav-brand font-serif">CheckMateAcademy</div>
        <div className="nav-buttons">
          <button className="nav-btn-white" onClick={() => navigate('/login')}>Login</button>
          <button className="nav-btn-white" onClick={() => navigate('/register')}>Register</button>
        </div>
      </nav>
      <div className="container">
        <div className="card">
          <h2 className="card-title font-serif">Welcome Future Grand Master</h2>
          <form onSubmit={handleSubmit}>
            <div className="login-form-group">
              <input name="email" type="email" placeholder="Username (Email)" className="input-field" onChange={handleChange} required />
              <input name="password" type="password" placeholder="Password" className="input-field" onChange={handleChange} required />
              <button type="submit" className="btn-submit-white mt-4">Login</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;