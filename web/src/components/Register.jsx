import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../api/authService';
import './RegisterPage.css'; //

const Register = () => {
  const navigate = useNavigate();
  const [role, setRole] = useState('Student');
  const [formData, setFormData] = useState({ email: '', password: '', fullName: '', role: 'Student', chessUsername: '', currentElo: 0 });

  const handleRoleChange = (newRole) => {
    setRole(newRole);
    setFormData({ ...formData, role: newRole });
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await authService.registerUser(formData);
      alert("Registration Successful!");
      navigate('/login');
    } catch (error) {
      alert("Registration failed. Email might already exist.");
    }
  };

  return (
    <div className="reg-wrapper">
      <nav className="reg-navbar">
        <div className="reg-brand">CheckMateAcademy</div>
        <div className="reg-nav-actions">
          <button className="reg-nav-btn" onClick={() => navigate('/login')}>Login</button>
          <button className="reg-nav-btn" onClick={() => navigate('/register')}>Register</button>
        </div>
      </nav>

      <div className="reg-container">
        <div className="reg-card">
          <div className="reg-toggle-group">
            <button
              type="button"
              className={`reg-toggle-btn ${role === 'Student' ? 'reg-toggle-btn-active' : ''}`}
              onClick={() => handleRoleChange('Student')}>Student</button>
            <button
              type="button"
              className={`reg-toggle-btn ${role === 'Coach' ? 'reg-toggle-btn-active' : ''}`}
              onClick={() => handleRoleChange('Coach')}>Coach</button>
          </div>

          <div className="reg-white-box">
            <form onSubmit={handleSubmit} className={`reg-form-grid ${role === 'Coach' ? 'reg-grid-coach' : 'reg-grid-student'}`}>
              <input name="email" type="email" placeholder="Email" className="reg-input" onChange={handleChange} required />
              <input name="password" type="password" placeholder="Password" className="reg-input" onChange={handleChange} required />
              <input name="fullName" type="text" placeholder="Fullname" className="reg-input" onChange={handleChange} required />

              {role === 'Coach' && (
                <>
                  <input name="currentElo" type="number" placeholder="ELO Rating" className="reg-input" onChange={handleChange} required />
                  <input name="chessUsername" type="text" placeholder="Chess Username" className="reg-input" onChange={handleChange} required />
                </>
              )}

              <button type="submit" className="reg-submit-btn">Register</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;