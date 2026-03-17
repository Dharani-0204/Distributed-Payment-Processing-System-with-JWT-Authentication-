import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import TransactionItem from '../components/TransactionItem';
import './Dashboard.css';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    // ─── State ───────────────────────────────────────
    const [balance, setBalance] = useState(0);
    const [transactions, setTransactions] = useState([]);
    const [stats, setStats] = useState({ totalSent: 0, totalReceived: 0 });
    const [filter, setFilter] = useState('ALL'); // ALL | IN | OUT | PENDING | FAILED

    // Deposit form
    const [depositAmount, setDepositAmount] = useState('');
    const [depositLoading, setDepositLoading] = useState(false);
    const [depositError, setDepositError] = useState('');

    // Transfer form
    const [receiverEmail, setReceiverEmail] = useState('');
    const [transferAmount, setTransferAmount] = useState('');
    const [referenceId, setReferenceId] = useState('');
    const [transferLoading, setTransferLoading] = useState(false);
    const [transferError, setTransferError] = useState('');
    const [transferSuccess, setTransferSuccess] = useState('');

    // Profile dropdown
    const [showProfileMenu, setShowProfileMenu] = useState(false);

    // ─── On mount: load everything ────────────────────
    useEffect(() => {
        if (user) {
            fetchUserData();
            fetchTransactions();
            fetchStats();
        }
    }, [user]);

    // ─── Auto-generate reference ID on load ──────────
    useEffect(() => {
        setReferenceId('TXN-' + Date.now());
    }, []);

    // ─── Format currency in Indian Rupees ────────────
    const formatRupees = (amount) => {
        return '₹' + parseFloat(amount || 0).toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    };

    // ─── API Calls ───────────────────────────────────
    const fetchUserData = async () => {
        try {
            // New secure profile endpoint — uses JWT to identify user
            const res = await api.get('/users/profile');
            setBalance(res.data.balance);
        } catch (e) {
            console.error('Failed to fetch user profile:', e);
        }
    };

    const fetchTransactions = async () => {
        try {
            // New secure endpoint — no userId in URL, extracted from JWT
            const res = await api.get('/transactions');
            setTransactions(res.data);
        } catch (e) {
            console.error('Failed to fetch transactions:', e);
        }
    };

    const fetchStats = async () => {
        try {
            const res = await api.get('/transactions/stats');
            setStats(res.data);
        } catch (e) {
            console.error('Failed to fetch stats:', e);
        }
    };

    // ─── Deposit ─────────────────────────────────────
    const handleDeposit = async (e) => {
        e.preventDefault();
        setDepositError('');
        setDepositLoading(true);
        try {
            // No userId in body — handled server-side via JWT
            const response = await api.post('/wallet/deposit', { amount: parseFloat(depositAmount) });
            console.log('Deposit success:', response.data);
            setDepositAmount('');
            await fetchUserData();
            await fetchTransactions();
            await fetchStats();
        } catch (err) {
            console.error('Deposit error full object:', err);
            const msg = err.response?.data?.message || 'Deposit failed. Please try again.';
            setDepositError(msg);
        } finally {
            setDepositLoading(false);
        }
    };

    // ─── Transfer ────────────────────────────────────
    const handleTransfer = async (e) => {
        e.preventDefault();
        setTransferError('');
        setTransferSuccess('');
        setTransferLoading(true);
        try {
            // No senderId — extracted from JWT. Receiver identified by email.
            await api.post('/payments/transfer', {
                receiverEmail,
                amount: parseFloat(transferAmount),
                referenceId
            });
            setTransferSuccess(`₹${transferAmount} sent to ${receiverEmail} successfully!`);
            setReceiverEmail('');
            setTransferAmount('');
            setReferenceId('TXN-' + Date.now()); // Generate fresh ref ID
            await fetchUserData();
            await fetchTransactions();
            await fetchStats();
        } catch (err) {
            const msg = err.response?.data?.message || 'Transfer failed. Please check details.';
            setTransferError(msg);
        } finally {
            setTransferLoading(false);
        }
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // ─── Filtered transactions ────────────────────────
    const filteredTransactions = transactions.filter(tx => {
        if (filter === 'ALL') return true;
        if (filter === 'IN')  return tx.direction === 'IN';
        if (filter === 'OUT') return tx.direction === 'OUT';
        if (filter === 'PENDING') return tx.status === 'PENDING';
        if (filter === 'FAILED')  return tx.status === 'FAILED';
        return true;
    });

    if (!user) return null;

    return (
        <div className="pw-root">

            {/* ─── Top Navigation Bar ─── */}
            <nav className="pw-navbar">
                <div className="pw-brand">
                    <span className="pw-brand-icon">💳</span>
                    <span className="pw-brand-name">Payment Wallet</span>
                </div>
                <div className="pw-nav-links">
                    <span className="pw-nav-link active">Dashboard</span>
                    <span className="pw-nav-link">Transactions ▾</span>
                </div>
                <div className="pw-nav-right">
                    <div className="pw-user-chip" onClick={() => setShowProfileMenu(!showProfileMenu)}>
                        <div className="pw-avatar">{user.name?.charAt(0).toUpperCase()}</div>
                        <span>Hello, {user.name?.split(' ')[0]}! {showProfileMenu ? '▴' : '▾'}</span>
                        
                        {showProfileMenu && (
                            <div className="pw-profile-dropdown">
                                <div className="pw-dropdown-header">
                                    <strong>{user.name}</strong>
                                    <span>{user.email}</span>
                                </div>
                                <div className="pw-dropdown-divider"></div>
                                <button className="pw-dropdown-item" onClick={handleLogout}>
                                    🚪 Logout
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </nav>

            <div className="pw-content">

                {/* ─── Stats Cards Row ─── */}
                <div className="pw-stats-row">
                    <div className="pw-stat-card stat-balance">
                        <div className="stat-info">
                            <p className="stat-title">Wallet Balance</p>
                            <h2 className="stat-value">{formatRupees(balance)}</h2>
                        </div>
                        <div className="stat-icon">💼</div>
                    </div>
                    <div className="pw-stat-card stat-sent">
                        <div className="stat-info">
                            <p className="stat-title">Total Sent</p>
                            <h2 className="stat-value">{formatRupees(stats.totalSent)}</h2>
                        </div>
                        <div className="stat-icon">📤</div>
                    </div>
                    <div className="pw-stat-card stat-received">
                        <div className="stat-info">
                            <p className="stat-title">Total Received</p>
                            <h2 className="stat-value">{formatRupees(stats.totalReceived)}</h2>
                        </div>
                        <div className="stat-icon">💰</div>
                    </div>
                </div>

                {/* ─── Deposit & Transfer Forms ─── */}
                <div className="pw-forms-row">

                    {/* Deposit Card */}
                    <div className="pw-form-card">
                        <h3 className="form-card-title">Deposit Money</h3>
                        <form onSubmit={handleDeposit} className="pw-form">
                            <input
                                type="number"
                                className="pw-input"
                                placeholder="Amount"
                                value={depositAmount}
                                onChange={(e) => setDepositAmount(e.target.value)}
                                min="1"
                                step="0.01"
                                required
                            />
                            {depositError && <p className="form-error">{depositError}</p>}
                            <button
                                type="submit"
                                className="pw-btn pw-btn-blue"
                                disabled={depositLoading}
                            >
                                {depositLoading ? 'Processing...' : 'Deposit'}
                            </button>
                        </form>
                    </div>

                    {/* Transfer Card */}
                    <div className="pw-form-card">
                        <h3 className="form-card-title">Transfer Money</h3>
                        <form onSubmit={handleTransfer} className="pw-form">
                            <div className="pw-input-row">
                                <input
                                    type="email"
                                    className="pw-input"
                                    placeholder="Recipient Email"
                                    value={receiverEmail}
                                    onChange={(e) => setReceiverEmail(e.target.value)}
                                    required
                                />
                                <input
                                    type="number"
                                    className="pw-input"
                                    placeholder="Amount"
                                    value={transferAmount}
                                    onChange={(e) => setTransferAmount(e.target.value)}
                                    min="1"
                                    step="0.01"
                                    required
                                />
                            </div>
                            <div className="pw-input-row">
                                <input
                                    type="text"
                                    className="pw-input"
                                    placeholder="Reference ID"
                                    value={referenceId}
                                    onChange={(e) => setReferenceId(e.target.value)}
                                    required
                                />
                                <button
                                    type="button"
                                    className="pw-btn pw-btn-icon"
                                    title="Generate new Reference ID"
                                    onClick={() => setReferenceId('TXN-' + Date.now())}
                                >🔄</button>
                            </div>
                            {transferError   && <p className="form-error">{transferError}</p>}
                            {transferSuccess && <p className="form-success">{transferSuccess}</p>}
                            <button
                                type="submit"
                                className="pw-btn pw-btn-purple"
                                disabled={transferLoading}
                            >
                                {transferLoading ? 'Sending...' : 'Send Money'}
                            </button>
                        </form>
                    </div>
                </div>

                {/* ─── Transaction History ─── */}
                <div className="pw-history-card">
                    <div className="history-header">
                        <h3 className="history-title">Transaction History</h3>
                        <div className="history-actions">
                            <button
                                className="icon-btn"
                                title="Refresh"
                                onClick={() => { fetchTransactions(); fetchStats(); fetchUserData(); }}
                            >🔄</button>
                        </div>
                    </div>

                    {/* Filter Tabs */}
                    <div className="tx-filters">
                        <span className="filter-label">Filter:</span>
                        {['ALL', 'IN', 'OUT', 'PENDING', 'FAILED'].map(f => (
                            <button
                                key={f}
                                className={`filter-btn ${filter === f ? 'filter-active' : ''}`}
                                onClick={() => setFilter(f)}
                            >
                                {f}
                            </button>
                        ))}
                    </div>

                    {/* Transaction Rows */}
                    <div className="tx-list">
                        {filteredTransactions.length === 0 ? (
                            <div className="tx-empty">
                                <span>📭</span>
                                <p>No transactions found.</p>
                            </div>
                        ) : (
                            filteredTransactions.map((tx) => (
                                <TransactionItem key={tx.id} tx={tx} />
                            ))
                        )}
                    </div>
                </div>

            </div>
        </div>
    );
};

export default Dashboard;
