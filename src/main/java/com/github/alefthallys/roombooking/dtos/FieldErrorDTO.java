package com.github.alefthallys.roombooking.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldErrorDTO {
	private String field;
	private String message;
	private Object rejectedValue;
	
	public FieldErrorDTO(String field, String message, Object rejectedValue) {
		this.field = field;
		this.message = message;
		this.rejectedValue = rejectedValue;
	}
}

