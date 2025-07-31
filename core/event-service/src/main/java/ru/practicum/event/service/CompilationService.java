package ru.practicum.event.service;


import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.NewCompilationDto;
import ru.practicum.ewm.main.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.dto.params.CompilationParamsPublic;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(CompilationParamsPublic params);

    CompilationDto getCompilationById(Long compId);

    CompilationDto create(NewCompilationDto newCompilationDto);

    void deleteById(Long compId);

    CompilationDto update(Long compId, UpdateCompilationRequest updateCompilationRequest);
}