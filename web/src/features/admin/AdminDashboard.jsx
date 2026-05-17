import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import adminService from './adminService';
import reportService from '../report/reportService';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [users, setUsers] = useState([]);
  const [reports, setReports] = useState([]);

  const loadUsers = async () => {
    try {
      const data = await adminService.getAllUsers();
      setUsers(data);
    } catch (error) {
      console.error("Failed to load users", error);
    }
  };

  const loadReports = async () => {
    try {
      const data = await reportService.getAllReports();
      setReports(data);
    } catch (error) {
      console.error("Failed to load reports", error);
    }
  };

  useEffect(() => {
    if (user.role === 'Admin') {
      loadUsers();
      loadReports();
    } else {
      navigate('/dashboard');
    }
  }, [user.role, navigate]);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleRoleChange = async (userId, newRole) => {
    try {
      await adminService.updateUserRole(userId, newRole);
      loadUsers();
    } catch (error) {
      alert("Failed to update user role.");
    }
  };

  const handleDelete = async (userId) => {
    if (!window.confirm("Are you sure you want to delete this user? This cannot be undone.")) return;
    try {
      await adminService.deleteUser(userId);
      loadUsers();
    } catch (error) {
      alert("Failed to delete user. They might have active lessons attached to their account.");
    }
  };

  const handleToggleFlag = async (userId) => {
    try {
      await adminService.toggleUserFlag(userId);
      loadUsers();
    } catch (error) {
      alert("Failed to toggle flag.");
    }
  };

  const handleToggleElo = async (userId) => {
    try {
      await adminService.toggleEloVerification(userId);
      loadUsers();
    } catch (error) {
      alert("Failed to verify ELO.");
    }
  };

  const handleResolveReport = async (reportId) => {
    try {
      await reportService.resolveReport(reportId);
      loadReports();
    } catch (error) {
      alert("Failed to resolve report.");
    }
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <h2 className="font-serif" style={{ textAlign: 'center', marginBottom: '1rem' }}>Admin Panel</h2>
        <div style={{ color: '#ccc', textAlign: 'center', fontStyle: 'italic' }}>System Overview</div>
      </aside>

      <main className="admin-main-content">
        <div className="admin-header">
          <span style={{ marginRight: '1.5rem', fontWeight: 'bold' }}>{user.fullName} (Admin)</span>
          <button onClick={handleLogout} className="admin-logout-btn font-serif">Logout</button>
        </div>

        <h2 className="font-serif" style={{ color: '#1a1a1a', margin: '0 0 2rem 0', fontSize: '2.5rem' }}>User Management</h2>

        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>User Details</th>
                <th>Role</th>
                <th>Safety & Verification</th>
                <th>Manage</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className={u.isFlagged ? 'user-row-flagged' : ''}>
                  <td>
                    <strong style={{ display: 'block', fontSize: '1.1rem' }}>{u.fullName}</strong>
                    <span style={{ color: '#666', fontSize: '0.9rem' }}>{u.email}</span>
                    {u.role === 'Coach' && (
                       <div style={{ marginTop: '4px', fontSize: '0.85rem', color: '#6B4F3A', fontWeight: 'bold' }}>
                         {u.chessUsername} • {u.currentElo} ELO
                       </div>
                    )}
                  </td>
                  <td>
                    <select
                      className="admin-select"
                      value={u.role}
                      onChange={(e) => handleRoleChange(u.id, e.target.value)}
                      disabled={u.id === user.id}
                    >
                      <option value="Student">Student</option>
                      <option value="Coach">Coach</option>
                      <option value="Admin">Admin</option>
                    </select>
                  </td>
                  <td>
                    <div className="admin-action-row">
                      <button
                        onClick={() => handleToggleFlag(u.id)}
                        className={`admin-toggle-btn ${u.isFlagged ? 'admin-btn-flagged' : 'admin-btn-clean'}`}
                      >
                        {u.isFlagged ? '🚩 Account Flagged' : 'Flag Account'}
                      </button>

                      {u.role === 'Coach' && (
                        <button
                          onClick={() => handleToggleElo(u.id)}
                          className={`admin-toggle-btn ${u.eloVerified ? 'admin-btn-verified' : 'admin-btn-unverified'}`}
                        >
                          {u.eloVerified ? '✓ ELO Verified' : 'Unverified ELO'}
                        </button>
                      )}
                    </div>
                  </td>
                  <td>
                    <button
                      onClick={() => handleDelete(u.id)}
                      className="admin-delete-btn"
                      disabled={u.id === user.id}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <h2 className="font-serif" style={{ color: '#1a1a1a', margin: '3rem 0 2rem 0', fontSize: '2.5rem' }}>Active Player Reports</h2>

        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Reported User</th>
                <th>Reason Provided</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {reports.length === 0 ? (
                <tr><td colSpan="5" style={{textAlign: 'center', padding: '2rem'}}>No reports pending.</td></tr>
              ) : (
                reports.map(r => (
                  <tr key={r.id}>
                    <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                    <td style={{ fontWeight: 'bold', color: '#E04F5F' }}>{r.reportedName}</td>
                    <td style={{ fontStyle: 'italic' }}>"{r.reason}"</td>
                    <td>
                      <span className={`admin-role-badge ${r.status === 'PENDING' ? 'role-Student' : 'role-Admin'}`}>
                        {r.status}
                      </span>
                    </td>
                    <td>
                      {r.status === 'PENDING' && (
                        <button onClick={() => handleResolveReport(r.id)} className="admin-btn-verified admin-toggle-btn">
                          Mark as Resolved
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;