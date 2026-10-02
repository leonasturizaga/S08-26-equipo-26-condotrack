import { useCallback, useEffect, useMemo, useState } from 'react'

import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import TableAction from '../components/TableAction.jsx'
import TableActions from '../components/TableActions.jsx'
import { getUnits } from '../api/unitsApi.js'
import { getResidents } from '../api/residentsApi.js'
import {
  getCommonAreas,
  getCommonAreaAvailabilityBlocks,
} from '../api/commonAreasApi.js'
import {
  createBooking,
  getAllBookings,
  getBooking,
  getBookings,
  updateBooking,
  updateBookingStatus,
} from '../api/bookingsApi.js'

const PAGE_SIZE = 20
const OPTION_PAGE_SIZE = 100

const CALENDAR_START_HOUR = 6
const CALENDAR_END_HOUR = 24
const CALENDAR_SLOT_MINUTES = 30
const CALENDAR_SLOT_HEIGHT = 28

const CALENDAR_SLOTS =
  ((CALENDAR_END_HOUR - CALENDAR_START_HOUR) * 60) / CALENDAR_SLOT_MINUTES

function formatDateTime(value) {
  if (!value) return '—'

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

function formatCalendarDate(value) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) return '—'

  return new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  }).format(date)
}

function formatCalendarTime(value) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) return '—'

  return new Intl.DateTimeFormat(undefined, {
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function toLocalInputValue(value) {
  if (!value) return ''

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) return ''

  const pad = (n) => String(n).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function toIso(value) {
  if (!value) return null

  const date = new Date(value)

  return Number.isNaN(date.getTime()) ? null : date.toISOString()
}

function statusClass(status) {
  switch (status) {
    case 'APPROVED':
      return 'status-success'
    case 'PENDING':
      return 'status-warning'
    case 'REJECTED':
    case 'CANCELLED':
      return 'status-danger'
    case 'COMPLETED':
      return 'status-success'
    case 'NO_SHOW':
      return 'status-danger'
    default:
      return 'status-muted'
  }
}

function createDefaultForm() {
  return {
    buildingId: '',
    commonAreaId: '',
    unitId: '',
    residentId: '',
    startAt: '',
    endAt: '',
    purpose: '',
  }
}

function allowedAdminTransitions(status) {
  if (status === 'PENDING') {
    return ['APPROVED', 'REJECTED', 'CANCELLED']
  }

  if (status === 'APPROVED') {
    return ['COMPLETED', 'CANCELLED', 'NO_SHOW']
  }

  return []
}

function startOfWeek(date) {
  const result = new Date(date)

  result.setHours(0, 0, 0, 0)

  const day = result.getDay()
  const daysFromMonday = day === 0 ? 6 : day - 1

  result.setDate(result.getDate() - daysFromMonday)

  return result
}

function addDays(date, amount) {
  const result = new Date(date)
  result.setDate(result.getDate() + amount)
  return result
}

function sameCalendarDay(first, second) {
  return (
    first.getFullYear() === second.getFullYear() &&
    first.getMonth() === second.getMonth() &&
    first.getDate() === second.getDate()
  )
}

function getWeekKey(date) {
  const start = startOfWeek(date)

  return [
    start.getFullYear(),
    String(start.getMonth() + 1).padStart(2, '0'),
    String(start.getDate()).padStart(2, '0'),
  ].join('-')
}

function getCalendarRange(weekStart) {
  const start = new Date(weekStart)
  start.setHours(CALENDAR_START_HOUR, 0, 0, 0)

  const end = addDays(weekStart, 7)
  end.setHours(0, 0, 0, 0)

  return { start, end }
}

function getEventLabel(event) {
  if (event.type === 'BOOKING') {
    return event.title || 'Booking'
  }

  if (event.blockType === 'REPAIR') {
    return 'Repair'
  }

  return 'Unavailable'
}

function eventTypeClass(event) {
  if (event.type === 'BOOKING') {
    return event.status === 'APPROVED'
      ? 'booking-calendar-event booking-calendar-approved'
      : 'booking-calendar-event booking-calendar-pending'
  }

  if (event.blockType === 'REPAIR') {
    return 'booking-calendar-event booking-calendar-repair'
  }

  return 'booking-calendar-event booking-calendar-unavailable'
}

function getMinutesIntoDay(date) {
  return date.getHours() * 60 + date.getMinutes()
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

function buildCalendarSegments(events, weekStart) {
  const segmentsByDay = Array.from({ length: 7 }, () => [])

  const visibleStart = new Date(weekStart)
  visibleStart.setHours(CALENDAR_START_HOUR, 0, 0, 0)

  const visibleEnd = addDays(weekStart, 7)
  visibleEnd.setHours(0, 0, 0, 0)

  events.forEach((event) => {
    const eventStart = new Date(event.startAt)
    const eventEnd = new Date(event.endAt)

    if (
      Number.isNaN(eventStart.getTime()) ||
      Number.isNaN(eventEnd.getTime()) ||
      eventEnd <= eventStart
    ) {
      return
    }

    if (eventEnd <= visibleStart || eventStart >= visibleEnd) {
      return
    }

    for (let dayIndex = 0; dayIndex < 7; dayIndex += 1) {
      const dayStart = addDays(weekStart, dayIndex)
      dayStart.setHours(CALENDAR_START_HOUR, 0, 0, 0)

      const dayEnd = addDays(weekStart, dayIndex)
      dayEnd.setDate(dayEnd.getDate() + 1)
      dayEnd.setHours(0, 0, 0, 0)

      const segmentStart = new Date(
        Math.max(eventStart.getTime(), dayStart.getTime()),
      )

      const segmentEnd = new Date(
        Math.min(eventEnd.getTime(), dayEnd.getTime()),
      )

      if (segmentEnd <= segmentStart) {
        continue
      }

      const startMinutes = clamp(
        getMinutesIntoDay(segmentStart),
        CALENDAR_START_HOUR * 60,
        CALENDAR_END_HOUR * 60,
      )

      let endMinutes = getMinutesIntoDay(segmentEnd)

      if (sameCalendarDay(segmentEnd, dayStart) === false && segmentEnd.getHours() === 0) {
        endMinutes = CALENDAR_END_HOUR * 60
      }

      endMinutes = clamp(
        endMinutes,
        CALENDAR_START_HOUR * 60,
        CALENDAR_END_HOUR * 60,
      )

      const topMinutes =
        startMinutes - CALENDAR_START_HOUR * 60

      const heightMinutes = Math.max(
        endMinutes - startMinutes,
        CALENDAR_SLOT_MINUTES,
      )

      segmentsByDay[dayIndex].push({
        ...event,
        segmentStart,
        segmentEnd,
        top: (topMinutes / CALENDAR_SLOT_MINUTES) * CALENDAR_SLOT_HEIGHT,
        height:
          (heightMinutes / CALENDAR_SLOT_MINUTES) * CALENDAR_SLOT_HEIGHT,
      })
    }
  })

  segmentsByDay.forEach((segments) => {
    segments.sort(
      (a, b) =>
        a.segmentStart.getTime() - b.segmentStart.getTime() ||
        b.segmentEnd.getTime() - a.segmentEnd.getTime(),
    )

    const laneEnds = []

    segments.forEach((segment) => {
      let lane = 0

      while (
        lane < laneEnds.length &&
        segment.segmentStart.getTime() < laneEnds[lane]
      ) {
        lane += 1
      }

      if (lane === laneEnds.length) {
        laneEnds.push(segment.segmentEnd.getTime())
      } else {
        laneEnds[lane] = segment.segmentEnd.getTime()
      }

      segment.lane = lane
      segment.laneCount = 1
    })

    const laneCount = laneEnds.length

    segments.forEach((segment) => {
      segment.laneCount = laneCount
    })
  })

  return segmentsByDay
}

function BookingsPage() {
  const { t } = useTranslation()
  const { user } = useAuth()

  const role = String(
    user?.roles?.[0] ?? user?.role ?? '',
  )
    .trim()
    .toUpperCase()

  const canCreate =
    role === 'ADMINISTRATOR' || role === 'RESIDENT'

  const canOperate = role === 'ADMINISTRATOR'

  const canView = [
    'ADMINISTRATOR',
    'RECEPTION',
    'RESIDENT',
  ].includes(role)

  const [commonAreas, setCommonAreas] = useState([])
  const [bookings, setBookings] = useState([])

  const [calendarBookings, setCalendarBookings] = useState([])
  const [calendarBlocks, setCalendarBlocks] = useState([])

  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [commonAreaLoading, setCommonAreaLoading] = useState(true)
  const [commonAreaError, setCommonAreaError] = useState('')

  const [calendarLoading, setCalendarLoading] = useState(false)
  const [calendarError, setCalendarError] = useState('')

  const [units, setUnits] = useState([])
  const [residents, setResidents] = useState([])
  const [optionsLoading, setOptionsLoading] = useState(false)
  const [optionsError, setOptionsError] = useState('')

  const [showCreate, setShowCreate] = useState(false)
  const [showEdit, setShowEdit] = useState(false)

  const [form, setForm] = useState(createDefaultForm)

  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const [selectedBooking, setSelectedBooking] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const [actionLoadingId, setActionLoadingId] = useState('')

  const [selectedCalendarAreaId, setSelectedCalendarAreaId] =
    useState('')

  const [calendarWeekStart, setCalendarWeekStart] =
    useState(() => startOfWeek(new Date()))

  const [selectedCalendarBlock, setSelectedCalendarBlock] =
    useState(null)

  const loadBookings = useCallback(
    async (targetPage = page) => {
      if (!canView) {
        setLoading(false)
        return
      }

      setLoading(true)
      setError('')

      try {
        const response = await getBookings(
          targetPage,
          PAGE_SIZE,
        )

        setBookings(
          Array.isArray(response?.content)
            ? response.content
            : [],
        )

        setPage(response?.page ?? targetPage)
        setTotalPages(response?.totalPages ?? 0)
        setTotalElements(response?.totalElements ?? 0)
      } catch (requestError) {
        setError(
          requestError.message ||
            t('Unable to load bookings.'),
        )

        setBookings([])
      } finally {
        setLoading(false)
      }
    },
    [canView, page, t],
  )

  const loadCommonAreas = useCallback(async () => {
    if (!canView) {
      setCommonAreaLoading(false)
      return
    }

    setCommonAreaLoading(true)
    setCommonAreaError('')

    try {
      const response = await getCommonAreas(
        '',
        0,
        100,
      )

      const areas = Array.isArray(response?.content)
        ? response.content
        : []

      setCommonAreas(areas)

      setSelectedCalendarAreaId((current) => {
        if (
          current &&
          areas.some((area) => area.id === current)
        ) {
          return current
        }

        return areas[0]?.id || ''
      })
    } catch (requestError) {
      setCommonAreaError(
        requestError.message ||
          t('Unable to load common areas.'),
      )

      setCommonAreas([])
      setSelectedCalendarAreaId('')
    } finally {
      setCommonAreaLoading(false)
    }
  }, [canView, t])

  const loadOptions = useCallback(async () => {
    if (!canCreate) return

    setOptionsLoading(true)
    setOptionsError('')

    try {
      const [
        unitsResponse,
        residentsResponse,
      ] = await Promise.all([
        getUnits(0, OPTION_PAGE_SIZE),
        getResidents(0, OPTION_PAGE_SIZE),
      ])

      setUnits(
        Array.isArray(unitsResponse?.content)
          ? unitsResponse.content.filter(
              (item) => item.active !== false,
            )
          : [],
      )

      setResidents(
        Array.isArray(residentsResponse?.content)
          ? residentsResponse.content.filter(
              (item) =>
                item.active !== false &&
                item.unitId,
            )
          : [],
      )
    } catch (requestError) {
      setOptionsError(
        requestError.message ||
          t('Unable to load booking options.'),
      )
    } finally {
      setOptionsLoading(false)
    }
  }, [canCreate, t])

  const loadCalendarData = useCallback(async () => {
    if (!canView || !selectedCalendarAreaId) {
      setCalendarBookings([])
      setCalendarBlocks([])
      return
    }

    setCalendarLoading(true)
    setCalendarError('')

    try {
      const [
        allBookings,
        blocks,
      ] = await Promise.all([
        getAllBookings(100),
        getCommonAreaAvailabilityBlocks(
          selectedCalendarAreaId,
        ),
      ])

      setCalendarBookings(
        Array.isArray(allBookings)
          ? allBookings.filter(
              (booking) =>
                booking.commonAreaId ===
                  selectedCalendarAreaId &&
                ['PENDING', 'APPROVED'].includes(
                  booking.status,
                ),
            )
          : [],
      )

      setCalendarBlocks(
        Array.isArray(blocks)
          ? blocks.filter(
              (block) => block.active !== false,
            )
          : [],
      )
    } catch (requestError) {
      setCalendarError(
        requestError.message ||
          t('Unable to load calendar data.'),
      )

      setCalendarBookings([])
      setCalendarBlocks([])
    } finally {
      setCalendarLoading(false)
    }
  }, [
    canView,
    selectedCalendarAreaId,
    t,
  ])

  useEffect(() => {
    loadBookings(page)
  }, [loadBookings])

  useEffect(() => {
    loadCommonAreas()
  }, [loadCommonAreas])

  useEffect(() => {
    loadOptions()
  }, [loadOptions])

  useEffect(() => {
    loadCalendarData()
  }, [loadCalendarData])

  const buildingOptions = useMemo(() => {
    const seen = new Set()

    return commonAreas
      .filter(
        (area) =>
          area.buildingId &&
          area.buildingCode,
      )
      .filter((area) => {
        if (seen.has(area.buildingId)) {
          return false
        }

        seen.add(area.buildingId)

        return true
      })
      .sort((a, b) =>
        String(a.buildingCode).localeCompare(
          String(b.buildingCode),
        ),
      )
  }, [commonAreas])

  const filteredCommonAreas = useMemo(
    () =>
      commonAreas.filter(
        (area) =>
          !form.buildingId ||
          area.buildingId === form.buildingId,
      ),
    [commonAreas, form.buildingId],
  )

  const filteredUnits = useMemo(
    () =>
      units.filter(
        (unit) =>
          !form.buildingId ||
          unit.buildingId === form.buildingId,
      ),
    [form.buildingId, units],
  )

  const filteredResidents = useMemo(
    () =>
      residents.filter(
        (resident) =>
          !form.unitId ||
          resident.unitId === form.unitId,
      ),
    [form.unitId, residents],
  )

  const selectedCalendarArea = useMemo(
    () =>
      commonAreas.find(
        (area) =>
          area.id === selectedCalendarAreaId,
      ) || null,
    [commonAreas, selectedCalendarAreaId],
  )

  const calendarEvents = useMemo(() => {
    const bookingEvents = calendarBookings.map(
      (booking) => ({
        id: booking.id,
        type: 'BOOKING',
        status: booking.status,
        startAt: booking.startAt,
        endAt: booking.endAt,
        title:
          booking.purpose ||
          booking.residentName ||
          t('Booking'),
        subtitle:
          booking.unitNumber ||
          booking.residentName ||
          '',
        booking,
      }),
    )

    const blockEvents = calendarBlocks.map(
      (block) => ({
        id: block.id,
        type: 'BLOCK',
        blockType: block.blockType,
        startAt: block.startAt,
        endAt: block.endAt,
        title: getEventLabel({
          type: 'BLOCK',
          blockType: block.blockType,
        }),
        subtitle: block.notes || '',
        block,
      }),
    )

    return [
      ...bookingEvents,
      ...blockEvents,
    ]
  }, [
    calendarBlocks,
    calendarBookings,
    t,
  ])

  const calendarSegments = useMemo(
    () =>
      buildCalendarSegments(
        calendarEvents,
        calendarWeekStart,
      ),
    [calendarEvents, calendarWeekStart],
  )

  const calendarDays = useMemo(
    () =>
      Array.from(
        { length: 7 },
        (_, index) =>
          addDays(calendarWeekStart, index),
      ),
    [calendarWeekStart],
  )

  const calendarGridHeight =
    CALENDAR_SLOTS * CALENDAR_SLOT_HEIGHT

  const calendarWeekLabel = useMemo(() => {
    const first = calendarDays[0]
    const last = calendarDays[6]

    if (!first || !last) return ''

    const firstLabel =
      new Intl.DateTimeFormat(undefined, {
        day: 'numeric',
        month: 'short',
      }).format(first)

    const lastLabel =
      new Intl.DateTimeFormat(undefined, {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      }).format(last)

    return `${firstLabel} – ${lastLabel}`
  }, [calendarDays])

  const resetBookingForm = () => {
    setForm(createDefaultForm())
    setFormError('')
  }

  const closeCreate = () => {
    if (!saving) {
      setShowCreate(false)
      resetBookingForm()
    }
  }

  const openCreate = () => {
    let ownBuildingId = ''
    let ownUnitId = ''
    let ownResidentId = ''

    if (role === 'RESIDENT') {
      const ownUnits = units

      const uniqueBuildingIds = [
        ...new Set(
          ownUnits
            .map((unit) => unit.buildingId)
            .filter(Boolean),
        ),
      ]

      ownBuildingId =
        uniqueBuildingIds.length === 1
          ? uniqueBuildingIds[0]
          : ''

      ownUnitId =
        ownUnits.length === 1
          ? ownUnits[0].id
          : ''

      const ownResidentOptions =
        residents.filter(
          (resident) =>
            !ownUnitId ||
            resident.unitId === ownUnitId,
        )

      ownResidentId =
        ownResidentOptions.length === 1
          ? ownResidentOptions[0].id
          : ''
    }

    setForm({
      ...createDefaultForm(),
      buildingId: ownBuildingId,
      unitId: ownUnitId,
      residentId: ownResidentId,
    })

    setFormError('')
    setSuccessMessage('')
    setShowCreate(true)
  }

  const handleChange = (event) => {
    const { name, value } = event.target

    if (name === 'buildingId') {
      setForm((current) => ({
        ...current,
        buildingId: value,
        commonAreaId: '',
        unitId: '',
        residentId: '',
      }))

      return
    }

    if (name === 'commonAreaId') {
      const selected =
        commonAreas.find(
          (area) => area.id === value,
        )

      setForm((current) => ({
        ...current,
        commonAreaId: value,
        buildingId:
          selected?.buildingId ||
          current.buildingId,
      }))

      return
    }

    if (name === 'unitId') {
      const selected =
        units.find(
          (unit) => unit.id === value,
        )

      setForm((current) => ({
        ...current,
        unitId: value,
        residentId: '',
        buildingId:
          selected?.buildingId ||
          current.buildingId,
      }))

      return
    }

    setForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  const handleCreate = async (event) => {
    event.preventDefault()

    setSaving(true)
    setFormError('')

    try {
      if (
        !form.buildingId ||
        !form.commonAreaId ||
        !form.unitId ||
        !form.residentId ||
        !form.startAt ||
        !form.endAt
      ) {
        throw new Error(
          t(
            'Please complete all required booking fields.',
          ),
        )
      }

      await createBooking({
        buildingId: form.buildingId,
        commonAreaId: form.commonAreaId,
        unitId: form.unitId,
        residentId: form.residentId,
        startAt: toIso(form.startAt),
        endAt: toIso(form.endAt),
        purpose:
          form.purpose.trim() || null,
      })

      setShowCreate(false)
      resetBookingForm()

      setSuccessMessage(
        t('Booking created successfully.'),
      )

      await Promise.all([
        loadBookings(0),
        loadCalendarData(),
      ])
    } catch (requestError) {
      setFormError(
        requestError.message ||
          t('Unable to create booking.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const openBooking = async (bookingId) => {
    setSelectedBooking(null)
    setDetailLoading(true)
    setDetailError('')

    try {
      const response =
        await getBooking(bookingId)

      if (
        !response ||
        typeof response !== 'object'
      ) {
        throw new Error(
          t(
            'The booking details could not be loaded.',
          ),
        )
      }

      setSelectedBooking(response)
    } catch (requestError) {
      setDetailError(
        requestError.message ||
          t('Unable to load booking details.'),
      )
    } finally {
      setDetailLoading(false)
    }
  }

  const openEdit = () => {
    if (!selectedBooking) return

    setForm({
      buildingId:
        selectedBooking.buildingId || '',
      commonAreaId:
        selectedBooking.commonAreaId || '',
      unitId:
        selectedBooking.unitId || '',
      residentId:
        selectedBooking.residentId || '',
      startAt: toLocalInputValue(
        selectedBooking.startAt,
      ),
      endAt: toLocalInputValue(
        selectedBooking.endAt,
      ),
      purpose:
        selectedBooking.purpose || '',
    })

    setFormError('')
    setShowEdit(true)
  }

  const closeEdit = () => {
    if (!saving) {
      setShowEdit(false)
      setFormError('')
    }
  }

  const handleEdit = async (event) => {
    event.preventDefault()

    if (!selectedBooking) return

    setSaving(true)
    setFormError('')

    try {
      if (!form.startAt || !form.endAt) {
        throw new Error(
          t(
            'Please complete all required booking fields.',
          ),
        )
      }

      const response =
        await updateBooking(
          selectedBooking.id,
          {
            startAt: toIso(form.startAt),
            endAt: toIso(form.endAt),
            purpose:
              form.purpose.trim() || null,
          },
        )

      setSelectedBooking(response)
      setShowEdit(false)

      setSuccessMessage(
        t('Booking updated successfully.'),
      )

      await Promise.all([
        loadBookings(page),
        loadCalendarData(),
      ])
    } catch (requestError) {
      setFormError(
        requestError.message ||
          t('Unable to update booking.'),
      )
    } finally {
      setSaving(false)
    }
  }

  const handleStatus = async (
    booking,
    status,
  ) => {
    setActionLoadingId(booking.id)
    setError('')

    try {
      const response =
        await updateBookingStatus(
          booking.id,
          { status },
        )

      setSelectedBooking(response)

      setSuccessMessage(
        t(
          'Booking status updated successfully.',
        ),
      )

      await Promise.all([
        loadBookings(page),
        loadCalendarData(),
      ])
    } catch (requestError) {
      setError(
        requestError.message ||
          t('Unable to update booking status.'),
      )
    } finally {
      setActionLoadingId('')
    }
  }

  const moveCalendarWeek = (amount) => {
    setCalendarWeekStart(
      (current) => addDays(current, amount * 7),
    )
  }

  const goToCurrentWeek = () => {
    setCalendarWeekStart(
      startOfWeek(new Date()),
    )
  }

  const handleCalendarEventClick = (
    event,
  ) => {
    if (event.type === 'BOOKING') {
      openBooking(event.booking.id)
      return
    }

    setSelectedCalendarBlock(event.block)
  }

  const ownCancellationAllowed =
    role === 'RESIDENT' &&
    ['PENDING', 'APPROVED'].includes(
      selectedBooking?.status,
    )

  const ownEditAllowed =
    role === 'RESIDENT' &&
    selectedBooking?.status === 'PENDING'

  const adminActions = selectedBooking
    ? allowedAdminTransitions(
        selectedBooking.status,
      )
    : []

  if (!canView) {
    return (
      <div className="empty-state">
        {t(
          'You do not have access to Common Area Bookings.',
        )}
      </div>
    )
  }

  return (
    <div className="module-page">
      <section className="module-page-header">
        <div>
          <p className="eyebrow">
            {t('AMENITIES')}
          </p>

          <h2>
            {t('Common Area Bookings')}
          </h2>

          <p className="muted">
            {t(
              'View common areas and manage booking requests within your role.',
            )}
          </p>
        </div>

        {canCreate && (
          <button
            className="button button-primary"
            type="button"
            onClick={openCreate}
          >
            {t('New booking')}
          </button>
        )}
      </section>

      {successMessage && (
        <div
          className="feedback feedback-success"
          role="status"
        >
          {successMessage}
        </div>
      )}

      {error && (
        <div
          className="feedback feedback-error"
          role="alert"
        >
          {error}
        </div>
      )}

      <section className="dashboard-grid">
        <article className="panel panel-wide">
          <div className="panel-header">
            <div>
              <p className="eyebrow">
                {t('COMMON AREAS')}
              </p>

              <h3>
                {t('Available amenities')}
              </h3>
            </div>

            <span className="status-pill">
              {commonAreas.length}{' '}
              {t('active areas')}
            </span>
          </div>

          {commonAreaLoading ? (
            <div className="feedback feedback-info">
              {t('Loading common areas...')}
            </div>
          ) : commonAreaError ? (
            <div className="feedback feedback-error">
              {commonAreaError}
            </div>
          ) : commonAreas.length === 0 ? (
            <div className="empty-state">
              {t('No common areas found.')}
            </div>
          ) : (
            <div className="module-link-grid">
              {commonAreas.map((area) => (
                <div
                  key={area.id}
                  className="module-link"
                >
                  <span className="module-link-icon">
                    <span aria-hidden="true">
                      ◆
                    </span>
                  </span>

                  <span className="module-link-content">
                    <strong>
                      {area.name}
                    </strong>

                    <small>
                      {t(area.areaType)} ·{' '}
                      {area.capacity ?? '—'}{' '}
                      {t('capacity')}
                    </small>
                  </span>

                  <span className="module-link-arrow">
                    {area.bookingDurationMinutes
                      ? `${area.bookingDurationMinutes} min`
                      : ''}
                  </span>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>

      {/* =====================================================
          M24.4 - WEEKLY CALENDAR
      ===================================================== */}

      <section className="panel booking-calendar-panel">
        <div className="panel-header booking-calendar-header">
          <div>
            <p className="eyebrow">
              {t('BOOKING CALENDAR')}
            </p>

            <h3>
              {t('Weekly availability')}
            </h3>

            <p className="muted booking-calendar-description">
              {t(
                'View pending and approved bookings together with temporary availability blocks.',
              )}
            </p>
          </div>

          <div className="booking-calendar-toolbar">
            <label className="booking-calendar-area-select">
              <span>
                {t('Common area')}
              </span>

              <select
                value={selectedCalendarAreaId}
                onChange={(event) =>
                  setSelectedCalendarAreaId(
                    event.target.value,
                  )
                }
                disabled={
                  commonAreaLoading ||
                  commonAreas.length === 0
                }
              >
                <option value="">
                  {t('Select common area')}
                </option>

                {commonAreas.map((area) => (
                  <option
                    key={area.id}
                    value={area.id}
                  >
                    {area.name}
                  </option>
                ))}
              </select>
            </label>

            <div className="booking-calendar-navigation">
              <button
                className="button button-secondary button-small"
                type="button"
                onClick={() =>
                  moveCalendarWeek(-1)
                }
              >
                {t('Previous')}
              </button>

              <button
                className="button button-secondary button-small"
                type="button"
                onClick={goToCurrentWeek}
              >
                {t('Current week')}
              </button>

              <button
                className="button button-secondary button-small"
                type="button"
                onClick={() =>
                  moveCalendarWeek(1)
                }
              >
                {t('Next')}
              </button>
            </div>
          </div>
        </div>

        <div className="booking-calendar-week-label">
          <strong>
            {calendarWeekLabel}
          </strong>

          {selectedCalendarArea && (
            <span>
              {selectedCalendarArea.name}
            </span>
          )}
        </div>

        <div className="booking-calendar-legend">
          <span>
            <i className="booking-calendar-legend-dot booking-calendar-legend-approved" />
            {t('Approved')}
          </span>

          <span>
            <i className="booking-calendar-legend-dot booking-calendar-legend-pending" />
            {t('Pending')}
          </span>

          <span>
            <i className="booking-calendar-legend-dot booking-calendar-legend-repair" />
            {t('Repair')}
          </span>

          <span>
            <i className="booking-calendar-legend-dot booking-calendar-legend-unavailable" />
            {t('Unavailable')}
          </span>
        </div>

        {calendarError && (
          <div className="feedback feedback-error">
            {calendarError}
          </div>
        )}

        {calendarLoading ? (
          <div className="feedback feedback-info">
            {t('Loading calendar...')}
          </div>
        ) : !selectedCalendarAreaId ? (
          <div className="empty-state">
            {t(
              'Select a common area to view its weekly availability.',
            )}
          </div>
        ) : (
          <div className="booking-calendar-scroll">
            <div
              className="booking-calendar"
              style={{
                '--booking-calendar-height': `${calendarGridHeight}px`,
              }}
            >
              <div className="booking-calendar-corner" />

              <div className="booking-calendar-day-headers">
                {calendarDays.map(
                  (day, index) => (
                    <div
                      key={day.toISOString()}
                      className="booking-calendar-day-header"
                    >
                      <span>
                        {new Intl.DateTimeFormat(
                          undefined,
                          {
                            weekday: 'short',
                          },
                        ).format(day)}
                      </span>

                      <strong>
                        {new Intl.DateTimeFormat(
                          undefined,
                          {
                            day: 'numeric',
                            month: 'short',
                          },
                        ).format(day)}
                      </strong>
                    </div>
                  ),
                )}
              </div>

              <div className="booking-calendar-time-column">
                {Array.from(
                  {
                    length:
                      CALENDAR_END_HOUR -
                      CALENDAR_START_HOUR,
                  },
                  (_, index) => {
                    const hour =
                      CALENDAR_START_HOUR +
                      index

                    return (
                      <div
                        key={hour}
                        className="booking-calendar-time-label"
                        style={{
                          height: `${CALENDAR_SLOT_HEIGHT * 2}px`,
                        }}
                      >
                        {`${String(hour).padStart(
                          2,
                          '0',
                        )}:00`}
                      </div>
                    )
                  },
                )}
              </div>

              <div
                className="booking-calendar-grid"
                style={{
                  height: `${calendarGridHeight}px`,
                }}
              >
                {calendarDays.map(
                  (day, dayIndex) => (
                    <div
                      key={day.toISOString()}
                      className="booking-calendar-day-column"
                      style={{
                        height: `${calendarGridHeight}px`,
                      }}
                    >
                      <div className="booking-calendar-hour-lines">
                        {Array.from(
                          {
                            length:
                              CALENDAR_SLOTS,
                          },
                          (_, slotIndex) => (
                            <div
                              key={slotIndex}
                              className={
                                slotIndex % 2 === 0
                                  ? 'booking-calendar-slot booking-calendar-slot-hour'
                                  : 'booking-calendar-slot'
                              }
                              style={{
                                height: `${CALENDAR_SLOT_HEIGHT}px`,
                              }}
                            />
                          ),
                        )}
                      </div>

                      {calendarSegments[
                        dayIndex
                      ].map((event) => {
                        const width =
                          100 /
                          event.laneCount

                        const left =
                          event.lane *
                          width

                        return (
                          <button
                            key={`${event.type}-${event.id}-${dayIndex}`}
                            type="button"
                            className={eventTypeClass(
                              event,
                            )}
                            style={{
                              top: `${event.top}px`,
                              height: `${Math.max(
                                event.height,
                                CALENDAR_SLOT_HEIGHT,
                              )}px`,
                              width: `calc(${width}% - 6px)`,
                              left: `calc(${left}% + 3px)`,
                            }}
                            onClick={() =>
                              handleCalendarEventClick(
                                event,
                              )
                            }
                            title={`${getEventLabel(event)}: ${formatCalendarTime(event.segmentStart)} – ${formatCalendarTime(event.segmentEnd)}`}
                          >
                            <strong>
                              {getEventLabel(event)}
                            </strong>

                            <span>
                              {formatCalendarTime(
                                event.segmentStart,
                              )}{' '}
                              –{' '}
                              {formatCalendarTime(
                                event.segmentEnd,
                              )}
                            </span>

                            {event.subtitle && (
                              <small>
                                {event.subtitle}
                              </small>
                            )}
                          </button>
                        )
                      })}
                    </div>
                  ),
                )}
              </div>
            </div>
          </div>
        )}
      </section>

      {/* =====================================================
          EXISTING BOOKING TABLE
      ===================================================== */}

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">
              {t('BOOKINGS')}
            </p>

            <h3>
              {t('Booking requests')}
            </h3>
          </div>

          <span className="status-pill">
            {totalElements} {t('total')}
          </span>
        </div>

        {loading ? (
          <div className="feedback feedback-info">
            {t('Loading bookings...')}
          </div>
        ) : bookings.length === 0 ? (
          <div className="empty-state">
            {t('No bookings found.')}
          </div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>
                    {t('Common area')}
                  </th>

                  <th>{t('Unit')}</th>

                  <th>{t('Resident')}</th>

                  <th>{t('Start')}</th>

                  <th>{t('End')}</th>

                  <th>{t('Status')}</th>

                  <th>{t('Actions')}</th>
                </tr>
              </thead>

              <tbody>
                {bookings.map(
                  (booking) => (
                    <tr key={booking.id}>
                      <td>
                        {booking.commonAreaName}
                      </td>

                      <td>
                        {booking.unitNumber}
                      </td>

                      <td>
                        {booking.residentName ||
                          '—'}
                      </td>

                      <td>
                        {formatDateTime(
                          booking.startAt,
                        )}
                      </td>

                      <td>
                        {formatDateTime(
                          booking.endAt,
                        )}
                      </td>

                      <td>
                        <span
                          className={`status-pill ${statusClass(
                            booking.status,
                          )}`}
                        >
                          {t(booking.status)}
                        </span>
                      </td>

                      <td>
                        <TableActions
                          moreLabel={t('More')}
                        >
                          <TableAction
                            icon="eye"
                            label={t('View')}
                            variant="view"
                            onClick={() =>
                              openBooking(
                                booking.id,
                              )
                            }
                          />

                          {canOperate &&
                            allowedAdminTransitions(
                              booking.status,
                            )
                              .slice(0, 1)
                              .map(
                                (status) => (
                                  <TableAction
                                    key={status}
                                    icon="check"
                                    label={t(
                                      status,
                                    )}
                                    variant={
                                      status ===
                                        'REJECTED' ||
                                      status ===
                                        'CANCELLED'
                                        ? 'delete'
                                        : 'edit'
                                    }
                                    disabled={
                                      actionLoadingId ===
                                      booking.id
                                    }
                                    onClick={() =>
                                      handleStatus(
                                        booking,
                                        status,
                                      )
                                    }
                                  />
                                ),
                              )}
                        </TableActions>
                      </td>
                    </tr>
                  ),
                )}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="button button-secondary button-small"
              type="button"
              disabled={page <= 0}
              onClick={() =>
                loadBookings(page - 1)
              }
            >
              {t('Previous')}
            </button>

            <span>
              {t('Page')} {page + 1}{' '}
              {t('of')} {totalPages}
            </span>

            <button
              className="button button-secondary button-small"
              type="button"
              disabled={
                page >= totalPages - 1
              }
              onClick={() =>
                loadBookings(page + 1)
              }
            >
              {t('Next')}
            </button>
          </div>
        )}
      </section>

      {/* =====================================================
          CREATE BOOKING MODAL
      ===================================================== */}

      <Modal
        open={showCreate}
        title={t('New booking')}
        onClose={closeCreate}
        size="medium"
        closeOnBackdrop={!saving}
      >
        <form
          className="form-grid"
          onSubmit={handleCreate}
        >
          {optionsError && (
            <div className="feedback feedback-error form-grid-full">
              {optionsError}
            </div>
          )}

          <label className="form-field">
            <span>{t('Building')}</span>

            <select
              name="buildingId"
              value={form.buildingId}
              onChange={handleChange}
              disabled={
                optionsLoading ||
                (role === 'RESIDENT' &&
                  Boolean(form.buildingId))
              }
              required
            >
              <option value="">
                {t('Select building')}
              </option>

              {buildingOptions.map(
                (building) => (
                  <option
                    key={building.buildingId}
                    value={building.buildingId}
                  >
                    {building.buildingCode}
                  </option>
                ),
              )}
            </select>
          </label>

          <label className="form-field">
            <span>
              {t('Common area')}
            </span>

            <select
              name="commonAreaId"
              value={form.commonAreaId}
              onChange={handleChange}
              disabled={
                optionsLoading ||
                commonAreaLoading ||
                !form.buildingId
              }
              required
            >
              <option value="">
                {t('Select common area')}
              </option>

              {filteredCommonAreas.map(
                (area) => (
                  <option
                    key={area.id}
                    value={area.id}
                  >
                    {area.name}
                  </option>
                ),
              )}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Unit')}</span>

            <select
              name="unitId"
              value={form.unitId}
              onChange={handleChange}
              disabled={
                optionsLoading ||
                (role === 'RESIDENT' &&
                  filteredUnits.length <= 1) ||
                !form.buildingId
              }
              required
            >
              <option value="">
                {t('Select unit')}
              </option>

              {filteredUnits.map(
                (unit) => (
                  <option
                    key={unit.id}
                    value={unit.id}
                  >
                    {unit.unitNumber}
                  </option>
                ),
              )}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Resident')}</span>

            <select
              name="residentId"
              value={form.residentId}
              onChange={handleChange}
              disabled={
                optionsLoading ||
                (role === 'RESIDENT' &&
                  filteredResidents.length <=
                    1) ||
                !form.unitId
              }
              required
            >
              <option value="">
                {t('Select resident')}
              </option>

              {filteredResidents.map(
                (resident) => (
                  <option
                    key={resident.id}
                    value={resident.id}
                  >
                    {resident.firstName}{' '}
                    {resident.lastName}
                  </option>
                ),
              )}
            </select>
          </label>

          <label className="form-field">
            <span>{t('Start')}</span>

            <input
              type="datetime-local"
              name="startAt"
              value={form.startAt}
              onChange={handleChange}
              required
            />
          </label>

          <label className="form-field">
            <span>{t('End')}</span>

            <input
              type="datetime-local"
              name="endAt"
              value={form.endAt}
              onChange={handleChange}
              required
            />
          </label>

          <label className="form-field form-grid-full">
            <span>{t('Purpose')}</span>

            <input
              name="purpose"
              value={form.purpose}
              onChange={handleChange}
              maxLength={255}
            />
          </label>

          {formError && (
            <div
              className="feedback feedback-error form-grid-full"
              role="alert"
            >
              {formError}
            </div>
          )}

          <div className="modal-actions form-grid-full">
            <button
              className="button button-secondary"
              type="button"
              disabled={saving}
              onClick={closeCreate}
            >
              {t('Cancel')}
            </button>

            <button
              className="button button-primary"
              type="submit"
              disabled={
                saving || optionsLoading
              }
            >
              {saving
                ? t('Saving...')
                : t('Create booking')}
            </button>
          </div>
        </form>
      </Modal>

      {/* =====================================================
          EDIT BOOKING MODAL
      ===================================================== */}

      <Modal
        open={showEdit}
        title={t('Edit booking')}
        onClose={closeEdit}
        size="medium"
        closeOnBackdrop={!saving}
      >
        <form
          className="form-grid"
          onSubmit={handleEdit}
        >
          <div className="booking-edit-context form-grid-full">
            <div>
              <span>{t('Building')}</span>
              <strong>
                {selectedBooking?.buildingCode ||
                  '—'}
              </strong>
            </div>

            <div>
              <span>
                {t('Common area')}
              </span>
              <strong>
                {selectedBooking?.commonAreaName ||
                  '—'}
              </strong>
            </div>

            <div>
              <span>{t('Unit')}</span>
              <strong>
                {selectedBooking?.unitNumber ||
                  '—'}
              </strong>
            </div>
          </div>

          <label className="form-field">
            <span>{t('Start')}</span>

            <input
              type="datetime-local"
              name="startAt"
              value={form.startAt}
              onChange={handleChange}
              required
            />
          </label>

          <label className="form-field">
            <span>{t('End')}</span>

            <input
              type="datetime-local"
              name="endAt"
              value={form.endAt}
              onChange={handleChange}
              required
            />
          </label>

          <label className="form-field form-grid-full">
            <span>{t('Purpose')}</span>

            <input
              name="purpose"
              value={form.purpose}
              onChange={handleChange}
              maxLength={255}
            />
          </label>

          {formError && (
            <div
              className="feedback feedback-error form-grid-full"
              role="alert"
            >
              {formError}
            </div>
          )}

          <div className="modal-actions form-grid-full">
            <button
              className="button button-secondary"
              type="button"
              disabled={saving}
              onClick={closeEdit}
            >
              {t('Cancel')}
            </button>

            <button
              className="button button-primary"
              type="submit"
              disabled={saving}
            >
              {saving
                ? t('Saving...')
                : t('Save changes')}
            </button>
          </div>
        </form>
      </Modal>

      {/* =====================================================
          BOOKING DETAILS MODAL
      ===================================================== */}

      <Modal
        open={
          detailLoading ||
          Boolean(selectedBooking)
        }
        title={t('Booking details')}
        onClose={() =>
          !detailLoading &&
          setSelectedBooking(null)
        }
        size="medium"
      >
        {detailLoading ? (
          <div className="feedback feedback-info">
            {t('Loading booking details...')}
          </div>
        ) : selectedBooking ? (
          <div className="detail-grid">
            <div>
              <span>
                {t('Common area')}
              </span>

              <strong>
                {selectedBooking.commonAreaName}
              </strong>
            </div>

            <div>
              <span>{t('Building')}</span>

              <strong>
                {selectedBooking.buildingCode ||
                  '—'}
              </strong>
            </div>

            <div>
              <span>{t('Unit')}</span>

              <strong>
                {selectedBooking.unitNumber}
              </strong>
            </div>

            <div>
              <span>{t('Resident')}</span>

              <strong>
                {selectedBooking.residentName ||
                  '—'}
              </strong>
            </div>

            <div>
              <span>{t('Status')}</span>

              <strong
                className={`status-pill ${statusClass(
                  selectedBooking.status,
                )}`}
              >
                {t(selectedBooking.status)}
              </strong>
            </div>

            <div>
              <span>{t('Start')}</span>

              <strong>
                {formatDateTime(
                  selectedBooking.startAt,
                )}
              </strong>
            </div>

            <div>
              <span>{t('End')}</span>

              <strong>
                {formatDateTime(
                  selectedBooking.endAt,
                )}
              </strong>
            </div>

            <div>
              <span>{t('Purpose')}</span>

              <strong>
                {selectedBooking.purpose ||
                  '—'}
              </strong>
            </div>

            {selectedBooking.cancellationReason && (
              <div className="field-span-2">
                <span>
                  {t('Cancellation reason')}
                </span>

                <strong>
                  {
                    selectedBooking.cancellationReason
                  }
                </strong>
              </div>
            )}
          </div>
        ) : (
          <div className="feedback feedback-error">
            {detailError}
          </div>
        )}

        {selectedBooking &&
          (canOperate ||
            ownEditAllowed ||
            ownCancellationAllowed) && (
            <div className="modal-actions">
              {ownEditAllowed && (
                <button
                  className="button button-secondary"
                  type="button"
                  disabled={
                    actionLoadingId ===
                    selectedBooking.id
                  }
                  onClick={openEdit}
                >
                  {t('Edit booking')}
                </button>
              )}

              {canOperate &&
                adminActions.map(
                  (status) => (
                    <button
                      key={status}
                      className={`button ${
                        status ===
                          'CANCELLED' ||
                        status === 'REJECTED'
                          ? 'button-danger'
                          : 'button-primary'
                      }`}
                      type="button"
                      disabled={
                        actionLoadingId ===
                        selectedBooking.id
                      }
                      onClick={() =>
                        handleStatus(
                          selectedBooking,
                          status,
                        )
                      }
                    >
                      {t(status)}
                    </button>
                  ),
                )}

              {ownCancellationAllowed && (
                <button
                  className="button button-danger"
                  type="button"
                  disabled={
                    actionLoadingId ===
                    selectedBooking.id
                  }
                  onClick={() =>
                    handleStatus(
                      selectedBooking,
                      'CANCELLED',
                    )
                  }
                >
                  {t('Cancel booking')}
                </button>
              )}
            </div>
          )}
      </Modal>

      {/* =====================================================
          AVAILABILITY BLOCK DETAILS
      ===================================================== */}

      <Modal
        open={Boolean(selectedCalendarBlock)}
        title={t('Availability block')}
        onClose={() =>
          setSelectedCalendarBlock(null)
        }
        size="medium"
      >
        {selectedCalendarBlock && (
          <>
            <div className="detail-grid">
              <div>
                <span>{t('Type')}</span>

                <strong>
                  {t(
                    selectedCalendarBlock.blockType,
                  )}
                </strong>
              </div>

              <div>
                <span>{t('Status')}</span>

                <strong>
                  {selectedCalendarBlock.active !==
                  false
                    ? t('Active')
                    : t('Inactive')}
                </strong>
              </div>

              <div>
                <span>{t('Start')}</span>

                <strong>
                  {formatDateTime(
                    selectedCalendarBlock.startAt,
                  )}
                </strong>
              </div>

              <div>
                <span>{t('End')}</span>

                <strong>
                  {formatDateTime(
                    selectedCalendarBlock.endAt,
                  )}
                </strong>
              </div>

              <div className="field-span-2">
                <span>{t('Notes')}</span>

                <strong>
                  {selectedCalendarBlock.notes ||
                    '—'}
                </strong>
              </div>
            </div>

            <div className="modal-actions">
              <button
                className="button button-secondary"
                type="button"
                onClick={() =>
                  setSelectedCalendarBlock(null)
                }
              >
                {t('Close')}
              </button>
            </div>
          </>
        )}
      </Modal>
    </div>
  )
}

export default BookingsPage