import React from 'react';
import { useNavigate } from 'react-router-dom';
import './StudentDashboard.css';

const StudentDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
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
        squares.push(
          <div key={`${row}-${col}`} className={`student-square ${isDark ? 'student-dark' : 'student-light'}`}>
            {piece}
          </div>
        );
      }
    }
    return squares;
  };

  return (
    <div className="student-layout">
      <aside className="student-sidebar">
        <h2 className="font-serif">CheckMate</h2>
        <button className="student-side-btn font-serif">Dashboard</button>
        <button className="student-side-btn font-serif">Activity</button>
        <button className="student-side-btn font-serif">Settings</button>
      </aside>

      <main className="student-main-content">
        <div className="student-header">
          <span style={{ marginRight: '1.5rem', fontWeight: 'bold' }}>{user?.fullName} (Student)</span>
          <button onClick={handleLogout} className="student-logout-btn font-serif">Logout</button>
        </div>

        <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Training Board</h2>

        <div className="student-board-container">
          <div className="student-chess-grid">
            {renderBoard()}
          </div>
        </div>
      </main>
    </div>
  );
};

export default StudentDashboard;