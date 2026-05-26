import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chess } from 'chess.js';
import { Chessboard } from 'react-chessboard';
import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  RefreshCw,
  RotateCcw
} from 'lucide-react';
import bookingService from './bookingService';
import authService from '../auth/authService';
import { useToast } from './useNotifications';
import './StudentDashboard.css';

const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

const PLAYSTYLE_OPTIONS = [
  'Aggressive openings',
  'Defensive play',
  'Endgames',
  'Positional play',
  'Tactics',
  'Strategy',
];

const STUDENT_VIEWS = ['schedule', 'reviews', 'booking', 'board', 'profile'];

const formatLocalDateTime = (date) => {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
};

const getSlotKey = (slot) => `${slot.coachId}-${slot.startTime}`;

const formatSlotTime = (value) => new Date(value).toLocaleTimeString([], {
  hour: 'numeric',
  minute: '2-digit'
});

const formatBookingDate = (value) => new Date(`${value}T00:00:00`).toLocaleDateString([], {
  weekday: 'long',
  month: 'short',
  day: 'numeric'
});

/**
 * True when the given slot's start time is still in the future relative to `now`.
 *
 * WHY this approach:
 * The backend may return startTime as "2026-05-26T10:00:00" (no timezone suffix).
 * Browsers differ on whether they treat that as local or UTC, which breaks a simple
 * `new Date(slot.startTime) > new Date()` comparison.
 *
 * Instead we explictly build a local Date from the selected bookingDate + the time
 * part of startTime, so it is always unambiguously in the local timezone.
 */
const isSlotFuture = (slot, bookingDate, now) => {
  try {
    // Extract HH:MM from whatever format the server returned (ISO or "HH:MM:SS" etc.)
    const rawTime = slot.startTime || '';
    const timePart = rawTime.includes('T')
      ? rawTime.split('T')[1].slice(0, 5)   // "2026-05-26T10:00:00" → "10:00"
      : rawTime.slice(0, 5);                 // "10:00:00" → "10:00"
    // Build an unambiguous local-time Date using the date the student chose
    const slotDate = new Date(`${bookingDate}T${timePart}:00`);
    return slotDate > now;
  } catch {
    return true; // fail open so valid slots are never accidentally hidden
  }
};

/**
 * True when the current time is within the join window:
 * 10 minutes before lesson start or any time after start.
 */
const canJoinLesson = (startTime) => {
  const windowOpen = new Date(new Date(startTime).getTime() - 10 * 60 * 1000);
  return new Date() >= windowOpen;
};

/** Human-readable label shown on the disabled Join button before the window opens. */
const joinCountdownLabel = (startTime) => {
  const minsUntil = Math.ceil((new Date(startTime) - new Date()) / 60_000);
  if (minsUntil <= 10) return 'Join Lesson';
  const hrs = Math.floor(minsUntil / 60);
  const mins = minsUntil % 60;
  if (hrs > 0) return `Opens in ${hrs}h ${mins}m`;
  return `Opens in ${minsUntil} min`;
};

