import React, { useState } from 'react';
import bookingService from './bookingService';

const CoachDirectory = () => {
  const [message, setMessage] = useState('');

  const handleBooking = async (coachId) => {
    try {
      const user = JSON.parse(sessionStorage.getItem('user'));
      const bookingData = {
        coachId: coachId,
        studentId: user.id,
        startTime: new Date().toISOString(),
        endTime: new Date(Date.now() + 3600000).toISOString(),
      };

      await bookingService.bookLesson(bookingData);
      setMessage('Lesson Booked Successfully!');
    } catch (error) {
      setMessage('Failed to book lesson.');
    }
  };

  return (
    <div style={{ backgroundColor: '#F5F1EA', minHeight: '100vh', padding: '2rem' }}>
      <h1 style={{ color: '#6B4F3A', textAlign: 'center', marginBottom: '2rem' }} className="font-serif">
        Find Your Coach
      </h1>

      {message && (
        <div style={{
          color: message.includes('Successfully') ? 'green' : 'red',
          textAlign: 'center',
          marginBottom: '1rem',
          fontWeight: 'bold'
        }}>
          {message}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '2rem' }}>
        {/* Coach Card Styled */}
        <div style={{
          backgroundColor: '#6B4F3A',
          borderRadius: '60px',
          padding: '2.5rem',
          color: 'white',
          textAlign: 'center',
          boxShadow: '0 10px 30px rgba(0,0,0,0.2)'
        }}>
          <h3 className="font-serif" style={{ fontSize: '1.8rem' }}>Grandmaster Magnus</h3>
          <p style={{ margin: '1rem 0', opacity: 0.9 }}>Rating: 2800 ELO</p>
          <button
            onClick={() => handleBooking('7a8b9c...')}
            style={{
              backgroundColor: 'white',
              color: '#6B4F3A',
              borderRadius: '999px',
              padding: '0.8rem 2rem',
              border: 'none',
              fontWeight: 'bold',
              marginTop: '1rem',
              cursor: 'pointer',
              fontSize: '1rem'
            }}>
            Book Lesson
          </button>
        </div>
      </div>
    </div>
  );
};

export default CoachDirectory;