package com.onespan.pdf.web.metadata.viewer.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onespan.pdf.web.metadata.viewer.exception.NotAllowedExtensionException;
import com.onespan.pdf.web.metadata.viewer.model.PDFMetadata;
import com.onespan.pdf.web.metadata.viewer.model.PdfLibrary;
import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;
import com.onespan.pdf.web.metadata.viewer.util.DocumentLanguageDetector;

@Service
public class PDFMetadataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDFMetadataService.class);

	private static final String NOT_ALLOWED_EXTENSION_EXCEPTION_MSG = "not.allowed.extension.exception.msg";

	@Autowired
	private MessageSource messageSource;

	public PDFMetadata process(MultipartFile fileMultipart, String libraryName, Locale locale,
			UnaryOperator<PDFMetadata> doWithMetadataAction) throws Exception, IOException {
		String extension = FilenameUtils.getExtension(fileMultipart.getOriginalFilename());

		LOGGER.info("File extension: {}", extension);

		if (isAllowedExtension(extension)) {
			if (!needsConversion(extension)) {
				PDFService pdfService = PDFService.getByLibrary(PdfLibrary.getByName(libraryName),
						fileMultipart.getInputStream(), messageSource, locale);
				return processPdf(pdfService, doWithMetadataAction);
			} else {
				return convertFromWordDocumentAndProcess(fileMultipart, libraryName, doWithMetadataAction);
			}

		} else {
			throw new NotAllowedExtensionException(
					messageSource.getMessage(NOT_ALLOWED_EXTENSION_EXCEPTION_MSG, null, locale), extension);
		}
	}

	private boolean isAllowedExtension(String extension) {
		return List.of("pdf", "docx").contains(extension);

	}

	private boolean needsConversion(String extension) {
		return List.of("docx").contains(extension);

	}

	private PDFMetadata processPdf(PDFService pdfService, UnaryOperator<PDFMetadata> doWithMetadataAction)
			throws Exception, IOException {

		PDFMetadata metadataFromService = getMetadataFromService(pdfService);

		return doWithMetadataAction.apply(metadataFromService);

	}

	private PDFMetadata getMetadataFromService(PDFService pdfService) throws Exception, IOException {

		String pdfFonts = pdfService.getFontList().stream().collect(Collectors.joining(", ", "(", ")"));
		PdfRestrictions documentRestriction = pdfService.getDocumentRestrictionSummary();

		return new PDFMetadata(pdfService.getPDFVersion(), pdfService.isValid(), pdfService.isAdaCompliant(), pdfFonts,
				String.valueOf(pdfService.getPages()),
				DocumentLanguageDetector.getLanguageFromDocumentText(pdfService.getDocumentRawText()),
				documentRestriction);
	}

	private PDFMetadata convertFromWordDocumentAndProcess(MultipartFile fileMultipart, String libraryName,
			UnaryOperator<PDFMetadata> doWithMetadataAction) throws IOException, Exception {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File tmpFile = new File(tmpdir + "/" + fileMultipart.getOriginalFilename());
		fileMultipart.transferTo(tmpFile);
		PDFService pdfService = PDFService.convertFromDocxUsingLibrary(PdfLibrary.getByName(libraryName),
				tmpFile.getAbsolutePath(), messageSource, null);
		tmpFile.delete();
		return processPdf(pdfService, doWithMetadataAction);
	}

}
