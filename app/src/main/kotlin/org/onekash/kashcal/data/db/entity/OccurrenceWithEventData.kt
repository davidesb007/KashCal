package org.onekash.kashcal.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Data class for JOIN query results combining Occurrence and Event data.
 *
 * Used by Room to map JOIN query results. The @Embedded annotation with prefix
 * allows Room to correctly map Event columns that would otherwise conflict with
 * Occurrence columns (e.g., both have 'id', 'start_ts', 'end_ts').
 *
 * Room tracks BOTH tables when this class is used in a JOIN query, so the Flow
 * will emit when either the occurrences OR events table changes. This fixes the
 * reactivity issue where event metadata changes (location, title, etc.) didn't
 * trigger UI updates.
 */
data class OccurrenceWithEventData(
    // Occurrence fields (no prefix needed - column names don't conflict)
    val id: Long,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "exception_event_id") val exceptionEventId: Long?,
    @ColumnInfo(name = "calendar_id") val calendarId: Long,
    @ColumnInfo(name = "start_ts") val startTs: Long,
    @ColumnInfo(name = "end_ts") val endTs: Long,
    @ColumnInfo(name = "start_day") val startDay: Int,
    @ColumnInfo(name = "end_day") val endDay: Int,
    @ColumnInfo(name = "is_cancelled") val isCancelled: Boolean,

    // Embed full Event with prefix to avoid column name collision
    // All Event columns in the query must be aliased with "e_" prefix
    @Embedded(prefix = "e_") val event: Event
) {
    /**
     * Convert to Occurrence entity for use in existing code.
     */
    fun toOccurrence() = Occurrence(
        id = id,
        eventId = eventId,
        exceptionEventId = exceptionEventId,
        calendarId = calendarId,
        startTs = startTs,
        endTs = endTs,
        startDay = startDay,
        endDay = endDay,
        isCancelled = isCancelled
    )
}
