import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LandingPage from './components/LandingPage';
import Login from './components/Login';
import Register from './components/Register';

// The Factory Logic: Redirects users based on their role
const ProtectedDashboard = ({ children }) => {
  const user = JSON.parse(localStorage.getItem('user')); // Get user from storage

  if (!user) return <Navigate to="/login" />;

  // Factory logic: Returns different views based on role
  // This ensures a Coach never sees a Student's dashboard [cite: 109, 156]
  return children;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Placeholder for role-based dashboard factory */}
        <Route
          path="/dashboard"
          element={
            <ProtectedDashboard>
                {/* DashboardFactory will go here in Phase 3 */}
                <div style={{color: 'white', textAlign: 'center'}}>Dashboard Loaded</div>
            </ProtectedDashboard>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;