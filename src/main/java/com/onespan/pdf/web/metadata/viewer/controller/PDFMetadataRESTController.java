package com.onespan.pdf.web.metadata.viewer.controller;

import java.util.Locale;
import java.util.function.UnaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.onespan.pdf.web.metadata.viewer.model.PDFMetadata;
import com.onespan.pdf.web.metadata.viewer.service.PDFMetadataService;

@RestController
@RequestMapping("/api")
public class PDFMetadataRESTController {

	@Autowired
	private PDFMetadataService pdfMetadataService;

	@PostMapping("/pdf")
	ResponseEntity<PDFMetadata> getRawMetadata(@RequestParam MultipartFile fileMultipart, String libraryName)
			throws Exception {
		PDFMetadata metadata = pdfMetadataService.process(fileMultipart, libraryName, Locale.US,
				UnaryOperator.identity());
		return ResponseEntity.ok(metadata);
	}

}
