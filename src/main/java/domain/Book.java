package domain;

import lombok.Builder;
@Builder
public record Book(
        String id,
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
