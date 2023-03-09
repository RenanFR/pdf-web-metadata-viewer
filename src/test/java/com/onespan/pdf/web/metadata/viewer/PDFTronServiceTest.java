package com.onespan.pdf.web.metadata.viewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.onespan.pdf.web.metadata.viewer.model.PdfLibrary;
import com.onespan.pdf.web.metadata.viewer.model.PdfRestrictions;
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
	public void shouldKnowDocumentIsValidPdf() throws Exception {
		assertTrue(pdfService.isValid());
	}

	@Test
	public void shouldKnowSampleDocumentIsNotAdaCompliant() throws Exception {
		assertFalse(pdfService.isAdaCompliant());
	}

	@Test
	public void shouldGetNumberOfPagesFromDocument() throws Exception {
		assertEquals(1L, pdfService.getPages());
	}

	@Test
	public void shouldGetDocumentPDFVersion() throws Exception {
		assertEquals("1.7", pdfService.getPDFVersion());
	}

	@Test
	public void shouldGetDocumentText() throws Exception {
		String documentRawText = """
				PDF for PB-91275This PDF is going to be used for PB-91275 demo. \
				This demo:•Must have:oExtract Information about Text Fields (Size of the Field,Font Name, Font Size)\
				oReplace a Text on the Text FieldoInject a Signature Field.oSigning one Signature.\
				•Good to Have:a.PDF validation (it is a PDF file, is a valid structure)b.\
				Verify if the document is encryptedc.Extract signatures informationd.\
				Replace a Text from a ParagraphThis is a new paragraph to be replaced. \
				It has a different fontText field that will have information extracted:\
				Text field that needs to be edited:Inject signature field:\
				Signature to be signed:Signature already signed:""";
		assertEquals(documentRawText, pdfService.getDocumentRawText().toString());
	}

	@Test
	public void shouldGetDocumentFontList() throws Exception {
		String fontList = pdfService.getFontList().stream().collect(Collectors.joining(", ", "(", ")"));
		assertEquals("(MinionPro-Regular, AdobeArabic-Bold, CourierStd, Symbol)", fontList);
	}

	@Test
	public void shouldGetDocumentRestriction() throws Exception {
		PdfRestrictions documentRestriction = pdfService.getDocumentRestrictionSummary();
		assertTrue(documentRestriction.isPrintingAllowed());
		assertTrue(documentRestriction.isCopyAllowed());
		assertTrue(documentRestriction.isModifyContentsAllowed());
	}
}
