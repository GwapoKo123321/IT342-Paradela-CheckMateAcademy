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
  const { id } = useParams(); // Gets the lesson ID from the URL
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user'));

  const [lesson, setLesson] = useState(null);
  const [notes, setNotes] = useState('');

  // Chess logic state
  const [board, setBoard] = useState(initialBoardState);
  const [selectedSquare, setSelectedSquare] = useState(null);

  useEffect(() => {
    const fetchLesson = async () => {
      try {
        const data = await bookingService.getLessonById(id);
        setLesson(data);
        setNotes(data.notes || '');
      } catch (error) {
        alert("Failed to load session data.");
        navigate('/dashboard');
      }
    };
    fetchLesson();
  }, [id, navigate]);

  const handleSaveNotes = async () => {
    try {
      await bookingService.saveLessonNotes(id, notes);
      alert("Notes saved successfully!");
    } catch (error) {
      alert("Failed to save notes.");
    }
  };

  // Click-to-Move Chess Logic
  const handleSquareClick = (row, col) => {
    if (selectedSquare) {
      // Move piece
      const newBoard = board.map(r => [...r]);
      newBoard[row][col] = newBoard[selectedSquare.row][selectedSquare.col];
      newBoard[selectedSquare.row][selectedSquare.col] = '';
      setBoard(newBoard);
      setSelectedSquare(null);
    } else {
      // Select piece (only if square is not empty)
      if (board[row][col] !== '') {
        setSelectedSquare({ row, col });
      }
    }
  };

  if (!lesson) return <div style={{ padding: '3rem', textAlign: 'center' }}>Loading Session...</div>;

  const isCoach = user.role === 'Coach';

  return (
    <div className="al-wrapper">
      <div className="al-header">
        <div>
          <h2 className="font-serif">Live Session</h2>
          <p>{lesson.coachName} (Coach) vs {lesson.studentName} (Student)</p>
        </div>
        <button className="al-btn-exit" onClick={() => navigate('/dashboard')}>Leave Session</button>
      </div>

      <div className="al-grid">
        {/* Playable Chess Board */}
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

        {/* Dynamic Notes Panel based on Role */}
        <div className="al-notes-panel">
          <h2 className="font-serif">Coach's Notes</h2>

          {isCoach ? (
            <>
              <textarea
                className="al-notes-area"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Write lesson feedback, tactics, and homework here..."
              />
              <button className="al-btn-save font-serif" onClick={handleSaveNotes}>Save Notes</button>
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