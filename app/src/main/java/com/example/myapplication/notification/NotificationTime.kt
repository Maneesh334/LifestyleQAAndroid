package com.example.myapplication.notification

data class NotificationTime(
    val hour: Int,
    val minute: Int,
    val question: String = ""
) {
    val id: Int
        get() = hour * 100 + minute

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationTime) return false
        return hour == other.hour && minute == other.minute
    }

    override fun hashCode(): Int {
        var result = hour
        result = 31 * result + minute
        return result
    }
}
