import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import bookingService from '../api/bookingService';
import './CoachDashboard.css';

const CoachDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));
  const [lessons, setLessons] = useState([]);


  useEffect(() => {
    if (user && user.id) {
      loadLessons();
    }
  }, []);

  const loadLessons = async () => {
    try {
      const data = await bookingService.getCoachLessons(user.id);
      setLessons(data);
    } catch (error) {
      console.error("Failed to load lessons", error);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleAction = async (lessonId, action) => {
    try {
      // Send the Accept or Reject status to the database
      await bookingService.updateLessonStatus(lessonId, action);
      alert(`Booking ${action}ED successfully.`);
      loadLessons();
    } catch (error) {
      alert("Failed to update status.");
    }
  };

  return (
    <div className="coach-layout">
      <aside className="coach-sidebar">
        <h2 className="font-serif">CheckMate</h2>
        <button className="coach-side-btn font-serif">Dashboard</button>
        <button className="coach-side-btn font-serif">Schedule</button>
        <button className="coach-side-btn font-serif">Settings</button>
      </aside>

      <main className="coach-main-content">
        <div className="coach-header">
          <span style={{ marginRight: '1.5rem', fontWeight: 'bold' }}>{user?.fullName} (Coach)</span>
          <button onClick={handleLogout} className="coach-logout-btn font-serif">Logout</button>
        </div>

        <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Booking Management</h2>

        <div className="coach-table-container">
          <table className="coach-table">
            <thead>
              <tr>
                <th>Student Name</th>
                <th>Date / Time</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {lessons.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>No bookings found.</td>
                </tr>
              ) : (
                lessons.map((lesson) => (
                  <tr key={lesson.id}>
                    <td style={{ fontWeight: 'bold' }}>{lesson.studentName || 'Unknown Student'}</td>
                    <td>{new Date(lesson.startTime).toLocaleString()}</td>
                    <td>
                      <span className={`status-badge status-${lesson.status}`}>
                        {lesson.status}
                      </span>
                    </td>
                    <td>
                      {lesson.status === 'PENDING' && (
                        <>
                          <button onClick={() => handleAction(lesson.id, 'ACCEPTED')} className="coach-action-btn coach-accept">Accept</button>
                          <button onClick={() => handleAction(lesson.id, 'REJECTED')} className="coach-action-btn coach-reject">Reject</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
};

export default CoachDashboard;