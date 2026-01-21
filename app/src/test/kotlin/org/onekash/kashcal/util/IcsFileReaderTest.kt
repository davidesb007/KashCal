package org.onekash.kashcal.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class IcsFileReaderTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var icsFileReader: IcsFileReader
    private val testUri = mockk<Uri>()

    @Before
    fun setup() {
        mockContext = mockk()
        mockContentResolver = mockk()
        every { mockContext.contentResolver } returns mockContentResolver
        icsFileReader = IcsFileReader(mockContext, testDispatcher)
    }

    // ==================== readIcsContent tests ====================

    @Test
    fun `readIcsContent returns success for valid ICS file`() = runTest(testDispatcher) {
        val validIcs = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR"
        every { mockContentResolver.openInputStream(testUri) } returns
            ByteArrayInputStream(validIcs.toByteArray())

        val result = icsFileReader.readIcsContent(testUri)

        assertTrue(result.isSuccess)
        assertEquals(validIcs, result.getOrNull())
    }

    @Test
    fun `readIcsContent returns failure for invalid ICS file without VCALENDAR`() = runTest(testDispatcher) {
        val invalidIcs = "Some random content without calendar markers"
        every { mockContentResolver.openInputStream(testUri) } returns
            ByteArrayInputStream(invalidIcs.toByteArray())

        val result = icsFileReader.readIcsContent(testUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Invalid ICS file: missing VCALENDAR", result.exceptionOrNull()?.message)
    }

    @Test
    fun `readIcsContent returns failure when openInputStream returns null`() = runTest(testDispatcher) {
        every { mockContentResolver.openInputStream(testUri) } returns null

        val result = icsFileReader.readIcsContent(testUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Could not open file", result.exceptionOrNull()?.message)
    }

    @Test
    fun `readIcsContent returns failure on FileNotFoundException`() = runTest(testDispatcher) {
        every { mockContentResolver.openInputStream(testUri) } throws FileNotFoundException("File not found")

        val result = icsFileReader.readIcsContent(testUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundException)
    }

    @Test
    fun `readIcsContent returns failure on IOException`() = runTest(testDispatcher) {
        every { mockContentResolver.openInputStream(testUri) } throws IOException("Read error")

        val result = icsFileReader.readIcsContent(testUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("Read error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `readIcsContent returns failure on SecurityException`() = runTest(testDispatcher) {
        every { mockContentResolver.openInputStream(testUri) } throws SecurityException("Permission denied")

        val result = icsFileReader.readIcsContent(testUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    // ==================== getFileName tests ====================

    @Test
    fun `getFileName returns filename from cursor`() = runTest(testDispatcher) {
        val mockCursor = mockk<Cursor>()
        val expectedFileName = "calendar.ics"
        every { mockContentResolver.query(testUri, null, null, null, null) } returns mockCursor
        every { mockCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getString(0) } returns expectedFileName
        every { mockCursor.close() } returns Unit

        val result = icsFileReader.getFileName(testUri)

        assertEquals(expectedFileName, result)
    }

    @Test
    fun `getFileName returns null when cursor is null`() = runTest(testDispatcher) {
        every { mockContentResolver.query(testUri, null, null, null, null) } returns null

        val result = icsFileReader.getFileName(testUri)

        assertNull(result)
    }

    @Test
    fun `getFileName returns null when column not found`() = runTest(testDispatcher) {
        val mockCursor = mockk<Cursor>()
        every { mockContentResolver.query(testUri, null, null, null, null) } returns mockCursor
        every { mockCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns -1
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.close() } returns Unit

        val result = icsFileReader.getFileName(testUri)

        assertNull(result)
    }

    @Test
    fun `getFileName returns null on exception`() = runTest(testDispatcher) {
        every { mockContentResolver.query(testUri, null, null, null, null) } throws
            SecurityException("Permission denied")

        val result = icsFileReader.getFileName(testUri)

        assertNull(result)
    }
}
