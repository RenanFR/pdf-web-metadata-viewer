package com.onespan.pdf.web.metadata.viewer.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFNet;

@Service
public class PDFApryseService implements AutoCloseable {

	private PDFDoc doc;

	public long getPages() throws Exception {
		return doc.getPageCount();
	}

	public String getPDFVersion() throws Exception {
		String pdfHeader = getPDFHeader();
		return pdfHeader.split("-")[1];
	}

	private String getPDFHeader() throws PDFNetException {
		return doc.getPage(1).getResourceDict().getDoc().getHeader();
	}

	public boolean isAdaCompliant() throws Exception {
		return doc.isTagged() && hasAllRequiredMetadata();
	}

	public boolean isValid() throws Exception {
		String pdfHeader = getPDFHeader();
		return pdfHeader != null && !pdfHeader.isBlank();
	}

	private boolean hasAllRequiredMetadata() throws Exception {
		return !doc.getDocInfo().getAuthor().isBlank() && !doc.getDocInfo().getKeywords().isBlank()
				&& !doc.getDocInfo().getTitle().isBlank() && !doc.getDocInfo().getKeywords().isBlank()
				&& !doc.getDocInfo().getSubject().isBlank();

	}

	public PDFApryseService() {
	}

	private PDFApryseService(InputStream fileInputStream) {
		PDFNet.initialize(System.getenv("PDF_APRYSE_KEY"));
		try {
			doc = new PDFDoc(fileInputStream);
		} catch (IOException | PDFNetException exception) {
			System.err.println(exception.getMessage());
		}
	}

	public static PDFApryseService initialize(InputStream fileInputStream) {
		return new PDFApryseService(fileInputStream);

	}

	@Override
	public void close() throws Exception {
		doc.close();
		PDFNet.terminate();
	}

	public PDFDoc getDoc() {
		return doc;
	}

}
