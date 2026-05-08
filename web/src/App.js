import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LandingPage from './features/core/LandingPage';
import Login from './features/auth/Login';
import Register from './features/auth/Register';
import StudentDashboard from './features/booking/StudentDashboard';
import CoachDashboard from './features/booking/CoachDashboard';


import ActiveLesson from './features/active-session/ActiveLesson';

const DashboardFactory = () => {
  const userStr = localStorage.getItem('user');
  if (!userStr) return <Navigate to="/login" />;
  const user = JSON.parse(userStr);
  return user.role === 'Coach' ? <CoachDashboard /> : <StudentDashboard />;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<DashboardFactory />} />

        {/* The Active Lesson Route */}
        <Route path="/lesson/:id" element={<ActiveLesson />} />
      </Routes>
    </Router>
  );
}

export default App;