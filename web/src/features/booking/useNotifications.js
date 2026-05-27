

import React, { useCallback, useRef, useState } from 'react';
import './notifications.css';

const ICONS = {
  success: '✓',
  error:   '✕',
  info:    'ℹ',
  warning: '⚠',
};

let _toastId = 0;

export function useToast() {
  const [toasts, setToasts] = useState([]);

  const toast = useCallback((message, type = 'info') => {
    const id = ++_toastId;
    setToasts(prev => [...prev, { id, message, type, leaving: false }]);

    setTimeout(() => {
      setToasts(prev => prev.map(t => t.id === id ? { ...t, leaving: true } : t));
      setTimeout(() => {
        setToasts(prev => prev.filter(t => t.id !== id));
      }, 250);
    }, 3500);
  }, []);

  const dismiss = useCallback((id) => {
    setToasts(prev => prev.map(t => t.id === id ? { ...t, leaving: true } : t));
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 250);
  }, []);

  const ToastContainer = useCallback(() => (
    <div className="cma-toast-root">
      {toasts.map(t => (
        <div key={t.id} className={`cma-toast cma-toast-${t.type} ${t.leaving ? 'cma-toast-out' : ''}`}>
          <span className="cma-toast-icon">{ICONS[t.type] ?? 'ℹ'}</span>
          <span className="cma-toast-text">{t.message}</span>
          <button className="cma-toast-close" onClick={() => dismiss(t.id)}>✕</button>
        </div>
      ))}
    </div>
  ), [toasts, dismiss]);

  return { toast, ToastContainer };
}

export function useConfirm() {
  const [modal, setModal] = useState(null);
  const resolveRef = useRef(null);

  const confirm = useCallback((message, { title = 'Confirm', danger = false } = {}) => {
    return new Promise((resolve) => {
      resolveRef.current = resolve;
      setModal({ message, title, danger });
    });
  }, []);

  const handle = (result) => {
    setModal(null);
    resolveRef.current?.(result);
    resolveRef.current = null;
  };

  const ConfirmModal = useCallback(() => {
    if (!modal) return null;
    return (
      <div className="cma-modal-backdrop" onClick={() => handle(false)}>
        <div className="cma-modal" onClick={e => e.stopPropagation()}>
          <p className="cma-modal-title">{modal.title}</p>
          <p className="cma-modal-body">{modal.message}</p>
          <div className="cma-modal-actions">
            <button className="cma-modal-cancel" onClick={() => handle(false)}>Cancel</button>
            <button className={`cma-modal-confirm ${modal.danger ? 'danger' : ''}`} onClick={() => handle(true)}>
              Confirm
            </button>
          </div>
        </div>
      </div>
    );
  }, [modal]);

  return { confirm, ConfirmModal };
}
