package com.onespan.pdf.web.metadata.viewer;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.onespan.pdf.web.metadata.viewer.model.PdfLibrary;
import com.onespan.pdf.web.metadata.viewer.service.PDFService;

@RunWith(SpringJUnit4ClassRunner.class)
public class PDFTronServiceTest {
	
	InputStream demoPdfInputStream;
	PDFService pdfService;
	
	@Before
	public void setup() throws Exception {
		demoPdfInputStream = getClass().getClassLoader().getResourceAsStream("PB-91275 - Demo PDF.pdf");
		pdfService = PDFService.getByLibrary(PdfLibrary.PDFTRON, demoPdfInputStream, null, null);
	}

	@Test
	public void shouldGetNumberOfPagesFromDocument() throws Exception {
		assertEquals(1L, pdfService.getPages());
	}
	
	@Test
	public void shouldGetDocumentPDFVersion() throws Exception {
		assertEquals("1.7", pdfService.getPDFVersion());

	}

}
