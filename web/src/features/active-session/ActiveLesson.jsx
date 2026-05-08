import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Chess } from 'chess.js';
import { Chessboard } from 'react-chessboard';
import bookingService from '../booking/bookingService';
import './ActiveLesson.css';

const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

const ActiveLesson = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const [lesson, setLesson] = useState(null);
  const [notes, setNotes] = useState('');

  const [fen, setFen] = useState(START_FEN);
  const [orientation, setOrientation] = useState(user.role === 'Coach' ? 'white' : 'black');

  const isCoach = user.role === 'Coach';
  const isCompleted = lesson?.status === 'COMPLETED';

  const fenRef = useRef(fen);
  const pendingBoardStateRef = useRef(null);

  useEffect(() => { fenRef.current = fen; }, [fen]);

  useEffect(() => {
    const loadSession = async () => {
      try {
        const data = await bookingService.getLessonById(id);
        setLesson(data);
        setNotes(data.notes || '');

        if (data.boardState && !data.boardState.includes('[')) {
          setFen(data.boardState);
        }
      } catch (error) {
        navigate('/dashboard');
      }
    };
    loadSession();

    const syncInterval = setInterval(async () => {
      if (isCompleted) return;

      try {
        const data = await bookingService.getLessonById(id);
        setLesson(data);

        const serverBoardState = data.boardState;

        if (serverBoardState && !serverBoardState.includes('[')) {
          if (serverBoardState === pendingBoardStateRef.current) {
            pendingBoardStateRef.current = null;
          }

          if (!pendingBoardStateRef.current && serverBoardState !== fenRef.current) {
            setFen(serverBoardState);
            fenRef.current = serverBoardState;
          }
        }

        if (!isCoach) {
          setNotes(prev => data.notes !== prev ? (data.notes || '') : prev);
        }
      } catch (e) {
        console.error("Sync failed");
      }
    }, 2000);

    return () => clearInterval(syncInterval);
  }, [id, isCoach, isCompleted, navigate]);

  const handleSaveNotes = async () => {
    try {
      await bookingService.saveLessonNotes(id, notes);
      alert("Notes saved successfully!");
    } catch (error) {
      alert("Failed to save notes.");
    }
  };

  const onDrop = ({ sourceSquare, targetSquare }) => {
    if (isCompleted) return false;

    const gameCopy = new Chess(fen);

    try {
      const move = gameCopy.move({
        from: sourceSquare,
        to: targetSquare,
        promotion: 'q',
      });

      if (move === null) return false;

      const newFen = gameCopy.fen();
      setFen(newFen);
      fenRef.current = newFen;
      pendingBoardStateRef.current = newFen;

      bookingService.updateBoardState(id, newFen)
        .then((updatedLesson) => {
          setLesson(updatedLesson);
          if (updatedLesson.boardState === pendingBoardStateRef.current) {
            pendingBoardStateRef.current = null;
          }
        })
        .catch(() => {
          pendingBoardStateRef.current = null;
          console.error("Failed to push move to server");
        });

      return true;
    } catch (error) {
      return false;
    }
  };

  if (!lesson) return <div style={{ padding: '3rem', textAlign: 'center', fontFamily: 'Inter' }}>Loading Session...</div>;

  const currentTurn = fen.split(' ')[1] === 'w' ? 'White' : 'Black';

  return (
    <div className="al-wrapper">
      <div className="al-header">
        <div>
          <h2 className="font-serif" style={{ margin: 0, fontSize: '2rem' }}>
            {isCompleted ? 'Lesson Review' : 'Live Session'}
            {isCompleted ? (
              <span className="al-badge-completed">COMPLETED</span>
            ) : (
              <span className="al-badge-live">● LIVE</span>
            )}
          </h2>
          <p style={{ margin: '0.5rem 0 0 0', opacity: 0.9 }}>
            {lesson.coachName} (Coach) vs {lesson.studentName} (Student)
          </p>
        </div>
        <button
          className={isCompleted ? 'al-btn-back' : 'al-btn-exit'}
          onClick={() => navigate('/dashboard')}
        >
          {isCompleted ? 'Back to Dashboard' : 'Leave Session'}
        </button>
      </div>

      <div className="al-grid">
        <div className="al-board-panel" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>

          <div style={{ width: '100%', maxWidth: '600px', display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', alignItems: 'center' }}>
            <span style={{
              fontWeight: 'bold',
              padding: '0.5rem 1rem',
              backgroundColor: currentTurn === 'White' ? '#F5F1EA' : '#6B4F3A',
              color: currentTurn === 'White' ? '#6B4F3A' : '#F5F1EA',
              borderRadius: '8px',
              border: '2px solid #6B4F3A'
            }}>
              {currentTurn === 'White' ? "⚪ White to Move" : "⚫ Black to Move"}
            </span>

            <button
              onClick={() => setOrientation(prev => prev === 'white' ? 'black' : 'white')}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#6B4F3A',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer',
                fontWeight: 'bold'
              }}
            >
              🔄 Flip Board
            </button>
          </div>

          <div style={{ width: '100%', maxWidth: '600px' }}>
            <Chessboard
              options={{
                position: fen,
                onPieceDrop: onDrop,
                boardOrientation: orientation,
                darkSquareStyle: { backgroundColor: '#6B4F3A' },
                lightSquareStyle: { backgroundColor: '#F5F1EA' },
                animationDurationInMs: 200,
              }}
            />
          </div>
        </div>

        <div className="al-notes-panel">
          <h2 className="font-serif" style={{ margin: 0, fontSize: '1.8rem' }}>
            {isCompleted ? "Final Evaluation Notes" : "Coach's Notes"}
          </h2>

          {isCoach ? (
            <>
              <textarea
                className="al-notes-area"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder={isCompleted ? "Add final review remarks here..." : "Write lesson feedback, tactics, and homework here..."}
              />
              <button className="al-btn-save font-serif" onClick={handleSaveNotes}>
                {isCompleted ? "Update Final Review" : "Save Notes"}
              </button>
            </>
          ) : (
            <div className="al-notes-area" style={{ overflowY: 'auto', whiteSpace: 'pre-wrap' }}>
              {notes || "Your coach hasn't added any notes yet."}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ActiveLesson;
