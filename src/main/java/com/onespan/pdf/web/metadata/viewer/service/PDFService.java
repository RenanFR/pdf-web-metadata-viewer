package com.onespan.pdf.web.metadata.viewer.service;

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.MessageSource;

import com.onespan.pdf.web.metadata.viewer.model.PdfLibrary;
import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;

public interface PDFService {

	long getPages() throws Exception;

	String getPDFVersion() throws Exception;

	boolean isValid() throws Exception;

	Set<String> getFontList() throws Exception;

	boolean isAdaCompliant() throws Exception;

	PdfRestrictions getDocumentRestrictionSummary() throws Exception;

	StringBuilder getDocumentRawText() throws Exception;

	static PDFService getByLibrary(PdfLibrary library, InputStream fileInputStream, MessageSource messageSource, Locale locale) throws Exception {
		return switch (library) {
		case ITEXT: {
			yield ItextPDFService.initialize(fileInputStream, messageSource, locale);
		}
		case PDFTRON: {
			yield PDFTronService.initialize(fileInputStream);
		}
		};
	}

	static PDFService convertFromDocxUsingLibrary(PdfLibrary library, String docx, MessageSource messageSource, Locale locale) throws Exception {
		return switch (library) {
		case ITEXT: {
			yield ItextPDFService.initializeFromDocx(docx, messageSource, locale);
		}
		case PDFTRON: {
			yield PDFTronService.initializeFromDocx(docx);
		}
		};
	}

}
