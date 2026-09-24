import { useCallback, useEffect, useState } from 'react'

import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '../api/notificationsApi.js'
import Modal from '../components/Modal.jsx'
import Icon from '../components/Icon.jsx'
import { useTranslation } from '../i18n/i18n.js'

const PAGE_SIZE = 20

function statusLabel(t, status) {
  const labels = {
    PENDING: 'Pending',
    SENT: 'Sent',
    DELIVERED: 'Delivered',
    READ: 'Read',
    FAILED: 'Failed',
    CANCELLED: 'Cancelled',
  }

  return t(labels[status] || status || 'Unknown')
}

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

function NotificationsPage() {
  const { t } = useTranslation()

  const [notifications, setNotifications] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [unreadCount, setUnreadCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [selectedNotification, setSelectedNotification] = useState(null)
  const [markingId, setMarkingId] = useState(null)

  const loadNotifications = useCallback(async (pageToLoad = 0) => {
    setLoading(true)
    setError('')

    try {
      const response = await getNotifications(pageToLoad, PAGE_SIZE)
      setNotifications(Array.isArray(response?.content) ? response.content : [])
      setPage(response?.page ?? pageToLoad)
      setTotalPages(response?.totalPages ?? 0)
      setTotalElements(response?.totalElements ?? 0)
      setUnreadCount(response?.unreadCount ?? 0)
    } catch (requestError) {
      setError(requestError.message || t('Unable to load notifications.'))
    } finally {
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    loadNotifications(0)
  }, [loadNotifications])

  const openNotification = async (notification) => {
    setSelectedNotification(notification)

    if (notification.status !== 'READ') {
      setMarkingId(notification.id)
      try {
        const updated = await markNotificationRead(notification.id)
        setSelectedNotification(updated)
        setNotifications((current) => current.map((item) => (
          item.id === notification.id ? updated : item
        )))
        setUnreadCount((current) => Math.max(0, current - 1))
      } catch (requestError) {
        setError(requestError.message || t('Unable to mark notification as read.'))
      } finally {
        setMarkingId(null)
      }
    }
  }

  const handleMarkAllRead = async () => {
    if (unreadCount === 0) return

    setError('')
    try {
      await markAllNotificationsRead()
      setNotifications((current) => current.map((notification) => ({
        ...notification,
        status: 'READ',
        readAt: notification.readAt || new Date().toISOString(),
      })))
      setUnreadCount(0)
      setSuccessMessage(t('All notifications marked as read.'))
    } catch (requestError) {
      setError(requestError.message || t('Unable to mark all notifications as read.'))
    }
  }

  return (
    <section className="module-page">
      <div className="module-page-header">
        <div>
          <p className="eyebrow">{t('NOTIFICATIONS')}</p>
          <h2>{t('Notifications')}</h2>
          <p className="muted">
            {t('View in-app communications and notification history for your account.')}
          </p>
        </div>

        <div className="table-actions">
          <span className="status-pill">
            {unreadCount} {t('unread')}
          </span>
          <button
            className="button button-ghost"
            type="button"
            onClick={handleMarkAllRead}
            disabled={unreadCount === 0}
          >
            <Icon name="check" size={16} />
            {t('Mark all read')}
          </button>
        </div>
      </div>

      {successMessage && (
        <div className="feedback feedback-success" role="status">
          {successMessage}
        </div>
      )}

      {error && (
        <div className="feedback feedback-error" role="alert">
          {error}
        </div>
      )}

      <article className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">{t('INBOX')}</p>
            <h3>{totalElements} {t('notifications')}</h3>
          </div>
        </div>

        {loading ? (
          <div className="feedback feedback-info">{t('Loading...')}</div>
        ) : notifications.length === 0 ? (
          <div className="empty-state">{t('No notifications found.')}</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t('Subject')}</th>
                  <th>{t('Status')}</th>
                  <th>{t('Date')}</th>
                  <th>{t('Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {notifications.map((notification) => (
                  <tr key={notification.id} className={notification.status !== 'READ' ? 'table-row-unread' : ''}>
                    <td>
                      <strong>{notification.subject || t('Notification')}</strong>
                      <div className="table-secondary-text">
                        {notification.message}
                      </div>
                    </td>
                    <td>
                      <span className={`status-pill status-${notification.status?.toLowerCase()}`}>
                        {statusLabel(t, notification.status)}
                      </span>
                    </td>
                    <td>{formatDateTime(notification.createdAt)}</td>
                    <td>
                      <button
                        className="button button-ghost button-small"
                        type="button"
                        onClick={() => openNotification(notification)}
                        disabled={markingId === notification.id}
                      >
                        {t('View')}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="button button-secondary button-small"
              type="button"
              onClick={() => loadNotifications(page - 1)}
              disabled={page <= 0 || loading}
            >
              {t('Previous')}
            </button>
            <span>{t('Page')} {page + 1} {t('of')} {totalPages}</span>
            <button
              className="button button-secondary button-small"
              type="button"
              onClick={() => loadNotifications(page + 1)}
              disabled={page + 1 >= totalPages || loading}
            >
              {t('Next')}
            </button>
          </div>
        )}
      </article>

      <Modal
        open={Boolean(selectedNotification)}
        onClose={() => setSelectedNotification(null)}
        eyebrow={t('NOTIFICATION')}
        title={selectedNotification?.subject || t('Notification')}
        size="medium"
      >
        {selectedNotification && (
          <div className="detail-grid">
            <div><span>{t('Status')}</span><strong>{statusLabel(t, selectedNotification.status)}</strong></div>
            <div><span>{t('Channel')}</span><strong>{selectedNotification.channel || '—'}</strong></div>
            <div><span>{t('Date')}</span><strong>{formatDateTime(selectedNotification.createdAt)}</strong></div>
            <div className="detail-grid-wide"><span>{t('Message')}</span><strong>{selectedNotification.message}</strong></div>
          </div>
        )}
      </Modal>
    </section>
  )
}

export default NotificationsPage
