package org.onekash.kashcal.ui.screens.settings

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.onekash.kashcal.data.preferences.KashCalDataStore
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Time format option with value, label, and dynamic example.
 */
private data class TimeFormatOption(
    val value: String,
    val label: String,
    val description: String
)

/**
 * Bottom sheet for selecting time format preference.
 *
 * Shows three options:
 * - System default: Follows device's 24-hour setting
 * - 12-hour: Always shows times like "2:30 PM"
 * - 24-hour: Always shows times like "14:30"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFormatSheet(
    sheetState: SheetState,
    currentFormat: String,
    onFormatSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val is24HourDevice = remember { DateFormat.is24HourFormat(context) }
    val now = remember { LocalTime.now() }

    // Format current time as examples
    val example12h = remember(now) {
        now.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }
    val example24h = remember(now) {
        now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }

    // Build options with dynamic descriptions
    val options = remember(is24HourDevice, example12h, example24h) {
        listOf(
            TimeFormatOption(
                value = KashCalDataStore.TIME_FORMAT_SYSTEM,
                label = "System default",
                description = if (is24HourDevice) "Currently: 24-hour ($example24h)"
                else "Currently: 12-hour ($example12h)"
            ),
            TimeFormatOption(
                value = KashCalDataStore.TIME_FORMAT_12H,
                label = "12-hour",
                description = "Example: $example12h"
            ),
            TimeFormatOption(
                value = KashCalDataStore.TIME_FORMAT_24H,
                label = "24-hour",
                description = "Example: $example24h"
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Time Format",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Options
            options.forEach { option ->
                TimeFormatOptionRow(
                    option = option,
                    isSelected = currentFormat == option.value,
                    onSelect = {
                        onFormatSelect(option.value)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeFormatOptionRow(
    option: TimeFormatOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
