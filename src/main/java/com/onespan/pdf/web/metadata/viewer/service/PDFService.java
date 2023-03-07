package com.onespan.pdf.web.metadata.viewer.service;

import java.io.InputStream;
import java.util.Set;

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

	static PDFService getByLibrary(PdfLibrary library, InputStream fileInputStream) throws Exception {
		return switch (library) {
		case ITEXT: {
			yield ItextPDFService.initialize(fileInputStream);
		}
		case PDFTRON: {
			yield PDFTronService.initialize(fileInputStream);
		}
		};

	}

}
