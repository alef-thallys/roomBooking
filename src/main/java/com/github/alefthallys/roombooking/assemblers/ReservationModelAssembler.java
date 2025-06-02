package com.github.alefthallys.roombooking.assemblers;

import com.github.alefthallys.roombooking.controllers.ReservationController;
import com.github.alefthallys.roombooking.dtos.Reservation.ReservationResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ReservationModelAssembler implements RepresentationModelAssembler<ReservationResponseDTO, EntityModel<ReservationResponseDTO>> {
	
	@Override
	public EntityModel<ReservationResponseDTO> toModel(ReservationResponseDTO dto) {
		return EntityModel.of(dto,
				linkTo(methodOn(ReservationController.class).findById(dto.id())).withSelfRel(),
				linkTo(methodOn(ReservationController.class).update(dto.id(), null)).withRel("update"),
				linkTo(methodOn(ReservationController.class).delete(dto.id())).withRel("delete")
		);
	}
}

