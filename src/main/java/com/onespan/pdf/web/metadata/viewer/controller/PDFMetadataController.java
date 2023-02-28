package com.onespan.pdf.web.metadata.viewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PDFMetadataController {
	
    @GetMapping("/")
    public String index() {
        return "home/index";
    }
    
    @GetMapping("/pdf")
    public String pdf() {
    	return "home/pdf";
    }

}
