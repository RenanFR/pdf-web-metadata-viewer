package com.onespan.pdf.web.metadata.viewer.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import com.onespan.pdf.web.metadata.viewer.exception.NotAllowedExtensionException;
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
	private static final String NOT_ALLOWED_EXTENSION_EXCEPTION_MSG = "not.allowed.extension.exception.msg";

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

		} else {
			throw new NotAllowedExtensionException(
					messageSource.getMessage(NOT_ALLOWED_EXTENSION_EXCEPTION_MSG, null, locale), extension);
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

		addViewAttributes(pdfFile, redirectAttributes, pdfService, locale);
	}

	private boolean isAllowedExtension(String extension) {
		return List.of("pdf", "docx").contains(extension);

	}

	private boolean needsConversion(String extension) {
		return List.of("docx").contains(extension);

	}

	private StringBuilder buildDocumentRestrictionSummaryText(Locale locale, String messageAllowed,
			String messageNotAllowed, PDFService pdfService, PdfRestrictions documentRestriction) throws Exception {

		StringBuilder documentRestrictionSummary = new StringBuilder();
		documentRestrictionSummary.append(messageSource.getMessage(PRINT, null, locale) + ": "
				+ (documentRestriction.isPrintingAllowed() ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage(COPY, null, locale) + ": "
				+ (documentRestriction.isCopyAllowed() ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage(MODIFY, null, locale) + ": "
				+ (documentRestriction.isModifyContentsAllowed() ? messageAllowed : messageNotAllowed));
		return documentRestrictionSummary;
	}

	private void addViewAttributes(MultipartFile pdfFile, RedirectAttributes redirectAttributes, PDFService pdfService,
			Locale locale) throws Exception, IOException {

		String messageYes = messageSource.getMessage(YES, null, locale);
		String messageNo = messageSource.getMessage(NO, null, locale);
		String messageAllowed = messageSource.getMessage(ALLOWED, null, locale);
		String messageNotAllowed = messageSource.getMessage(NOT_ALLOWED, null, locale);

		Map<String, Object> metadataFromService = getMetadataFromService(pdfService);

		redirectAttributes.addFlashAttribute("pdfVersion", metadataFromService.get("pdfVersion"));
		redirectAttributes.addFlashAttribute("pdfValidity",
				(boolean) metadataFromService.get("pdfValidity") ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfAda",
				(boolean) metadataFromService.get("pdfAda") ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfFonts", metadataFromService.get("pdfFonts"));
		redirectAttributes.addFlashAttribute("pdfPages", metadataFromService.get("pdfPages"));
		redirectAttributes.addFlashAttribute("pdfLanguage", metadataFromService.get("pdfLanguage"));

		StringBuilder documentRestrictionSummary = buildDocumentRestrictionSummaryText(locale, messageAllowed,
				messageNotAllowed, pdfService, (PdfRestrictions) metadataFromService.get("documentRestriction"));

		redirectAttributes.addFlashAttribute("pdfRestrictions", documentRestrictionSummary.toString());

		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());
		redirectAttributes.addFlashAttribute("pdfSize", getPDFSize(pdfFile));
	}

	private String getPDFSize(MultipartFile pdfFile) {
		return pdfFile.getSize() + " Bytes";
	}

	private Map<String, Object> getMetadataFromService(PDFService pdfService) throws Exception, IOException {

		String pdfFonts = pdfService.getFontList().stream().collect(Collectors.joining(", ", "(", ")"));
		PdfRestrictions documentRestriction = pdfService.getDocumentRestrictionSummary();

		return Map.of("pdfVersion", pdfService.getPDFVersion(), "pdfValidity", pdfService.isValid(), "pdfAda",
				pdfService.isAdaCompliant(), "pdfFonts", pdfFonts, "pdfPages", String.valueOf(pdfService.getPages()),
				"pdfLanguage", DocumentLanguageDetector.getLanguageFromDocumentText(pdfService.getDocumentRawText()),
				"documentRestriction", documentRestriction);
	}

	@ExceptionHandler(RuntimeException.class)
	public String handleException(RuntimeException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("MSG_ERROR", ex.getMessage());
		return "redirect:/pdf";
	}

}
