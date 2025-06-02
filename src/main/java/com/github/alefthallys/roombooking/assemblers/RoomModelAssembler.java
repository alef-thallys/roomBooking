package com.github.alefthallys.roombooking.assemblers;

import com.github.alefthallys.roombooking.controllers.RoomController;
import com.github.alefthallys.roombooking.dtos.Room.RoomResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RoomModelAssembler implements RepresentationModelAssembler<RoomResponseDTO, EntityModel<RoomResponseDTO>> {
	
	@Override
	public EntityModel<RoomResponseDTO> toModel(RoomResponseDTO dto) {
		return EntityModel.of(dto,
				linkTo(methodOn(RoomController.class).findById(dto.id())).withSelfRel(),
				linkTo(methodOn(RoomController.class).update(dto.id(), null)).withRel("update"),
				linkTo(methodOn(RoomController.class).delete(dto.id())).withRel("delete")
		);
	}
}

