package domain;

public record PostBookDto(
        String type,
        Integer quantity,
        String[] authors,
        String name,
        Integer pages,
        String publisher,
        Integer year,
        String city,
        String department_id,
        String summary,
        String room
) {}
