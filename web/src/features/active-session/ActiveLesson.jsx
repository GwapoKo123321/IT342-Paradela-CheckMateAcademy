import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Chess } from 'chess.js';
import { Chessboard } from 'react-chessboard';
import bookingService from '../booking/bookingService';
import reportService from '../report/reportService';
import './ActiveLesson.css';

const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

const loadPgn = (game, pgnText) => {
  if (!pgnText?.trim()) return true;

  try {
    if (typeof game.loadPgn === 'function') {
      game.loadPgn(pgnText);
      return true;
    }
    return game.load_pgn(pgnText) !== false;
  } catch (error) {
    return false;
  }
};

const ActiveLesson = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(sessionStorage.getItem('user') || '{}');

  const [lesson, setLesson] = useState(null);

  // Chat State
  const [chatMessages, setChatMessages] = useState([]);
  const [currentMessage, setCurrentMessage] = useState('');
  const chatEndRef = useRef(null);

  // Board & History State
  const [fen, setFen] = useState(START_FEN);
  const [pgn, setPgn] = useState('');
  const historyListRef = useRef(null);
  const [orientation, setOrientation] = useState(user.role === 'Coach' ? 'white' : 'black');

  // Move History Tracking
  const [gameHistory, setGameHistory] = useState([]);
  const [viewIndex, setViewIndex] = useState(-1);

  const isCoach = user.role === 'Coach';
  const isCompleted = lesson?.status === 'COMPLETED';

  // Refs
  const fenRef = useRef(fen);
  const pgnRef = useRef(pgn);
  const pendingBoardStateRef = useRef(null);
  const pendingNotesRef = useRef(null);
  const chatMessagesRef = useRef(chatMessages);
  const lastLocalMoveTime = useRef(0);
  const lastChatTimeRef = useRef(0);
  const viewIndexRef = useRef(viewIndex);

  useEffect(() => {
    fenRef.current = fen;
    pgnRef.current = pgn;
    chatMessagesRef.current = chatMessages;
  }, [fen, pgn, chatMessages]);

  useEffect(() => {
    viewIndexRef.current = viewIndex;
  }, [viewIndex]);

  // Update gameHistory whenever PGN changes
  useEffect(() => {
    const tempChess = new Chess();
    try {
      loadPgn(tempChess, pgn);

      const rawMoves = tempChess.history();
      const replayBoard = new Chess();
      const hist = [];

      for (let i = 0; i < rawMoves.length; i++) {
        replayBoard.move(rawMoves[i]);
        hist.push({
          san: rawMoves[i],
          fen: replayBoard.fen()
        });
      }

      setGameHistory(hist);

      const wasViewingLatest = viewIndex === gameHistory.length - 1 || gameHistory.length === 0;

      if ((Date.now() - lastLocalMoveTime.current < 3000 || wasViewingLatest) && hist.length > 0) {
        viewIndexRef.current = hist.length - 1;
        setViewIndex(hist.length - 1);
      } else if (isCompleted && viewIndex === -1 && hist.length > 0) {
        viewIndexRef.current = hist.length - 1;
        setViewIndex(hist.length - 1);
      }
    } catch (e) {
      // Ignore PGN parsing errors
    }
  }, [pgn, isCompleted, viewIndex, gameHistory.length]);

  // Auto-scroll chat
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

  // Auto-scroll move history when viewIndex changes
  useEffect(() => {
    const activeMoveEl = historyListRef.current?.querySelector('.active-move');
    if (activeMoveEl) {
      activeMoveEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }, [viewIndex]);

  useEffect(() => {
    const loadSession = async () => {
      try {
        const data = await bookingService.getLessonById(id);
        setLesson(data);

        if (data.notes) {
          try {
            setChatMessages(JSON.parse(data.notes));
          } catch (e) {
            setChatMessages([{ sender: 'System', name: 'System', text: data.notes, time: new Date().toLocaleTimeString() }]);
          }
        }

        if (data.boardState && !data.boardState.includes('[')) {
          setFen(data.boardState);
          setPgn(data.pgnHistory || '');
        }
      } catch (error) {
        navigate('/dashboard');
      }
    };
    loadSession();

    const syncInterval = setInterval(async () => {
      if (isCompleted) return;

      try {
        const requestStartedAt = Date.now();
        const data = await bookingService.getLessonById(id);
        setLesson(data);

        // Board Sync
        if (requestStartedAt >= lastLocalMoveTime.current) {
          const serverBoardState = data.boardState;

          if (serverBoardState && !serverBoardState.includes('[')) {
            if (serverBoardState === pendingBoardStateRef.current) {
              pendingBoardStateRef.current = null;
            }

            if (!pendingBoardStateRef.current && serverBoardState !== fenRef.current) {
              setFen(serverBoardState);
              fenRef.current = serverBoardState;
              setPgn(data.pgnHistory || '');
            }
          }
        }

        // Chat Sync
        if (requestStartedAt >= lastChatTimeRef.current) {
          if (data.notes) {
            if (data.notes === pendingNotesRef.current) {
              pendingNotesRef.current = null;
            }

            if (pendingNotesRef.current) return;

            try {
              const parsed = JSON.parse(data.notes);
              if (JSON.stringify(parsed) !== JSON.stringify(chatMessagesRef.current)) {
                setChatMessages(parsed);
                chatMessagesRef.current = parsed;
              }
            } catch (e) {
              // Ignore
            }
          }
        }

      } catch (e) {
        console.error("Sync failed");
      }
    }, 2000);

    return () => clearInterval(syncInterval);
  }, [id, isCoach, isCompleted, navigate]);

  // Chat Submission
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!currentMessage.trim() || isCompleted) return;

    const newMessage = {
      sender: user.role,
      name: user.fullName || user.role,
      text: currentMessage.trim(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    const updatedMessages = [...chatMessages, newMessage];
    const updatedNotes = JSON.stringify(updatedMessages);

    lastChatTimeRef.current = Date.now();
    pendingNotesRef.current = updatedNotes;
    setChatMessages(updatedMessages);
    chatMessagesRef.current = updatedMessages;
    setCurrentMessage('');

    try {
      const updatedLesson = await bookingService.saveLessonNotes(id, updatedNotes);
      setLesson(updatedLesson);
      if (updatedLesson.notes === pendingNotesRef.current) {
        pendingNotesRef.current = null;
      }
    } catch (error) {
      pendingNotesRef.current = null;
      console.error("Failed to send message.");
    }
  };

  const updateViewIndex = (nextIndex) => {
    viewIndexRef.current = nextIndex;
    setViewIndex(nextIndex);
  };

  const goToStart = () => updateViewIndex(-1);
  const goBack = () => updateViewIndex(Math.max(-1, viewIndexRef.current - 1));
  const goForward = () => updateViewIndex(Math.min(gameHistory.length - 1, viewIndexRef.current + 1));
  const goToLatest = () => updateViewIndex(gameHistory.length - 1);

  const handleSaveViewedBoardState = async () => {
    if (isCompleted) return;

    const boardStateToSave = currentViewFen;
    pendingBoardStateRef.current = boardStateToSave;
    lastLocalMoveTime.current = Date.now();
    setFen(boardStateToSave);
    fenRef.current = boardStateToSave;

    try {
      const updatedLesson = await bookingService.updateBoardState(id, boardStateToSave, pgnRef.current);
      setLesson(updatedLesson);
      if (updatedLesson.boardState === pendingBoardStateRef.current) {
        pendingBoardStateRef.current = null;
      }
    } catch (error) {
      pendingBoardStateRef.current = null;
      console.error("Failed to save board state.");
    }
  };

  const handleReportUser = async () => {
    const reason = window.prompt("Why are you reporting this user? (e.g., Offensive language, Cheating)");
    if (!reason || !reason.trim()) return;

    const reportedId = isCoach ? lesson.studentId : lesson.coachId;
    const reportedName = isCoach ? lesson.studentName : lesson.coachName;

    try {
      await reportService.submitReport({
        reporterId: user.id,
        reportedId: reportedId,
        reportedName: reportedName,
        reason: reason.trim()
      });
      alert("Report submitted successfully. An Admin will review this immediately.");
    } catch (error) {
      alert("Failed to submit report.");
    }
  };

  const currentViewFen = viewIndex === -1 && gameHistory.length > 0
    ? START_FEN
    : (viewIndex >= 0 && gameHistory[viewIndex]?.fen)
      ? gameHistory[viewIndex].fen
      : fen;

  const onDrop = ({ sourceSquare, targetSquare }) => {
    if (isCompleted) return false;

    const isAtPresent = viewIndexRef.current === gameHistory.length - 1 || (gameHistory.length === 0 && viewIndexRef.current === -1);
    if (!isAtPresent) return false;

    const hasPgnHistory = Boolean(pgnRef.current?.trim());
    const gameCopy = hasPgnHistory ? new Chess() : new Chess(fenRef.current);

    try {
      if (hasPgnHistory && !loadPgn(gameCopy, pgnRef.current)) return false;

      const move = gameCopy.move({
        from: sourceSquare,
        to: targetSquare,
        promotion: 'q',
      });

      if (move === null) return false;

      const newFen = gameCopy.fen();
      const newPgn = gameCopy.pgn();
      const latestMoveIndex = gameCopy.history().length - 1;
      setFen(newFen);
      setPgn(newPgn);
      updateViewIndex(latestMoveIndex);
      fenRef.current = newFen;
      pgnRef.current = newPgn;
      pendingBoardStateRef.current = newFen;

      lastLocalMoveTime.current = Date.now();

      bookingService.updateBoardState(id, newFen, newPgn)
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

  const currentTurn = currentViewFen.split(' ')[1] === 'w' ? 'White' : 'Black';

  const movePairs = [];
  for (let i = 0; i < gameHistory.length; i += 2) {
    movePairs.push({
      turn: Math.floor(i / 2) + 1,
      white: gameHistory[i],
      whiteIndex: i,
      black: gameHistory[i + 1] ? gameHistory[i + 1] : null,
      blackIndex: i + 1
    });
  }

  return (
    <div className="al-wrapper">
      <div className="al-header">
        <div>
          <h2 className="font-serif" style={{ margin: 0, fontSize: '2rem' }}>
            {isCompleted ? 'Lesson Review' : 'Live Session'}
            {isCompleted ? (
              <span className="al-badge-completed" style={{marginLeft: '10px', fontSize: '1rem', backgroundColor: '#555', padding: '4px 8px', borderRadius: '4px', color: 'white'}}>COMPLETED</span>
            ) : (
              <span className="al-badge-live" style={{marginLeft: '10px', fontSize: '1rem', backgroundColor: '#E04F5F', padding: '4px 8px', borderRadius: '4px', color: 'white'}}>● LIVE</span>
            )}
          </h2>
          <p style={{ margin: '0.5rem 0 0 0', opacity: 0.9 }}>
            {lesson.coachName} (Coach) vs {lesson.studentName} (Student)
          </p>
        </div>

        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
          <button
            onClick={handleReportUser}
            style={{
              backgroundColor: 'transparent',
              color: '#FFB3B3',
              padding: '8px 16px',
              borderRadius: '25px',
              border: '2px solid #FFB3B3',
              fontWeight: 'bold',
              cursor: 'pointer',
              fontSize: '0.9rem'
            }}
          >
            🚩 Report Chat
          </button>

          <button
            className={isCompleted ? 'al-btn-back' : 'al-btn-exit'}
            onClick={() => navigate('/dashboard')}
            style={{
              backgroundColor: isCompleted ? '#C29B31' : '#E04F5F',
              color: 'white',
              padding: '10px 24px',
              borderRadius: '25px',
              border: 'none',
              fontWeight: 'bold',
              cursor: 'pointer',
              fontSize: '1rem'
            }}
          >
            {isCompleted ? 'Back to Dashboard' : 'Leave Session'}
          </button>
        </div>
      </div>

      <div className="al-grid">
        <div className="al-side-panel al-history-panel" style={{backgroundColor: 'white', display: 'flex', flexDirection: 'column'}}>
          <h2 className="font-serif" style={{ margin: '0 0 1rem 0', fontSize: '1.8rem', color: '#6B4F3A' }}>
            Move History
          </h2>

          <div ref={historyListRef} style={{ flex: 1, overflowY: 'auto', paddingRight: '5px' }}>
            {movePairs.length === 0 ? (
              <div style={{ color: '#888', fontStyle: 'italic', textAlign: 'center', padding: '2rem 0' }}>
                Waiting for the first move...
              </div>
            ) : (
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '1rem', color: '#333' }}>
                <tbody>
                  {movePairs.map((pair) => (
                    <tr key={pair.turn} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '8px 4px', color: '#888', width: '30px' }}>{pair.turn}.</td>
                      <td style={{ padding: '4px' }}>
                        <div
                          className={viewIndex === pair.whiteIndex ? 'active-move' : ''}
                          onClick={() => updateViewIndex(pair.whiteIndex)}
                          style={{
                            padding: '6px 10px',
                            cursor: 'pointer',
                            borderRadius: '6px',
                            fontWeight: '500',
                            backgroundColor: viewIndex === pair.whiteIndex ? '#D4AF37' : 'transparent',
                            color: viewIndex === pair.whiteIndex ? '#6B4F3A' : '#333'
                          }}
                        >
                          {pair.white.san}
                        </div>
                      </td>
                      <td style={{ padding: '4px' }}>
                        {pair.black && (
                          <div
                            className={viewIndex === pair.blackIndex ? 'active-move' : ''}
                            onClick={() => updateViewIndex(pair.blackIndex)}
                            style={{
                              padding: '6px 10px',
                              cursor: 'pointer',
                              borderRadius: '6px',
                              fontWeight: '500',
                              backgroundColor: viewIndex === pair.blackIndex ? '#D4AF37' : 'transparent',
                              color: viewIndex === pair.blackIndex ? '#6B4F3A' : '#333'
                            }}
                          >
                            {pair.black.san}
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', backgroundColor: '#F5F1EA', borderRadius: '10px', padding: '5px' }}>
            <button onClick={goToStart} disabled={viewIndex === -1} style={{flex: 1, padding: '10px', background: 'none', border: 'none', cursor: viewIndex === -1 ? 'not-allowed' : 'pointer', color: '#6B4F3A', fontWeight: 'bold', fontSize: '1.2rem', opacity: viewIndex === -1 ? 0.45 : 1}}>|&lt;</button>
            <button onClick={goBack} disabled={viewIndex === -1} style={{flex: 1, padding: '10px', background: 'none', border: 'none', cursor: viewIndex === -1 ? 'not-allowed' : 'pointer', color: '#6B4F3A', fontWeight: 'bold', fontSize: '1.2rem', opacity: viewIndex === -1 ? 0.45 : 1}}>&lt;</button>
            <button onClick={goForward} disabled={viewIndex >= gameHistory.length - 1} style={{flex: 1, padding: '10px', background: 'none', border: 'none', cursor: viewIndex >= gameHistory.length - 1 ? 'not-allowed' : 'pointer', color: '#6B4F3A', fontWeight: 'bold', fontSize: '1.2rem', opacity: viewIndex >= gameHistory.length - 1 ? 0.45 : 1}}>&gt;</button>
            <button onClick={goToLatest} disabled={viewIndex >= gameHistory.length - 1} style={{flex: 1, padding: '10px', background: 'none', border: 'none', cursor: viewIndex >= gameHistory.length - 1 ? 'not-allowed' : 'pointer', color: '#6B4F3A', fontWeight: 'bold', fontSize: '1.2rem', opacity: viewIndex >= gameHistory.length - 1 ? 0.45 : 1}}>&gt;|</button>
          </div>

          <button
            onClick={handleSaveViewedBoardState}
            disabled={isCompleted}
            style={{
              width: '100%',
              marginTop: '0.75rem',
              padding: '0.85rem 1rem',
              backgroundColor: '#6B4F3A',
              color: 'white',
              border: 'none',
              borderRadius: '10px',
              cursor: isCompleted ? 'not-allowed' : 'pointer',
              fontWeight: 'bold',
              opacity: isCompleted ? 0.6 : 1
            }}
          >
            Save Current Board State
          </button>
        </div>

        <div className="al-board-panel">
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
          <div style={{ width: '100%', maxWidth: '600px', aspectRatio: '1/1' }}>
            <Chessboard
              options={{
                position: currentViewFen,
                onPieceDrop: onDrop,
                boardOrientation: orientation,
                showAnimations: false,
                animationDurationInMs: 0,
              }}
            />
          </div>
        </div>

        <div className="al-side-panel al-notes-panel" >
          <h2 className="font-serif" style={{ margin: '0 0 1rem 0', fontSize: '1.8rem', color: 'white' }}>
            Live Chat
          </h2>
          <div style={{ flex: 1, backgroundColor: '#F5F1EA', borderRadius: '12px', padding: '1rem', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '1rem' }}>
            {chatMessages.length === 0 ? (
              <div style={{ textAlign: 'center', color: '#888', marginTop: '2rem', fontStyle: 'italic' }}>
                No messages yet. Say hello!
              </div>
            ) : (
              chatMessages.map((msg, index) => {
                const isMe = msg.sender === user.role;
                return (
                  <div key={index} style={{ alignSelf: isMe ? 'flex-end' : 'flex-start', maxWidth: '85%', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '0.75rem', color: '#666', marginBottom: '2px', textAlign: isMe ? 'right' : 'left' }}>
                      {msg.name} • {msg.time}
                    </span>
                    <div style={{
                      backgroundColor: isMe ? '#6B4F3A' : 'white',
                      color: isMe ? 'white' : '#333',
                      padding: '10px 15px',
                      borderRadius: isMe ? '15px 15px 0 15px' : '15px 15px 15px 0',
                      boxShadow: '0 2px 5px rgba(0,0,0,0.05)',
                      lineHeight: '1.4'
                    }}>
                      {msg.text}
                    </div>
                  </div>
                );
              })
            )}
            <div ref={chatEndRef} />
          </div>
          <form onSubmit={handleSendMessage} style={{ display: 'flex', gap: '10px' }}>
            <input
              type="text"
              value={currentMessage}
              onChange={(e) => setCurrentMessage(e.target.value)}
              disabled={isCompleted}
              placeholder={isCompleted ? "Chat disabled (Session Completed)" : "Type a message..."}
              style={{ flex: 1, padding: '12px 15px', borderRadius: '25px', border: 'none', outline: 'none', fontSize: '1rem' }}
            />
            <button
              type="submit"
              disabled={isCompleted || !currentMessage.trim()}
              style={{
                backgroundColor: '#D4AF37',
                color: '#6B4F3A',
                border: 'none',
                borderRadius: '25px',
                padding: '0 20px',
                fontWeight: 'bold',
                cursor: (isCompleted || !currentMessage.trim()) ? 'not-allowed' : 'pointer',
                opacity: (isCompleted || !currentMessage.trim()) ? 0.6 : 1
              }}
            >
              Send
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ActiveLesson;