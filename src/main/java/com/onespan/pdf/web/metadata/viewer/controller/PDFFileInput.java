package com.onespan.pdf.web.metadata.viewer.controller;

import org.springframework.web.multipart.MultipartFile;

public class PDFFileInput {
	
	private MultipartFile pdfFile;

	public MultipartFile getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(MultipartFile pdfFile) {
		this.pdfFile = pdfFile;
	}

}
