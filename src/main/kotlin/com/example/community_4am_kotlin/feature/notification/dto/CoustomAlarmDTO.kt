package com.example.community_4am_Kotlin.feature.notification.dto


import com.example.community_4am_Kotlin.domain.notification.CoustomAlarm
import java.time.LocalTime

data class CoustomAlarmDTO(
    var id: Long? = null,
    var message: String? = null,
    var notificationDays: MutableSet<String>? = null,
    var reserveAt: String? = null, // "HH:mm" 형식
    var status: Boolean? = null,
    var isRead: Boolean,
    var dataType: String = "CoustomAlarm"
) {
    fun toDTO(alarm: CoustomAlarm): CoustomAlarmDTO {
        return CoustomAlarmDTO(
            id = alarm.id,
            message = alarm.message,
            notificationDays = alarm.notificationDays,
            reserveAt = alarm.reserveAt.toString(), // "HH:mm" 형식으로 변환
            status = alarm.status,
            dataType = "CoustomAlarm",
            isRead = alarm.isRead
        )
    }

    fun toEntity(dto: CoustomAlarmDTO): CoustomAlarm {
        return CoustomAlarm(
            id = dto.id,
            message = dto.message,
            notificationDays = dto.notificationDays,
            reserveAt = LocalTime.parse(dto.reserveAt),
            status = dto.status,
            isRead = dto.isRead
        )
    }
}
