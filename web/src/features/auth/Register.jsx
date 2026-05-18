import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from './authService';
import './RegisterPage.css';

const Register = () => {
  const navigate = useNavigate();
  const [role, setRole] = useState('Student');
  const [formData, setFormData] = useState({ email: '', password: '', fullName: '', role: 'Student', chessUsername: '', currentElo: 0 });
  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleRoleChange = (newRole) => {
    setRole(newRole);
    setFormData({ ...formData, role: newRole });
    setErrorMsg(''); // Clear errors when switching roles
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrorMsg(''); // Clear error when user starts typing again
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    // --- BASIC VALIDATIONS ---
    if (formData.password.length < 6) {
      setErrorMsg("Password must be at least 6 characters long.");
      return;
    }
    if (formData.fullName.trim() === '') {
      setErrorMsg("Full name cannot be empty.");
      return;
    }
    if (role === 'Coach') {
      if (formData.currentElo < 100 || formData.currentElo > 3500) {
        setErrorMsg("Please enter a valid ELO rating (between 100 and 3500).");
        return;
      }
      if (formData.chessUsername.trim() === '') {
        setErrorMsg("Chess username is required for coaches.");
        return;
      }
    }

    setIsLoading(true);
    try {
      await authService.registerUser(formData);
      alert("Registration Successful! You can now log in.");
      navigate('/login');
    } catch (error) {
      setErrorMsg("Registration failed. This email might already be in use.");
    } finally {
      setIsLoading(false);
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
            {errorMsg && (
              <div style={{ color: '#D8000C', backgroundColor: '#FFD2D2', padding: '10px', borderRadius: '10px', marginBottom: '1.5rem', fontWeight: 'bold' }}>
                ⚠️ {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className={`reg-form-grid ${role === 'Coach' ? 'reg-grid-coach' : 'reg-grid-student'}`}>
              <input name="email" type="email" placeholder="Email Address" className="reg-input" onChange={handleChange} required />
              <input name="password" type="password" placeholder="Password (Min 6 chars)" className="reg-input" onChange={handleChange} required />
              <input name="fullName" type="text" placeholder="Full Display Name" className="reg-input" onChange={handleChange} required />

              {role === 'Coach' && (
                <>
                  <input name="currentElo" type="number" placeholder="Current ELO Rating" className="reg-input" onChange={handleChange} required />
                  <input name="chessUsername" type="text" placeholder="Chess.com / Lichess Handle" className="reg-input" onChange={handleChange} required />
                </>
              )}

              <button type="submit" className="reg-submit-btn" disabled={isLoading}>
                {isLoading ? 'Registering...' : 'Register'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;