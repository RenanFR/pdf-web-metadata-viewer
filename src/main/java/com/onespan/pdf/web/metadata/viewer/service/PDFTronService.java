package com.onespan.pdf.web.metadata.viewer.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Element;
import com.pdftron.pdf.ElementReader;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.PageIterator;

public class PDFTronService implements AutoCloseable, PDFService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDFTronService.class);

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

	public PdfRestrictions getDocumentRestrictionSummary() throws Exception {
		return new PdfRestrictions(true, true, true);
	}

	public boolean isAdaCompliant() throws Exception {
		return doc.isTagged() && hasAllRequiredMetadata();
	}

	public boolean isValid() throws Exception {
		String pdfHeader = getPDFHeader();
		return pdfHeader != null && !pdfHeader.isBlank();
	}

	public Set<String> getFontList() throws Exception {
		Set<String> fontList = new HashSet<String>();
		iterateDocumentTextAndDo(el -> {
			try {
				fontList.add(el.getGState().getFont().getFamilyName());
			} catch (PDFNetException e) {
				LOGGER.error(e.getMessage());
			}
		});
		return fontList;

	}

	private void iterateDocumentTextAndDo(Consumer<Element> doWithText) throws PDFNetException {

		List<Page> pageList = new ArrayList<Page>();
		PageIterator pageIterator = doc.getPageIterator();
		ElementReader reader = new ElementReader();
		while (pageIterator.hasNext()) {
			Page page = pageIterator.next();
			pageList.add(page);
		}
		for (Page page : pageList) {

			reader.begin(page);
			Element element;
			while (Optional.ofNullable((element = reader.next())).isPresent()) {
				if (element.getType() == Element.e_text) {
					LOGGER.info(element.getTextString());
					doWithText.accept(element);

				}
			}
			reader.end();
		}
	}

	private boolean hasAllRequiredMetadata() throws Exception {
		return !doc.getDocInfo().getAuthor().isBlank() && !doc.getDocInfo().getKeywords().isBlank()
				&& !doc.getDocInfo().getTitle().isBlank() && !doc.getDocInfo().getSubject().isBlank();

	}

	public PDFTronService() {
	}

	private PDFTronService(InputStream fileInputStream) throws Exception {
		PDFNet.initialize(System.getenv("PDF_APRYSE_KEY"));
		try {
			doc = new PDFDoc(fileInputStream);
		} catch (PDFNetException exception) {
			System.err.println(exception.getMessage());
		}
	}

	public static PDFTronService initialize(InputStream fileInputStream) throws Exception {
		return new PDFTronService(fileInputStream);

	}

	@Override
	public void close() throws Exception {
		doc.close();
		PDFNet.terminate();
	}

	public PDFDoc getDoc() {
		return doc;
	}

	@Override
	public StringBuilder getDocumentRawText() throws Exception {
		StringBuilder documentText = new StringBuilder();
		iterateDocumentTextAndDo(el -> {
			try {
				documentText.append(el.getTextString());
			} catch (PDFNetException e) {
				LOGGER.error(e.getMessage());
			}
		});
		return documentText;
	}

}
