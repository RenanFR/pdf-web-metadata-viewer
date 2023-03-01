package com.onespan.pdf.web.metadata.viewer.controller;

import java.io.File;
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

import com.onespan.pdf.web.metadata.viewer.service.PDFService;

@Controller
public class PDFMetadataController {

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
	public String receiveAndAnalysePdf(@RequestParam MultipartFile pdfFile, RedirectAttributes redirectAttributes,
			@RequestParam(required = false) String lang) throws Exception {

		File tmpFile = new File(System.getProperty("java.io.tmpdir") + "/" + pdfFile.getOriginalFilename());
		pdfFile.transferTo(tmpFile);

		Locale locale = lang != null ? new Locale(lang) : Locale.US;
		String messageYes = messageSource.getMessage("home.pdf.info.y", null, locale);
		String messageNo = messageSource.getMessage("home.pdf.info.n", null, locale);
		String messageAllowed = messageSource.getMessage("home.pdf.info.allowed", null, locale);
		String messageNotAllowed = messageSource.getMessage("home.pdf.info.not_allowed", null, locale);

		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());

		PDFService pdfService = PDFService.initialize(tmpFile.getAbsolutePath());

		redirectAttributes.addFlashAttribute("pdfValidity", pdfService.isValid() ? messageYes : messageNo);
		redirectAttributes.addFlashAttribute("pdfPages", pdfService.getPages());
		redirectAttributes.addFlashAttribute("pdfSize", pdfFile.getSize() + " Bytes");
		redirectAttributes.addFlashAttribute("pdfVersion", pdfService.getPDFVersion());
		redirectAttributes.addFlashAttribute("pdfAda", pdfService.isAdaCompliant() ? messageYes : messageNo);

		boolean[] documentRestriction = pdfService.getDocumentRestrictionSummary(tmpFile.getAbsolutePath());
		StringBuilder documentRestrictionSummary = new StringBuilder();
		documentRestrictionSummary.append(messageSource.getMessage("home.pdf.info.restrictions.print", null, locale)
				+ ": " + (documentRestriction[0] ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary.append(messageSource.getMessage("home.pdf.info.restrictions.copy", null, locale)
				+ ": " + (documentRestriction[1] ? messageAllowed : messageNotAllowed) + ", ");
		documentRestrictionSummary
				.append(messageSource.getMessage("home.pdf.info.restrictions.modification", null, locale) + ": "
						+ (documentRestriction[2] ? messageAllowed : messageNotAllowed));

		redirectAttributes.addFlashAttribute("pdfRestrictions", documentRestrictionSummary.toString());

		String pdfFonts = pdfService.getFontList().stream().collect(Collectors.joining(", ", "(", ")"));

		redirectAttributes.addFlashAttribute("pdfFonts", pdfFonts);
		redirectAttributes.addFlashAttribute("pdfLanguage", pdfService.getDocumentLanguage());

		return "redirect:/pdf";
	}

}
