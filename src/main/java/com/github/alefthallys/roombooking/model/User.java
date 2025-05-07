package com.github.alefthallys.roombooking.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String name;
	private String email;
	private String password;
	private String phone;
	private Role role;
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(getId(), user.getId()) && Objects.equals(getName(), user.getName()) && Objects.equals(getEmail(), user.getEmail()) && Objects.equals(getPassword(), user.getPassword()) && Objects.equals(getPhone(), user.getPhone()) && getRole() == user.getRole();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getName(), getEmail(), getPassword(), getPhone(), getRole());
	}
	
	private enum Role {
		ADMIN,
		USER
	}
}
