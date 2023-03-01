package com.onespan.pdf.web.metadata.viewer.controller;

import java.util.Locale;

import org.apache.el.parser.AstFalse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onespan.pdf.web.metadata.viewer.service.PDFApryseService;

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
		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());
		PDFApryseService pdfService = PDFApryseService.initialize(pdfFile.getInputStream());
		redirectAttributes.addFlashAttribute("pdfPages", pdfService.getPages());
		redirectAttributes.addFlashAttribute("pdfSize", pdfFile.getSize() + " Bytes");
		redirectAttributes.addFlashAttribute("pdfVersion", pdfService.getPDFVersion());
		Locale locale = lang != null ? new Locale(lang) : Locale.US;
		redirectAttributes.addFlashAttribute("pdfAda",
				pdfService.isAdaCompliant() ? messageSource.getMessage("home.pdf.info.ada.y", null, locale)
						: messageSource.getMessage("home.pdf.info.ada.n", null, locale));
		return "redirect:/pdf";
	}

}
