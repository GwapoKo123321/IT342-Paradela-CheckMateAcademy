import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import bookingService from './bookingService';

import './CoachDashboard.css';

const DAY_OPTIONS = [
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' },
  { value: 7, label: 'Sunday' },
];

const COACH_VIEWS = ['schedule', 'reviews', 'profile'];

const CoachDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const activeViewStorageKey = `coach-dashboard-view-${user.id || 'guest'}`;
  const [lessons, setLessons] = useState([]);
  const [activeView, setActiveView] = useState(() => {
    const savedView = localStorage.getItem(activeViewStorageKey);
    return COACH_VIEWS.includes(savedView) ? savedView : 'schedule';
  });
  const [profile, setProfile] = useState({ specialties: '', bio: '', availability: [] });
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  const loadLessons = useCallback(async () => {
    try {
      const data = await bookingService.getCoachLessons(user.id);
      setLessons(data);
    } catch (error) {
      console.error("Failed to load lessons", error);
    }
  }, [user.id]);

  const loadProfile = useCallback(async () => {
    try {
      const data = await bookingService.getCoachProfile(user.id);
      setProfile({
        specialties: data.specialties || '',
        bio: data.bio || '',
        availability: data.availability?.length ? data.availability : []
      });
    } catch (error) {
      console.error("Failed to load coach profile", error);
    }
  }, [user.id]);

  useEffect(() => {
    if (user.id) {
      loadLessons();
    }
  }, [user.id, loadLessons]);

  useEffect(() => {
    localStorage.setItem(activeViewStorageKey, activeView);
  }, [activeView, activeViewStorageKey]);

  useEffect(() => {
    if (activeView === 'profile' && user?.id) {
      loadProfile();
    }
  }, [activeView, user?.id, loadProfile]);

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

  const addAvailabilitySlot = () => {
    setProfile(prev => ({
      ...prev,
      availability: [
        ...prev.availability,
        { dayOfWeek: 1, startTime: '09:00', endTime: '10:00' }
      ]
    }));
  };

  const updateAvailabilitySlot = (index, field, value) => {
    setProfile(prev => ({
      ...prev,
      availability: prev.availability.map((slot, slotIndex) => (
        slotIndex === index ? { ...slot, [field]: field === 'dayOfWeek' ? Number(value) : value } : slot
      ))
    }));
  };

  const removeAvailabilitySlot = (index) => {
    setProfile(prev => ({
      ...prev,
      availability: prev.availability.filter((_, slotIndex) => slotIndex !== index)
    }));
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setIsSavingProfile(true);

    try {
      const savedProfile = await bookingService.saveCoachProfile(user.id, profile);
      setProfile({
        specialties: savedProfile.specialties || '',
        bio: savedProfile.bio || '',
        availability: savedProfile.availability || []
      });
      alert("Profile and availability saved.");
    } catch (error) {
      alert("Failed to save profile.");
    } finally {
      setIsSavingProfile(false);
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
        <button className={`coach-side-btn font-serif ${activeView === 'profile' ? 'coach-side-btn-active' : ''}`} onClick={() => setActiveView('profile')}>Profile & Availability</button>
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

        {activeView === 'profile' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Coach Profile</h2>
            <form className="coach-profile-form" onSubmit={handleSaveProfile}>
              <div className="coach-form-group">
                <label>Teaching strengths / playstyles</label>
                <input
                  value={profile.specialties}
                  onChange={(e) => setProfile(prev => ({ ...prev, specialties: e.target.value }))}
                  placeholder="Aggressive openings, endgames, positional play"
                />
              </div>

              <div className="coach-form-group">
                <label>Short coach bio</label>
                <textarea
                  value={profile.bio}
                  onChange={(e) => setProfile(prev => ({ ...prev, bio: e.target.value }))}
                  placeholder="Tell students what you help with and who your lessons are best for."
                />
              </div>

              <div className="coach-availability-header">
                <h3 className="font-serif">Weekly Availability</h3>
                <button type="button" className="coach-add-slot-btn" onClick={addAvailabilitySlot}>Add Time Slot</button>
              </div>

              <div className="coach-availability-list">
                {profile.availability.length === 0 ? (
                  <p className="coach-empty-slots">Add at least one time slot so students can book you.</p>
                ) : (
                  profile.availability.map((slot, index) => (
                    <div className="coach-slot-row" key={`${slot.dayOfWeek}-${slot.startTime}-${index}`}>
                      <select value={slot.dayOfWeek} onChange={(e) => updateAvailabilitySlot(index, 'dayOfWeek', e.target.value)}>
                        {DAY_OPTIONS.map(day => <option key={day.value} value={day.value}>{day.label}</option>)}
                      </select>
                      <input type="time" value={slot.startTime || ''} onChange={(e) => updateAvailabilitySlot(index, 'startTime', e.target.value)} />
                      <span>to</span>
                      <input type="time" value={slot.endTime || ''} onChange={(e) => updateAvailabilitySlot(index, 'endTime', e.target.value)} />
                      <button type="button" className="coach-remove-slot-btn" onClick={() => removeAvailabilitySlot(index)}>Remove</button>
                    </div>
                  ))
                )}
              </div>

              <button type="submit" className="coach-save-profile-btn" disabled={isSavingProfile}>
                {isSavingProfile ? 'Saving...' : 'Save Profile'}
              </button>
            </form>
          </>
        )}
      </main>
    </div>
  );
};

export default CoachDashboard;
