package com.onespan.pdf.web.metadata.viewer;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.onespan.pdf.web.metadata.viewer.model.PdfLibrary;
import com.onespan.pdf.web.metadata.viewer.service.PDFService;
import com.onespan.pdf.web.metadata.viewer.util.DocumentLanguageDetector;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentLanguageDetectorTest {

	StringBuilder documentText;

	@Before
	public void setup() throws Exception {
		InputStream demoPdfInputStream = getClass().getClassLoader().getResourceAsStream("PB-91275 - Demo PDF.pdf");
		PDFService pdfService = PDFService.getByLibrary(PdfLibrary.PDFTRON, demoPdfInputStream, null, null);
		documentText = pdfService.getDocumentRawText();
	}

	@Test
	public void shouldRecognizeLanguageWithinSampleDocument() throws Exception {
		assertEquals("en", DocumentLanguageDetector.getLanguageFromDocumentText(documentText));
	}

}
