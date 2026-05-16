package com.nammashalli.inventory.ui.common

import androidx.compose.ui.graphics.Color
import com.nammashalli.inventory.ui.theme.*

enum class AssetStatus(val label: String, val color: Color, val bgColor: Color, val emoji: String) {
    GOOD("Good", StatusGreen, StatusGreenBg, "✓"),
    FAIR("Fair", StatusYellow, StatusYellowBg, "~"),
    NEEDS_REPAIR("NeedsRepair", StatusRed, StatusRedBg, "!"),
    LOST("Lost", StatusBlack, StatusBlackBg, "✗");

    companion object {
        fun fromString(value: String): AssetStatus = entries.find {
            it.name == value || it.label == value
        } ?: GOOD

        fun displayLabel(value: String): String = fromString(value).label
        fun statusColor(value: String): Color = fromString(value).color
        fun statusBgColor(value: String): Color = fromString(value).bgColor
    }
}

enum class AssetCategory(val label: String) {
    LAB_EQUIPMENT("Lab Equipment"),
    SPORTS_KIT("Sports Kit"),
    TECHNOLOGY("Technology"),
    FURNITURE("Furniture"),
    OTHER("Other");

    companion object {
        fun all() = entries.map { it.label }
        fun fromLabel(label: String) = entries.find { it.label == label } ?: OTHER
    }
}

enum class AssetLocation(val label: String) {
    PHYSICS_LAB("Physics Lab"),
    CHEMISTRY_LAB("Chemistry Lab"),
    BIOLOGY_LAB("Biology Lab"),
    SPORTS("Sports Room"),
    COMPUTER_LAB("Computer Lab"),
    LIBRARY("Library"),
    CLASSROOM("Classroom"),
    OTHER("Other");

    companion object {
        fun all() = entries.map { it.label }
    }
}

enum class UserRole(val label: String) {
    TEACHER("Teacher"),
    SDMC_MEMBER("SDMC Member"),
    PRINCIPAL("Principal");

    companion object {
        fun all() = entries.map { it.label }
    }
}

enum class IssueType(val label: String) {
    BROKEN("Broken"),
    LOST("Lost"),
    STOLEN("Stolen"),
    MISSING("Missing"),
    DAMAGED("Damaged"),
    MAINTENANCE_NEEDED("Maintenance Needed"),
    OTHER("Other");

    companion object {
        fun all() = entries.map { it.label }
    }
}

enum class RepairPriority(val label: String, val color: Color) {
    HIGH("High", StatusRed),
    MEDIUM("Medium", StatusYellow),
    LOW("Low", StatusGreen);

    companion object {
        fun all() = entries.map { it.label }
        fun colorFor(label: String) = entries.find { it.label == label }?.color ?: StatusYellow
    }
}
