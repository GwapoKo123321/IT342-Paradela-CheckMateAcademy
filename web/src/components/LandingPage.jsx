import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

const LandingPage = () => {
  const navigate = useNavigate();
  return (
    <div>
      <nav className="navbar">
        <div className="nav-brand">CheckMateAcademy</div>
        <div className="nav-buttons">
          <button className="nav-btn-white" onClick={() => navigate('/login')}>Login</button>
          <button className="nav-btn-white" onClick={() => navigate('/register')}>Register</button>
        </div>
      </nav>
      <div className="container">
        <div className="hero">
          <h1>Master the Board.<br/>Elevate Your Game.</h1>
          <hr style={{width: '60%', margin: '1rem auto', opacity: 0.3}} />
          <p style={{fontSize: '1.2rem', fontWeight: 300}}>
            Connect with expert coaches, track your ELO progress, and book real-time lessons —
            all in one elegant platform built for serious players.
          </p>
        </div>
      </div>
    </div>
  );
};
export default LandingPage;