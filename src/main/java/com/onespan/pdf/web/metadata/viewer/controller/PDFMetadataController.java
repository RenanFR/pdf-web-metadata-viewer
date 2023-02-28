package com.onespan.pdf.web.metadata.viewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onespan.pdf.web.metadata.viewer.service.PDFApryseService;

@Controller
public class PDFMetadataController {

	@GetMapping("/")
	public String index() {
		return "home/index";
	}

	@GetMapping("/pdf")
	public String pdfUploadPage() {
		return "home/pdf";
	}

	@PostMapping("/pdf")
	public String receiveAndAnalysePdf(@RequestParam MultipartFile pdfFile, RedirectAttributes redirectAttributes)
			throws Exception {
		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());
		PDFApryseService pdfService = PDFApryseService.initialize(pdfFile.getInputStream());
		redirectAttributes.addFlashAttribute("pdfPages", pdfService.getPages());
		return "redirect:/pdf";
	}

}