const StudentDashboard = () => {
  const navigate = useNavigate();
  const { toast, ToastContainer } = useToast();
  const [user, setUser] = useState(() => JSON.parse(sessionStorage.getItem('user') || '{}'));
  const activeViewStorageKey = `student-dashboard-view-${user.id || 'guest'}`;

  const [activeView, setActiveView] = useState(() => {
    const savedView = sessionStorage.getItem(activeViewStorageKey);
    return STUDENT_VIEWS.includes(savedView) ? savedView : 'schedule';
  });

  const [rawSlots, setRawSlots] = useState([]);      // all slots returned by the server
  const [myLessons, setMyLessons] = useState([]);

  // Tick every 60 s so the slot list re-filters automatically as time passes
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const tick = setInterval(() => setNow(new Date()), 60_000);
    return () => clearInterval(tick);
  }, []);

  const [selectedSlotKey, setSelectedSlotKey] = useState('');
  const [bookingDate, setBookingDate] = useState('');
  const [preferredStyle, setPreferredStyle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Derived: only the slots that haven't started yet (recalculated every render)
  const availableSlots = rawSlots.filter(s => isSlotFuture(s, bookingDate, now));


  // Profile Edit State
  const [profileForm, setProfileForm] = useState({ fullName: user.fullName || '', chessUsername: user.chessUsername || '', currentElo: user.currentElo || 0 });
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  const [trainingFen, setTrainingFen] = useState(START_FEN);
  const [trainingMoves, setTrainingMoves] = useState([]);
  const [trainingHistory, setTrainingHistory] = useState([]);
  const [trainingViewIndex, setTrainingViewIndex] = useState(-1);
  const [trainingOrientation, setTrainingOrientation] = useState('white');

  const todayInputValue = formatLocalDateTime(new Date()).slice(0, 10);

  useEffect(() => {
    sessionStorage.setItem(activeViewStorageKey, activeView);
  }, [activeView, activeViewStorageKey]);

  // ── Booking slots fetch — filters out past slots client-side ────────────────
  useEffect(() => {
    if (activeView !== 'booking') return;
    if (!bookingDate) { setRawSlots([]); setSelectedSlotKey(''); return; }

    const fetchAvailableSlots = async () => {
      try {
        const filters = { date: bookingDate, studentId: user.id };
        if (preferredStyle) filters.style = preferredStyle;
        const slots = await bookingService.getAvailableSlots(filters);
        // Store ALL slots raw — the live `availableSlots` derived value
        // filters out past ones on every render so stale results are impossible.
        setRawSlots(slots);
      } catch (error) {
        console.error('Failed to fetch available slots');
        setRawSlots([]);
      }
    };
    fetchAvailableSlots();
  }, [activeView, user.id, bookingDate, preferredStyle]);

  // Keep selectedSlotKey in sync: clear it if its slot is no longer in the visible list
  useEffect(() => {
    if (selectedSlotKey && !availableSlots.some(s => getSlotKey(s) === selectedSlotKey)) {
      setSelectedSlotKey('');
    }
  }, [availableSlots, selectedSlotKey]);

  // ── Schedule / reviews — fetch immediately then poll every 10 s ───────────
  // This ensures status changes made by the coach (accept/reject) appear
  // automatically without the student having to navigate away and back.
  useEffect(() => {
    if (activeView !== 'schedule' && activeView !== 'reviews') return;

    const fetchLessons = async () => {
      try {
        const lessons = await bookingService.getStudentLessons(user.id);
        setMyLessons(lessons);
      } catch (error) {
        console.error('Failed to fetch schedule');
      }
    };

    fetchLessons();                                      // immediate fetch on mount/tab switch
    const interval = setInterval(fetchLessons, 10_000); // then every 10 seconds
    return () => clearInterval(interval);               // clean up when leaving the tab
  }, [activeView, user.id]);

  const handleLogout = () => {
    sessionStorage.clear();
    navigate('/login');
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    setIsSavingProfile(true);
    try {
      const updatedUser = await authService.updateProfile(user.id, profileForm);
      updatedUser.role = user.role;
      sessionStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);
      toast('Profile updated successfully!', 'success');
    } catch (error) {
      toast('Failed to update profile changes.', 'error');
    } finally {
      setIsSavingProfile(false);
    }
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    const selectedSlot = availableSlots.find(slot => getSlotKey(slot) === selectedSlotKey);
    if (!selectedSlot) return;
    if (selectedSlot.studentConflict) {
      toast('This time conflicts with another lesson already on your schedule.', 'warning');
      return;
    }

    setIsSubmitting(true);

    const bookingData = {
      coachId: selectedSlot.coachId,
      studentId: user.id,
      coachName: selectedSlot.coachName,
      studentName: user.fullName,
      startTime: selectedSlot.startTime,
      endTime: selectedSlot.endTime,
      status: "PENDING"
    };

    try {
      await bookingService.bookLesson(bookingData);
      toast(`Success! Your lesson with ${selectedSlot.coachName} has been requested.`, 'success');
      setSelectedSlotKey('');
      setBookingDate('');
      setPreferredStyle('');
      setActiveView('schedule');
    } catch (error) {
      toast(error.message || 'Failed to submit booking.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const replayTrainingLine = (moves = trainingMoves) => {
    const game = new Chess();
    for (const san of moves) { game.move(san); }
    return game;
  };

  useEffect(() => {
    const replayBoard = new Chess();
    const nextHistory = [];

    for (const san of trainingMoves) {
      replayBoard.move(san);
      nextHistory.push({ san, fen: replayBoard.fen() });
    }

    setTrainingHistory(nextHistory);

    if (trainingMoves.length === 0) {
      setTrainingViewIndex(-1);
      setTrainingFen(START_FEN);
    } else {
      setTrainingViewIndex(trainingMoves.length - 1);
      setTrainingFen(nextHistory[nextHistory.length - 1].fen);
    }
  }, [trainingMoves]);

  const currentTrainingFen = trainingViewIndex === -1
    ? START_FEN
    : trainingHistory[trainingViewIndex]?.fen || trainingFen;

  const trainingTurn = currentTrainingFen.split(' ')[1] === 'w' ? 'White' : 'Black';
  const canStepBack = trainingViewIndex > -1;
  const canStepForward = trainingViewIndex < trainingHistory.length - 1;

  const onTrainingDrop = ({ sourceSquare, targetSquare }) => {
    try {
      const baseMoves = trainingMoves.slice(0, trainingViewIndex + 1);
      const game = replayTrainingLine(baseMoves);
      const move = game.move({ from: sourceSquare, to: targetSquare, promotion: 'q' });

      if (!move) return false;
      setTrainingMoves([...baseMoves, move.san]);
      return true;
    } catch (error) {
      return false;
    }
  };

  const resetTrainingBoard = () => {
    setTrainingMoves([]);
    setTrainingHistory([]);
    setTrainingViewIndex(-1);
    setTrainingFen(START_FEN);
  };

  const undoTrainingMove = () => {
    setTrainingMoves(prev => prev.slice(0, -1));
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
        <button className={`student-side-btn font-serif ${activeView === 'profile' ? 'student-side-btn-active' : ''}`} onClick={() => setActiveView('profile')}>Account Profile</button>
      </aside>

      <main className="student-main-content">
        <div className="student-header">
          <span style={{ marginRight: '1.5rem', fontWeight: 'bold' }}>{user?.fullName} (Student)</span>
          <button onClick={handleLogout} className="student-logout-btn font-serif">Logout</button>
        </div>

        {activeView === 'profile' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Edit Profile</h2>
            <div className="student-booking-section">
              <form onSubmit={handleProfileUpdate}>
                <div className="student-form-group">
                  <label>Full Name</label>
                  <input type="text" className="student-date-input" value={profileForm.fullName} onChange={(e) => setProfileForm({...profileForm, fullName: e.target.value})} required />
                </div>
                <div className="student-form-group">
                  <label>Chess Username</label>
                  <input type="text" className="student-date-input" value={profileForm.chessUsername} onChange={(e) => setProfileForm({...profileForm, chessUsername: e.target.value})} placeholder="Chess.com or Lichess handle" />
                </div>
                <div className="student-form-group">
                  <label>Personal ELO Rating</label>
                  <input type="number" className="student-date-input" value={profileForm.currentElo} onChange={(e) => setProfileForm({...profileForm, currentElo: Number(e.target.value)})} />
                </div>
                <button type="submit" className="student-submit-btn" disabled={isSavingProfile}>
                  {isSavingProfile ? 'Saving...' : 'Save Updates'}
                </button>
              </form>
            </div>
          </>
        )}

        {activeView === 'board' && (
          <div className="student-training-view">
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Tactics Board</h2>
            <div className="student-training-shell">
              <section className="student-board-container">
                <div className="student-board-toolbar">
                  <span className={`student-turn-pill ${trainingTurn === 'Black' ? 'student-turn-pill-dark' : ''}`}>
                    {trainingTurn} to move
                  </span>
                  <button type="button" className="student-icon-btn" onClick={() => setTrainingOrientation(prev => prev === 'white' ? 'black' : 'white')} title="Flip board">
                    <RefreshCw size={18} />
                  </button>
                </div>
                <div className="student-training-board">
                  <Chessboard options={{ position: currentTrainingFen, onPieceDrop: onTrainingDrop, boardOrientation: trainingOrientation, animationDurationInMs: 160 }} />
                </div>
                <div className="student-board-controls">
                  <button type="button" className="student-icon-btn" onClick={() => setTrainingViewIndex(-1)} disabled={!canStepBack}><ChevronsLeft size={18} /></button>
                  <button type="button" className="student-icon-btn" onClick={() => setTrainingViewIndex(prev => Math.max(-1, prev - 1))} disabled={!canStepBack}><ChevronLeft size={18} /></button>
                  <button type="button" className="student-icon-btn" onClick={() => setTrainingViewIndex(prev => Math.min(trainingHistory.length - 1, prev + 1))} disabled={!canStepForward}><ChevronRight size={18} /></button>
                  <button type="button" className="student-icon-btn" onClick={() => setTrainingViewIndex(trainingHistory.length - 1)} disabled={!canStepForward}><ChevronsRight size={18} /></button>
                  <button type="button" className="student-icon-btn" onClick={undoTrainingMove} disabled={trainingMoves.length === 0}><RotateCcw size={18} /></button>
                </div>
              </section>

              <aside className="student-training-panel">
                <h3 className="font-serif">Move History</h3>
                <div className="student-training-history">
                  {trainingHistory.length === 0 ? (
                    <p className="student-empty-history">Play a move to start analysis.</p>
                  ) : (
                    <table>
                      <tbody>
                        {Array.from({ length: Math.ceil(trainingHistory.length / 2) }).map((_, rowIndex) => {
                          const whiteIndex = rowIndex * 2;
                          const blackIndex = whiteIndex + 1;
                          return (
                            <tr key={rowIndex}>
                              <td className="student-move-number">{rowIndex + 1}.</td>
                              <td><button type="button" className={`student-move-btn ${trainingViewIndex === whiteIndex ? 'student-move-btn-active' : ''}`} onClick={() => setTrainingViewIndex(whiteIndex)}>{trainingHistory[whiteIndex]?.san}</button></td>
                              <td>{trainingHistory[blackIndex] && ( <button type="button" className={`student-move-btn ${trainingViewIndex === blackIndex ? 'student-move-btn-active' : ''}`} onClick={() => setTrainingViewIndex(blackIndex)}>{trainingHistory[blackIndex].san}</button>)}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  )}
                </div>
                <button type="button" className="student-clear-line-btn" onClick={resetTrainingBoard} disabled={trainingMoves.length === 0}>Clear Line</button>
              </aside>
            </div>
          </div>
        )}

        {activeView === 'booking' && (
          <>
            <h2 className="font-serif" style={{ color: '#6B4F3A', marginBottom: '2rem', fontSize: '2.5rem' }}>Schedule a Session</h2>
            <div className="student-booking-section">
              <form onSubmit={handleBookingSubmit}>
                <div className="student-form-group">
                  <label>Select Date</label>
                  <input type="date" className="student-date-input" value={bookingDate} min={todayInputValue} onChange={(e) => { setBookingDate(e.target.value); setSelectedSlotKey(''); }} required />
                </div>
                <div className="student-form-group">
                  <label>Preferred Playstyle</label>
                  <select className="student-select" value={preferredStyle} onChange={(e) => { setPreferredStyle(e.target.value); setSelectedSlotKey(''); }}>
                    <option value="">Any playstyle</option>
                    {PLAYSTYLE_OPTIONS.map(style => <option key={style} value={style}>{style}</option>)}
                  </select>
                </div>
                <div className="student-form-group">
                  <label>Available Lesson Times</label>
                  <div className="student-coach-results">
                    {availableSlots.length === 0 ? (
                      <div className="student-no-coaches">
                        {bookingDate ? 'No bookable lesson times match this date and playstyle yet.' : 'Choose a date to see precise coach availability.'}
                      </div>
                    ) : (
                      availableSlots.map(slot => {
                        const isConflicting = Boolean(slot.studentConflict);
                        return (
                          <button type="button" key={getSlotKey(slot)} className={`student-coach-card ${selectedSlotKey === getSlotKey(slot) ? 'student-coach-card-active' : ''} ${isConflicting ? 'student-coach-card-conflict' : ''}`} onClick={() => { if (!isConflicting) setSelectedSlotKey(getSlotKey(slot)); }} disabled={isConflicting}>
                            <div>
                              <strong>
                                {slot.coachName}
                                {slot.eloVerified && (
                                  <span style={{ backgroundColor: '#C29B31', color: 'white', fontSize: '0.75rem', padding: '3px 8px', borderRadius: '12px', marginLeft: '8px', fontWeight: 'bold' }}>
                                    ✓ Verified Coach
                                  </span>
                                )}
                              </strong>
                              <span>{formatSlotTime(slot.startTime)} - {formatSlotTime(slot.endTime)}</span>
                            </div>
                            <p>{slot.specialties || 'No playstyle listed yet.'}</p>
                            <div className="student-slot-meta">
                              <span>{bookingDate ? formatBookingDate(bookingDate) : 'Selected date'}</span>
                              <span>{slot.currentElo || 0} ELO</span>
                              {isConflicting && <span className="student-conflict-chip">{slot.conflictLabel || 'Schedule conflict'}</span>}
                            </div>
                          </button>
                        );
                      })
                    )}
                  </div>
                </div>
                <button type="submit" className="student-submit-btn" disabled={isSubmitting || !selectedSlotKey}>
                  {isSubmitting ? 'Requesting...' : 'Confirm Booking'}
                </button>
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
                            canJoinLesson(lesson.startTime)
                              ? <button onClick={() => navigate(`/lesson/${lesson.id}`)} className="student-action-btn font-serif">Join Lesson</button>
                              : <button disabled className="student-action-btn font-serif" style={{ opacity: 0.45, cursor: 'not-allowed', background: '#9e9e9e' }} title="Available 10 minutes before the lesson starts">{joinCountdownLabel(lesson.startTime)}</button>
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
                        <td><button onClick={() => navigate(`/lesson/${lesson.id}`)} className="student-action-btn student-action-btn-secondary font-serif">View Notes</button></td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}
      </main>
      <ToastContainer />
    </div>
  );
};

export default StudentDashboard;