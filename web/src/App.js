import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LandingPage from './features/core/LandingPage';
import Login from './features/auth/Login';
import Register from './features/auth/Register';
import StudentDashboard from './features/booking/StudentDashboard';
import CoachDashboard from './features/booking/CoachDashboard';
import AdminDashboard from './features/admin/AdminDashboard';
import ActiveLesson from './features/active-session/ActiveLesson';

const DashboardFactory = () => {
  const userStr = localStorage.getItem('user');
  if (!userStr) return <Navigate to="/login" />;
  const user = JSON.parse(userStr);

  // NEW ROUTING LOGIC: Checks for Admin first
  if (user.role === 'Admin') return <AdminDashboard />;
  if (user.role === 'Coach') return <CoachDashboard />;
  return <StudentDashboard />;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<DashboardFactory />} />
        <Route path="/lesson/:id" element={<ActiveLesson />} />
      </Routes>
    </Router>
  );
}

export default App;