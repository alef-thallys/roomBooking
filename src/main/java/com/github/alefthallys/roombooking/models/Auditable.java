package com.github.alefthallys.roombooking.models;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable<U> {
	
	@CreatedBy
	@Column(name = "created_by", updatable = false)
	protected U createdBy;
	
	@CreatedDate
	@Column(name = "created_date", updatable = false)
	protected LocalDateTime createdDate;
	
	@LastModifiedBy
	@Column(name = "last_modified_by")
	protected U lastModifiedBy;
	
	@LastModifiedDate
	@Column(name = "last_modified_date")
	protected LocalDateTime lastModifiedDate;
}
