package com.onespan.pdf.web.metadata.viewer.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
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
	public String receiveAndAnalysePdf(@RequestParam MultipartFile pdfFile, String libraryName,
			RedirectAttributes redirectAttributes, @RequestParam(required = false) String lang) throws Exception {

		Locale locale = lang != null ? new Locale(lang) : Locale.US;
		String messageYes = messageSource.getMessage(YES, null, locale);
		String messageNo = messageSource.getMessage(NO, null, locale);
		String messageAllowed = messageSource.getMessage(ALLOWED, null, locale);
		String messageNotAllowed = messageSource.getMessage(NOT_ALLOWED, null, locale);

		PDFService pdfService = PDFService.getByLibrary(PdfLibrary.getByName(libraryName), pdfFile.getInputStream());

		StringBuilder documentRestrictionSummary = buildDocumentRestrictionSummaryText(locale, messageAllowed,
				messageNotAllowed, pdfService);

		String pdfFonts = pdfService.getFontList().stream().collect(Collectors.joining(", ", "(", ")"));

		addViewAttributes(pdfFile, redirectAttributes, messageYes, messageNo, pdfService, documentRestrictionSummary,
				pdfFonts);

		return "redirect:/pdf";
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

}
