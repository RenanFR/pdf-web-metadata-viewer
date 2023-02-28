package com.onespan.pdf.web.metadata.viewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PDFMetadataController {

	@GetMapping("/")
	public String index() {
		return "home/index";
	}

	@GetMapping("/pdf")
	public String pdfUploadPage(Model model) {
		model.addAttribute("pdfFileInput", new PDFFileInput());
		return "home/pdf";
	}

	@PostMapping("/pdf")
	public String receiveAndAnalysePdf(@RequestParam MultipartFile pdfFile,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("MSG_SUCCESS", pdfFile.getOriginalFilename());
		return "redirect:/pdf";
	}

}
