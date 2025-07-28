package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @Size(min = 1, max = 50, message = "Название категории должно быть от 1 до 50 символов!")
    @NotBlank(message = "Название категории не может быть пустым!")
    private String name;
}