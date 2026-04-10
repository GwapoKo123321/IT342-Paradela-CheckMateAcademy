import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import bookingService from '../api/bookingService';
import './ActiveLesson.css';

const initialBoardState = [
  ['♜', '♞', '♝', '♛', '♚', '♝', '♞', '♜'],
  ['♟', '♟', '♟', '♟', '♟', '♟', '♟', '♟'],
  ['', '', '', '', '', '', '', ''],
  ['', '', '', '', '', '', '', ''],
  ['', '', '', '', '', '', '', ''],
  ['', '', '', '', '', '', '', ''],
  ['♙', '♙', '♙', '♙', '♙', '♙', '♙', '♙'],
  ['♖', '♘', '♗', '♕', '♔', '♗', '♘', '♖']
];

const ActiveLesson = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));

  const [lesson, setLesson] = useState(null);
  const [notes, setNotes] = useState('');

  const [board, setBoard] = useState(initialBoardState);
  const [selectedSquare, setSelectedSquare] = useState(null);

  const isCoach = user.role === 'Coach';
  const isCompleted = lesson?.status === 'COMPLETED';

  useEffect(() => {
    // Initial Load
    const loadSession = async () => {
      try {
        const data = await bookingService.getLessonById(id);
        setLesson(data);
        setNotes(data.notes || '');
        if (data.boardState) setBoard(JSON.parse(data.boardState));
      } catch (error) {
        navigate('/dashboard');
      }
    };
    loadSession();


    const syncInterval = setInterval(async () => {
      if (isCompleted) return; // Stop syncing if the lesson is over

      try {
        const data = await bookingService.getLessonById(id);
        setLesson(data);

        // Update the board if the opponent moved
        if (data.boardState) {
          setBoard(prev => {
            const newState = data.boardState;
            const oldState = JSON.stringify(prev);
            return newState !== oldState ? JSON.parse(newState) : prev;
          });
        }


        if (!isCoach) {
          setNotes(prev => data.notes !== prev ? (data.notes || '') : prev);
        }
      } catch (e) {
        console.error("Sync failed");
      }
    }, 2000);

    return () => clearInterval(syncInterval);
  }, [id, navigate, isCoach, isCompleted]);

  const handleSaveNotes = async () => {
    try {
      await bookingService.saveLessonNotes(id, notes);
      alert("Notes saved successfully!");
    } catch (error) {
      alert("Failed to save notes.");
    }
  };

  const handleSquareClick = async (row, col) => {
    if (isCompleted) return; // Prevent moving pieces during a past review

    if (selectedSquare) {
      const newBoard = board.map(r => [...r]);
      newBoard[row][col] = newBoard[selectedSquare.row][selectedSquare.col];
      newBoard[selectedSquare.row][selectedSquare.col] = '';

      setBoard(newBoard);
      setSelectedSquare(null);


      try {
        await bookingService.updateBoardState(id, JSON.stringify(newBoard));
      } catch (e) {
        console.error("Failed to push move");
      }

    } else {
      if (board[row][col] !== '') {
        setSelectedSquare({ row, col });
      }
    }
  };

  if (!lesson) return <div style={{ padding: '3rem', textAlign: 'center', fontFamily: 'Inter' }}>Loading Session...</div>;

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
        <div className="al-board-panel">
          <div className="al-chess-grid">
            {board.map((row, rowIndex) =>
              row.map((piece, colIndex) => {
                const isDark = (rowIndex + colIndex) % 2 === 1;
                const isSelected = selectedSquare?.row === rowIndex && selectedSquare?.col === colIndex;
                return (
                  <div
                    key={`${rowIndex}-${colIndex}`}
                    className={`al-square ${isDark ? 'al-dark' : 'al-light'} ${isSelected ? 'al-selected' : ''}`}
                    onClick={() => handleSquareClick(rowIndex, colIndex)}
                  >
                    {piece}
                  </div>
                );
              })
            )}
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