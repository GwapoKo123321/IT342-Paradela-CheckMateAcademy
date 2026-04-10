import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import bookingService from '../api/bookingService';
import './StudentDashboard.css';

const StudentDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));

  const [activeView, setActiveView] = useState('schedule');
  const [coaches, setCoaches] = useState([]);
  const [myLessons, setMyLessons] = useState([]);

  // Booking Form State
  const [selectedCoachId, setSelectedCoachId] = useState('');
  const [bookingDate, setBookingDate] = useState('');
  const [bookingTime, setBookingTime] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (activeView === 'booking') {
      const fetchCoaches = async () => {
        try {
          const coachList = await bookingService.getCoaches();
          setCoaches(coachList);
        } catch (error) { console.error("Failed to fetch coaches"); }
      };
      fetchCoaches();
    } else if (activeView === 'schedule' || activeView === 'reviews') {
      const fetchLessons = async () => {
        try {
          const lessons = await bookingService.getStudentLessons(user.id);
          setMyLessons(lessons);
        } catch (error) { console.error("Failed to fetch schedule"); }
      };
      fetchLessons();
    }
  }, [activeView, user.id]);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    if (!selectedCoachId || !bookingDate || !bookingTime) return;

    setIsSubmitting(true);
    const selectedCoach = coaches.find(c => c.id === selectedCoachId);
    const startDateTime = new Date(`${bookingDate}T${bookingTime}:00`);
    const endDateTime = new Date(startDateTime.getTime() + 60 * 60 * 1000);

    const bookingData = {
      coachId: selectedCoach.id,
      studentId: user.id,
      coachName: selectedCoach.fullName,
      studentName: user.fullName,
      startTime: startDateTime.toISOString(),
      endTime: endDateTime.toISOString(),
      status: "PENDING"
    };

    try {
      await bookingService.bookLesson(bookingData);
      alert(`Success! Your lesson with ${selectedCoach.fullName} has been requested.`);
      setSelectedCoachId(''); setBookingDate(''); setBookingTime('');
      setActiveView('schedule');
    } catch (error) {
      alert(error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const renderBoard = () => {
    const squares = [];
    const pieces = {
      '0-0': '♜', '0-1': '♞', '0-2': '♝', '0-3': '♛', '0-4': '♚', '0-5': '♝', '0-6': '♞', '0-7': '♜',
      '1-0': '♟', '1-1': '♟', '1-2': '♟', '1-3': '♟', '1-4': '♟', '1-5': '♟', '1-6': '♟', '1-7': '♟',
      '6-0': '♙', '6-1': '♙', '6-2': '♙', '6-3': '♙', '6-4': '♙', '6-5': '♙', '6-6': '♙', '6-7': '♙',
      '7-0': '♖', '7-1': '♘', '7-2': '♗', '7-3': '♕', '7-4': '♔', '7-5': '♗', '7-6': '♘', '7-7': '♖',
    };

    for (let row = 0; row < 8; row++) {
      for (let col = 0; col < 8; col++) {
        const isDark = (row + col) % 2 === 1;
        const piece = pieces[`${row}-${col}`] || '';
        squares.push(<div key={`${row}-${col}`} className={`student-square ${isDark ? 'student-dark' : 'student-light'}`}>{piece}</div>);
      }
    }
    return squares;
  };

  const activeLessons = myLessons.filter(l => l.status !== 'COMPLETED');
  const pastLessons = myLessons.filter(l => l.status === 'COMPLETED');

  return (
    <div className="student-layout">
      <aside className="student-sidebar">
        <h2 className="font-serif" style={{ textAlign: 'center', marginBottom: '1rem' }}>CheckMateAcademy</h2>
        <button className={`student-side-btn font-serif ${activeView === 'schedule' ? 'student-side-btn-active' : ''}`} onClick={() => setActiveView('schedule')}>My Schedule</button>
        <button className={`student-side-btn font-serif ${activeView === 'reviews' ? 'student-side-btn-active' : ''}`} onClick={() => setActiveView('reviews')}>Lesson Reviews</button>
        <button className={`student-side-btn font-serif ${activeView === 'booking' ? 'student-side-btn-active' : ''}`} onClick={() => setActiveView('booking')}>Book a Lesson</button>
        <button className={`student-side-btn font-serif ${activeView === 'board' ? 'student-side-btn-active' : ''}`} onClick={() => setActiveView('board')}>Training Board</button>
      </aside>

      <main className="student-main-content">
        <div className="student-header">
          <span style={{ marginRight: '1.5rem', fontWeight: 'bold' }}>{user?.fullName} (Student)</span>
          <button onClick={handleLogout} className="student-logout-btn font-serif">Logout</button>
        </div>

        {activeView === 'board' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Tactics Board</h2>
            <div className="student-board-container"><div className="student-chess-grid">{renderBoard()}</div></div>
          </>
        )}

        {activeView === 'booking' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Schedule a Session</h2>
            <div className="student-booking-section">
              <form onSubmit={handleBookingSubmit}>
                <div className="student-form-group">
                  <label>Select a Coach</label>
                  <select className="student-select" value={selectedCoachId} onChange={(e) => setSelectedCoachId(e.target.value)} required>
                    <option value="">-- Choose a Grandmaster --</option>
                    {coaches.map(coach => <option key={coach.id} value={coach.id}>{coach.fullName}</option>)}
                  </select>
                </div>
                <div className="student-form-group">
                  <label>Select Date</label>
                  <input type="date" className="student-date-input" value={bookingDate} onChange={(e) => setBookingDate(e.target.value)} required />
                </div>
                <div className="student-form-group">
                  <label>Select Time</label>
                  <input type="time" className="student-date-input" value={bookingTime} onChange={(e) => setBookingTime(e.target.value)} required />
                </div>
                <button type="submit" className="student-submit-btn" disabled={isSubmitting}>{isSubmitting ? 'Requesting...' : 'Confirm Booking'}</button>
              </form>
            </div>
          </>
        )}

        {activeView === 'schedule' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Upcoming Schedule</h2>
            <div className="student-table-container">
              <table className="student-table">
                <thead><tr><th>Coach Name</th><th>Date / Time</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {activeLessons.length === 0 ? (
                    <tr><td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>You have no upcoming bookings.</td></tr>
                  ) : (
                    activeLessons.map((lesson) => (
                      <tr key={lesson.id}>
                        <td style={{ fontWeight: 'bold' }}>{lesson.coachName}</td>
                        <td>{new Date(lesson.startTime).toLocaleString()}</td>
                        <td><span className={`status-badge status-${lesson.status}`}>{lesson.status}</span></td>
                        <td>
                          {lesson.status === 'ACCEPTED' && (
                            <button onClick={() => navigate(`/lesson/${lesson.id}`)} className="student-action-btn font-serif">Join Lesson</button>
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
            <div className="student-table-container">
              <table className="student-table">
                <thead><tr><th>Coach Name</th><th>Date Completed</th><th>Status</th><th>Review</th></tr></thead>
                <tbody>
                  {pastLessons.length === 0 ? (
                    <tr><td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>No past lesson reviews available.</td></tr>
                  ) : (
                    pastLessons.map((lesson) => (
                      <tr key={lesson.id}>
                        <td style={{ fontWeight: 'bold' }}>{lesson.coachName}</td>
                        <td>{new Date(lesson.startTime).toLocaleDateString()}</td>
                        <td><span className={`status-badge status-${lesson.status}`}>{lesson.status}</span></td>
                        <td>
                          <button onClick={() => navigate(`/lesson/${lesson.id}`)} className="student-action-btn student-action-btn-secondary font-serif">View Notes</button>
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

export default StudentDashboard;