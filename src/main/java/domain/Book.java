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
) {
	public Book withId(String id) {
		return new Book(id, type(), quantity(), authors(), name(), pages(), publisher(), year(), city(), department_id(), summary(), room());
	}
}