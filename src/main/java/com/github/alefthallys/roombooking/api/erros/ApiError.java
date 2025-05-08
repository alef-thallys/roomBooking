package com.github.alefthallys.roombooking.api.erros;

import java.time.Instant;

public class ApiError {
	
	private int status;
	private String message;
	private long timestamp;
	
	public ApiError(int status, String message) {
		this.status = status;
		this.message = message;
		this.timestamp = Instant.now().toEpochMilli();
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
