package com.onespan.pdf.web.metadata.viewer.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfEncryptor;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;

public class ItextPDFService implements AutoCloseable, PDFService {

	private PdfReader pdfReader;

	private ItextPDFService(InputStream fileInputStream) throws Exception {
		pdfReader = new PdfReader(fileInputStream);
	}

	public static ItextPDFService initialize(InputStream fileInputStream) throws Exception {

		return new ItextPDFService(fileInputStream);

	}

	@Override
	public long getPages() throws Exception {
		return pdfReader.getNumberOfPages();
	}

	@Override
	public String getPDFVersion() throws Exception {
		return String.valueOf(pdfReader.getPdfVersion());
	}

	@Override
	public boolean isValid() throws Exception {
		return true;
	}

	@Override
	public Set<String> getFontList() throws Exception {
		Set<String> fontList = new HashSet<String>();
		PdfObject pdfObject;
		PdfDictionary dictionary;
		for (int i = 0; i < pdfReader.getXrefSize(); i++) {
			pdfObject = pdfReader.getPdfObject(i);
			if (pdfObject == null || !pdfObject.isDictionary()) {
				continue;
			}
			dictionary = (PdfDictionary) pdfObject;
			if (dictionary.get(PdfName.FONTFAMILY) != null) {
				fontList.add(dictionary.getAsString(PdfName.FONTFAMILY).toString());
			}

		}
		return fontList;
	}

	@Override
	public boolean isAdaCompliant() throws Exception {
		return pdfReader.isTagged();
	}

	@Override
	public PdfRestrictions getDocumentRestrictionSummary() throws Exception {

		if (pdfReader.isEncrypted()) {

			int permissions = (int) pdfReader.getPermissions();
			return new PdfRestrictions(PdfEncryptor.isPrintingAllowed(permissions),
					PdfEncryptor.isCopyAllowed(permissions), PdfEncryptor.isModifyContentsAllowed(permissions));
		} else {
			return new PdfRestrictions(true, true, true);

		}
	}

	@Override
	public StringBuilder getDocumentRawText() throws Exception {
		PdfReaderContentParser contentParser = new PdfReaderContentParser(pdfReader);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(out);
		TextExtractionStrategy textExtractionStrategy;
		for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
			textExtractionStrategy = contentParser.processContent(i, new SimpleTextExtractionStrategy());
			writer.println(textExtractionStrategy.getResultantText());
		}
		writer.flush();
		writer.close();
		return new StringBuilder(out.toString());
	}

	@Override
	public void close() throws Exception {
		pdfReader.close();
	}

}
