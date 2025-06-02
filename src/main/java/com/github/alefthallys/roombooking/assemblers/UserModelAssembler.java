package com.github.alefthallys.roombooking.assemblers;

import com.github.alefthallys.roombooking.controllers.UserController;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserResponseDTO, EntityModel<UserResponseDTO>> {
	
	@Override
	public EntityModel<UserResponseDTO> toModel(UserResponseDTO dto) {
		return EntityModel.of(dto,
				linkTo(methodOn(UserController.class).findById(dto.id())).withSelfRel(),
				linkTo(methodOn(UserController.class).update(dto.id(), null)).withRel("update"),
				linkTo(methodOn(UserController.class).delete(dto.id())).withRel("delete")
		);
	}
}

