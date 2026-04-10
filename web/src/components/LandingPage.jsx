import React from 'react';
import { useNavigate } from 'react-router-dom';
import './LandingPage.css';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="landing-wrapper">
      <nav className="landing-navbar">
        <div className="landing-brand">CheckMateAcademy</div>
        <div className="landing-nav-actions">
          <button className="landing-btn" onClick={() => navigate('/login')}>Login</button>
          <button className="landing-btn" onClick={() => navigate('/register')}>Register</button>
        </div>
      </nav>

      <div className="landing-hero">
        <div>
          <h1 className="landing-title">Master the Board.<br/>Elevate Your Game.</h1>
          <hr className="landing-divider" />
          <p className="landing-subtitle">
            Connect with expert coaches, track your ELO progress, and book real-time lessons —
            all in one elegant platform built for serious players.
          </p>
        </div>
      </div>
    </div>
  );
};

export default LandingPage;