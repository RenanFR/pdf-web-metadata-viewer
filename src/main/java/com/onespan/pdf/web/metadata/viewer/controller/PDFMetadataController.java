package com.onespan.pdf.web.metadata.viewer.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onespan.pdf.web.metadata.viewer.model.PdfLibrary;
import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;
import com.onespan.pdf.web.metadata.viewer.service.PDFService;
import com.onespan.pdf.web.metadata.viewer.util.DocumentLanguageDetector;

@Controller
public class PDFMetadataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDFMetadataController.class);

	private static final String MODIFY = "home.pdf.info.restrictions.modification";
	private static final String COPY = "home.pdf.info.restrictions.copy";
	private static final String PRINT = "home.pdf.info.restrictions.print";
	private static final String NOT_ALLOWED = "home.pdf.info.not_allowed";
	private static final String ALLOWED = "home.pdf.info.allowed";
	private static final String NO = "home.pdf.info.n";
	private static final String YES = "home.pdf.info.y";

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/")
	public String index() {
		return "home/index";
	}

	@GetMapping("/pdf")
	public String pdfUploadPage() {
		return "home/pdf";
	}

	@PostMapping("/pdf")
	public String receiveAndAnalysePdf(@RequestParam MultipartFile fileMultipart, String libraryName,
			RedirectAttributes redirectAttributes, @RequestParam(required = false) String lang) throws Exception {

		String extension = FilenameUtils.getExtension(fileMultipart.getOriginalFilename());
		Locale locale = lang != null ? new Locale(lang) : Locale.US;

		LOGGER.info("File extension: {}", extension);

		if (isAllowedExtension(extension)) {
			if (!needsConversion(extension)) {
				PDFService pdfService = PDFService.getByLibrary(PdfLibrary.getByName(libraryName),
						fileMultipart.getInputStream(), messageSource, locale);
				processPdf(fileMultipart, redirectAttributes, locale, pdfService);
			} else {
				convertFromWordDocumentAndProcess(fileMultipart, libraryName, redirectAttributes, locale);
			}

		}

		return "redirect:/pdf";
	}

	private void convertFromWordDocumentAndProcess(MultipartFile fileMultipart, String libraryName,
			RedirectAttributes redirectAttributes, Locale locale) throws IOException, Exception {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File tmpFile = new File(tmpdir + "/" + fileMultipart.getOriginalFilename());
		fileMultipart.transferTo(tmpFile);
		PDFService pdfService = PDFService.convertFromDocxUsingLibrary(PdfLibrary.getByName(libraryName),
				tmpFile.getAbsolutePath(), messageSource, null);
		tmpFile.delete();
		processPdf(fileMultipart, redirectAttributes, locale, pdfService);
	}

	private void processPdf(MultipartFile pdfFile, RedirectAttributes redirectAttributes, Locale locale,
			PDFService pdfService) throws Exception, IOException {

		String messageYes = messageSource.getMessage(YES, null, locale);
		String messageNo = messageSource.getMessage(NO, null, locale);
		String messageAllowed = messageSource.getMessage(ALLOWED, null, locale);
		String messageNotAllowed = messageSource.getMessage(NOT_ALLOWED, null, locale);

		StringBuilder documentRestrictionSummary = buildDocumentRestrictionSummaryText(locale, messageAllowed,
				messageNotAllowed, pdfService);

		String pdfFonts = pdfService.getFontList().stream().collect(Collectors.joining(", ", "(", ")"));

		addViewAttributes(pdfFile, redirectAttributes, messageYes, messageNo, pdfService, documentRestrictionSummary,
				pdfFonts);
	}

	private boolean isAllowedExtension(String extension) {
		return List.of("pdf", "docx").contains(extension);

	}

	private boolean needsConversion(String extension) {
		return List.of("docx").contains(extension);

	}

	private StringBuilder buildDocumentRestrictionSummaryText(Locale locale, String messageAllowed,
			String messageNotAllowed, PDFService pdfService) throws Exception {
		PdfRestrictions documentRestriction = pdfService.getDocumentRestrictionSummary();
		StringBuilder documentRestrictionSummary = new StringBuilder();
		documentRestrictionSummary.append(messageSource.getMessage(PRINT, null, locale) + ": "
				+ (documentRestriction.isPrintingAllowed() ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage(COPY, null, locale) + ": "
				+ (documentRestriction.isCopyAllowed() ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage(MODIFY, null, locale) + ": "
				+ (documentRestriction.isModifyContentsAllowed() ? messageAllowed : messageNotAllowed));
		return documentRestrictionSummary;
	}

	private void addViewAttributes(MultipartFile pdfFile, RedirectAttributes redirectAttributes, String messageYes,
			String messageNo, PDFService pdfService, StringBuilder documentRestrictionSummary, String pdfFonts)
			throws Exception, IOException {
		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());
		redirectAttributes.addFlashAttribute("pdfVersion", pdfService.getPDFVersion());
		redirectAttributes.addFlashAttribute("pdfValidity", pdfService.isValid() ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfAda", pdfService.isAdaCompliant() ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfFonts", pdfFonts);
		redirectAttributes.addFlashAttribute("pdfRestrictions", documentRestrictionSummary.toString());
		redirectAttributes.addFlashAttribute("pdfPages", pdfService.getPages());
		redirectAttributes.addFlashAttribute("pdfSize", pdfFile.getSize() + " Bytes");
		redirectAttributes.addFlashAttribute("pdfLanguage",
				DocumentLanguageDetector.getLanguageFromDocumentText(pdfService.getDocumentRawText()));
	}

	@ExceptionHandler(UnsupportedOperationException.class)
	public String handleUnsupportedOperation(UnsupportedOperationException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("MSG_ERROR", ex.getMessage());
		return "redirect:/pdf";
	}

}
