import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../api/authService';
import '../App.css';

const Register = () => {
  const navigate = useNavigate();
  const [role, setRole] = useState('Student');
  const [formData, setFormData] = useState({ email: '', password: '', full_name: '', role: 'Student', chess_username: '', current_elo: 0 });

  const handleRoleChange = (newRole) => {
    setRole(newRole);
    setFormData({ ...formData, role: newRole });
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Ensure ELO is sent as number
      const submissionData = { ...formData, current_elo: parseInt(formData.current_elo) || 0 };
      await authService.registerUser(submissionData);
      alert("Registration Successful!");
      navigate('/login');
    } catch (error) {
      alert("Registration failed. Email might already exist.");
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
          <div className="toggle-group">
            <button type="button" className={`btn-toggle ${role === 'Student' ? 'active' : ''}`} onClick={() => handleRoleChange('Student')}>Student</button>
            <button type="button" className={`btn-toggle ${role === 'Coach' ? 'active' : ''}`} onClick={() => handleRoleChange('Coach')}>Coach</button>
          </div>
          <div className="form-grid-inner">
            <form onSubmit={handleSubmit}>
              <div className={`grid-layout ${role === 'Coach' ? 'grid-coach' : 'grid-student'}`}>
                <input name="email" type="email" placeholder="Email" className="input-field reg-input" onChange={handleChange} required />
                <input name="password" type="password" placeholder="Password" className="input-field reg-input" onChange={handleChange} required />
                <input name="full_name" type="text" placeholder="Fullname" className="input-field reg-input" onChange={handleChange} required />

                {role === 'Coach' && (
                  <>
                    <input name="current_elo" type="number" placeholder="ELO Rating" className="input-field reg-input" onChange={handleChange} required />
                    <input name="chess_username" type="text" placeholder="Chess Username" className="input-field reg-input" onChange={handleChange} required />
                  </>
                )}

                <div style={{gridColumn: role === 'Coach' ? 'span 2' : 'span 1'}}>
                  <button type="submit" className="btn-reg-brown">Register</button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;