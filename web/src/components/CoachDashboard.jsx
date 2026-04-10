import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import bookingService from '../api/bookingService';
import './CoachDashboard.css';

const CoachDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));
  const [lessons, setLessons] = useState([]);
  const [activeView, setActiveView] = useState('schedule');

  useEffect(() => {
    if (user && user.id) {
      loadLessons();
    }
  }, [user]);

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
    // Confirm before marking as completed
    if (action === 'COMPLETED') {
      const confirmComplete = window.confirm("Are you sure you want to mark this lesson as completed? This will move it to Match Reviews.");
      if (!confirmComplete) return;
    }

    try {
      await bookingService.updateLessonStatus(lessonId, action);
      alert(`Lesson status updated to ${action}.`);
      loadLessons();
    } catch (error) {
      alert("Failed to update status.");
    }
  };

  const activeLessons = lessons.filter(l => l.status !== 'COMPLETED');
  const pastLessons = lessons.filter(l => l.status === 'COMPLETED');

  return (
    <div className="coach-layout">
      <aside className="coach-sidebar">
        <h2 className="font-serif" style={{ textAlign: 'center', marginBottom: '1rem' }}>CheckMateAcademy</h2>
        <button className={`coach-side-btn font-serif ${activeView === 'schedule' ? 'coach-side-btn-active' : ''}`} onClick={() => setActiveView('schedule')}>Schedule & Requests</button>
        <button className={`coach-side-btn font-serif ${activeView === 'reviews' ? 'coach-side-btn-active' : ''}`} onClick={() => setActiveView('reviews')}>Lesson Reviews</button>
      </aside>

      <main className="coach-main-content">
        <div className="coach-header">
          <span style={{ marginRight: '1.5rem', fontWeight: 'bold' }}>{user?.fullName} (Coach)</span>
          <button onClick={handleLogout} className="coach-logout-btn font-serif">Logout</button>
        </div>

        {activeView === 'schedule' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Booking Management</h2>
            <div className="coach-table-container">
              <table className="coach-table">
                <thead><tr><th>Student Name</th><th>Date / Time</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {activeLessons.length === 0 ? (
                    <tr><td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>No active bookings found.</td></tr>
                  ) : (
                    activeLessons.map((lesson) => (
                      <tr key={lesson.id}>
                        <td style={{ fontWeight: 'bold' }}>{lesson.studentName || 'Unknown Student'}</td>
                        <td>{new Date(lesson.startTime).toLocaleString()}</td>
                        <td><span className={`status-badge status-${lesson.status}`}>{lesson.status}</span></td>
                        <td>
                          {lesson.status === 'PENDING' && (
                            <>
                              <button onClick={() => handleAction(lesson.id, 'ACCEPTED')} className="coach-action-btn coach-accept font-serif">Accept</button>
                              <button onClick={() => handleAction(lesson.id, 'REJECTED')} className="coach-action-btn coach-reject font-serif">Reject</button>
                            </>
                          )}
                          {lesson.status === 'ACCEPTED' && (
                            <>
                              <button onClick={() => navigate(`/lesson/${lesson.id}`)} className="coach-action-btn coach-join font-serif">Join Lesson</button>
                              <button onClick={() => handleAction(lesson.id, 'COMPLETED')} className="coach-action-btn coach-complete font-serif">Mark Complete</button>
                            </>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}

        {activeView === 'reviews' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Past Lesson Reviews</h2>
            <div className="coach-table-container">
              <table className="coach-table">
                <thead><tr><th>Student Name</th><th>Date Completed</th><th>Status</th><th>Review</th></tr></thead>
                <tbody>
                  {pastLessons.length === 0 ? (
                    <tr><td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>No past lesson reviews available.</td></tr>
                  ) : (
                    pastLessons.map((lesson) => (
                      <tr key={lesson.id}>
                        <td style={{ fontWeight: 'bold' }}>{lesson.studentName || 'Unknown Student'}</td>
                        <td>{new Date(lesson.startTime).toLocaleDateString()}</td>
                        <td><span className={`status-badge status-${lesson.status}`}>{lesson.status}</span></td>
                        <td>
                          <button onClick={() => navigate(`/lesson/${lesson.id}`)} className="coach-action-btn coach-complete font-serif">View / Edit Notes</button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default CoachDashboard;