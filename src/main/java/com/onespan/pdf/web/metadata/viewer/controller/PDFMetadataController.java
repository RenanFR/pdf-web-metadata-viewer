package com.onespan.pdf.web.metadata.viewer.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.function.UnaryOperator;

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

import com.onespan.pdf.web.metadata.viewer.model.PDFMetadata;
import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;
import com.onespan.pdf.web.metadata.viewer.service.PDFMetadataService;

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

	@Autowired
	private PDFMetadataService pdfMetadataService;

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

		Locale locale = lang != null ? new Locale(lang) : Locale.US;

		pdfMetadataService.process(fileMultipart, libraryName, locale,
				getAction(fileMultipart, redirectAttributes, locale));

		return "redirect:/pdf";
	}

	private UnaryOperator<PDFMetadata> getAction(MultipartFile fileMultipart, RedirectAttributes redirectAttributes,
			Locale locale) {
		UnaryOperator<PDFMetadata> doWithMetadataAction = metadata -> {
			try {
				return addViewAttributes(fileMultipart, redirectAttributes, metadata, locale);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
			return null;
		};
		return doWithMetadataAction;
	}

	private StringBuilder buildDocumentRestrictionSummaryText(Locale locale, String messageAllowed,
			String messageNotAllowed, PdfRestrictions documentRestriction) throws Exception {

		StringBuilder documentRestrictionSummary = new StringBuilder();
		documentRestrictionSummary.append(messageSource.getMessage(PRINT, null, locale) + ": "
				+ (documentRestriction.isPrintingAllowed() ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage(COPY, null, locale) + ": "
				+ (documentRestriction.isCopyAllowed() ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage(MODIFY, null, locale) + ": "
				+ (documentRestriction.isModifyContentsAllowed() ? messageAllowed : messageNotAllowed));
		return documentRestrictionSummary;
	}

	private PDFMetadata addViewAttributes(MultipartFile pdfFile, RedirectAttributes redirectAttributes,
			PDFMetadata metadataFromService, Locale locale) throws Exception, IOException {

		String messageYes = messageSource.getMessage(YES, null, locale);
		String messageNo = messageSource.getMessage(NO, null, locale);
		String messageAllowed = messageSource.getMessage(ALLOWED, null, locale);
		String messageNotAllowed = messageSource.getMessage(NOT_ALLOWED, null, locale);

		redirectAttributes.addFlashAttribute("pdfVersion", metadataFromService.pdfVersion());
		redirectAttributes.addFlashAttribute("pdfValidity", metadataFromService.pdfValidity() ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfAda", metadataFromService.pdfAda() ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfFonts", metadataFromService.pdfFonts());
		redirectAttributes.addFlashAttribute("pdfPages", metadataFromService.pdfPages());
		redirectAttributes.addFlashAttribute("pdfLanguage", metadataFromService.pdfLanguage());

		StringBuilder documentRestrictionSummary = buildDocumentRestrictionSummaryText(locale, messageAllowed,
				messageNotAllowed, (PdfRestrictions) metadataFromService.pdfRestrictions());

		redirectAttributes.addFlashAttribute("pdfRestrictions", documentRestrictionSummary.toString());

		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());
		redirectAttributes.addFlashAttribute("pdfSize", getPDFSize(pdfFile));
		return metadataFromService;
	}

	private String getPDFSize(MultipartFile pdfFile) {
		return pdfFile.getSize() + " Bytes";
	}

	@ExceptionHandler(RuntimeException.class)
	public String handleException(RuntimeException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("MSG_ERROR", ex.getMessage());
		return "redirect:/pdf";
	}

}
