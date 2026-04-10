import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import Register from './components/Register';
import StudentDashboard from './components/StudentDashboard';
import CoachDashboard from './components/CoachDashboard';
import ActiveLesson from './components/ActiveLesson';

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

        {/* NEW: The Active Lesson Route */}
        <Route path="/lesson/:id" element={<ActiveLesson />} />
      </Routes>
    </Router>
  );
}

export default App;