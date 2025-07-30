package ru.practicum.event.service;

import ru.practicum.event.dto.CompilationDto;
import ru.practicum.event.dto.params.CompilationParamsPublic;
import ru.practicum.event.dto.NewCompilationDto;
import ru.practicum.event.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(CompilationParamsPublic params);

    CompilationDto getCompilationById(Long compId);

    CompilationDto create(NewCompilationDto newCompilationDto);

    void deleteById(Long compId);

    CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest);
}