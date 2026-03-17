/**
 * Formats a timestamp into a relative / absolute display string.
 * Examples: "Today, 3:15 PM", "Yesterday, 1:00 PM", "Apr 15, 2024, 10:00 AM"
 */
function formatTime(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    const now = new Date();

    const isToday = date.toDateString() === now.toDateString();
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    const isYesterday = date.toDateString() === yesterday.toDateString();

    const timeStr = date.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });

    if (isToday) return `Today, ${timeStr}`;
    if (isYesterday) return `Yesterday, ${timeStr}`;
    return date.toLocaleDateString('en-IN', {
        month: 'short', day: 'numeric', year: 'numeric'
    }) + ', ' + timeStr;
}

/**
 * Returns the icon and color class for a transaction based on direction and status.
 */
function getIconConfig(direction, status, type) {
    if (type === 'DEPOSIT') {
        return { icon: '↑', colorClass: 'icon-green', label: 'deposit' };
    }
    if (status === 'PENDING') {
        return { icon: '⏱', colorClass: 'icon-orange', label: 'pending' };
    }
    if (status === 'FAILED') {
        return { icon: '↑', colorClass: 'icon-red', label: 'failed' };
    }
    if (direction === 'IN') {
        return { icon: '↓', colorClass: 'icon-green', label: 'received' };
    }
    return { icon: '↑', colorClass: 'icon-red', label: 'sent' };
}

/**
 * Builds the human-readable transaction label.
 * Examples: "Sent to: Rahul Sharma", "Received from: Priya Mehta", "Deposit to Wallet"
 */
function getLabel(tx) {
    if (tx.type === 'DEPOSIT') return 'Deposit to Wallet';
    if (tx.direction === 'OUT') {
        const name = tx.receiver?.name || tx.receiver?.email || 'Unknown';
        return `Sent to: ${name}`;
    }
    const name = tx.sender?.name || tx.sender?.email || 'Unknown';
    return `Received from: ${name}`;
}

/**
 * Single transaction row component — used in the Transaction History list.
 */
const TransactionItem = ({ tx }) => {
    const { icon, colorClass } = getIconConfig(tx.direction, tx.status, tx.type);
    const label = getLabel(tx);
    const isPositive = tx.direction === 'IN' || tx.type === 'DEPOSIT';
    const amountStr = `₹${parseFloat(tx.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    const statusClass = `badge badge-${(tx.status || '').toLowerCase()}`;

    return (
        <div className="tx-row">
            {/* Direction icon */}
            <div className={`tx-icon ${colorClass}`}>
                <span>{icon}</span>
            </div>

            {/* Label (Sent to / Received from) */}
            <div className="tx-label-col">
                <span className="tx-label">{label}</span>
            </div>

            {/* Amount */}
            <div className={`tx-amount-col ${isPositive ? 'amount-in' : 'amount-out'}`}>
                {amountStr}
            </div>

            {/* Status badge */}
            <div className="tx-status-col">
                <span className={statusClass}>{tx.status}</span>
            </div>

            {/* Reference ID */}
            <div className="tx-ref-col">
                Ref: {tx.referenceId}
            </div>

            {/* Time */}
            <div className="tx-time-col">
                {formatTime(tx.createdAt)}
            </div>
        </div>
    );
};

export default TransactionItem;
