package com.onespan.pdf.web.metadata.viewer.model;

public record PDFMetadata(String pdfVersion, boolean pdfValidity, boolean pdfAda, String pdfFonts, String pdfPages,
		String pdfLanguage, PdfRestrictions pdfRestrictions) {

}
