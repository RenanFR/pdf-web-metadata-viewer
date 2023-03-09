package com.onespan.pdf.web.metadata.viewer.exception;

public class NotAllowedExtensionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotAllowedExtensionException(String msg, String fileExtension) {
		super(msg.replace("{}", fileExtension));
	}

}
