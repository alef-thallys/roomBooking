package com.github.alefthallys.roombooking.annotations;

import com.github.alefthallys.roombooking.validadors.ReservationDatesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReservationDatesValidator.class)
@Documented
public @interface ValidReservationDates {
	String message() default "Start date must be before end date";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}

