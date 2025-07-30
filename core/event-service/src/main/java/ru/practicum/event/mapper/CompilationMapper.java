package ru.practicum.event.mapper;

import ru.practicum.event.dto.CompilationDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewCompilationDto;
import ru.practicum.event.model.Compilation;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Set;

public class CompilationMapper {

    public static Compilation toCompilationFromNewCompilationDto(NewCompilationDto newCompilationDto, Set<Event> events) {
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .events(events)
                .pinned(newCompilationDto.isPinned())
                .build();
    }

    public static CompilationDto toCompilationDtoFromCompilation(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(events)
                .build();
    }
}