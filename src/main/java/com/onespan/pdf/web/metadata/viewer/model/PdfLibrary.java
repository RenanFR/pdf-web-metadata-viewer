package com.onespan.pdf.web.metadata.viewer.model;

import java.util.Arrays;

public enum PdfLibrary {

	ITEXT("iText"), PDFTRON("PDFTron");

	private String name;

	private PdfLibrary(String name) {
		this.name = name;
	}

	public static PdfLibrary getByName(String name) {
		PdfLibrary library = Arrays.asList(PdfLibrary.values()).stream().filter(lib -> lib.name.equalsIgnoreCase(name))
				.findAny().orElse(PDFTRON);
		return library;
	}

}
