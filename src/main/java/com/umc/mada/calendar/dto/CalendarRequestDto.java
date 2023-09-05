package com.umc.mada.calendar.dto;

import lombok.*;
import reactor.util.annotation.Nullable;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CalendarRequestDto {
    private Long calendarId;
    private String calendarName;
    @Nullable
    private Date startDate;
    private Date endDate;
    private String color;
    private String repeatInfo;
    @Nullable
    private LocalTime startTime;
    private LocalTime endTime;
    private String  repeat;
    private Character dday;
    private String memo;
    private Boolean isExpired;
}
