package com.umc.mada.todo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TodoAverageResponseDto {
    Double todosPercent;
    Double completeTodoPercent;

    @Builder
    public TodoAverageResponseDto(Double todosPercent, Double completeTodoPercent){
        this.todosPercent = todosPercent;
        this.completeTodoPercent = completeTodoPercent;
    }

    public static TodoAverageResponseDto of(Double todosPercent, Double completeTodoPercent){
        return TodoAverageResponseDto.builder()
                .todosPercent(todosPercent)
                .completeTodoPercent(completeTodoPercent)
                .build();
    }
}
