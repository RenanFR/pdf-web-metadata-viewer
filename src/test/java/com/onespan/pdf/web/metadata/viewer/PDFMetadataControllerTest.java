package com.onespan.pdf.web.metadata.viewer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@ContextConfiguration(classes = { PdfWebMetadataViewerApplication.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class PDFMetadataControllerTest {

	private static final String DEMO_PDF = "PB-91275 - Demo PDF.pdf";

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	public void shouldGoToPdfPageWithDataAfterUpload() throws Exception {
		final byte[] fileBytes = Files
				.readAllBytes(Paths.get(getClass().getClassLoader().getResource(DEMO_PDF).toURI()));
		MockMultipartFile mockMultipartFile = new MockMultipartFile("fileMultipart", DEMO_PDF, "application/pdf",
				fileBytes);

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		mockMvc.perform(multipart("/pdf").file(mockMultipartFile)).andExpect(status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfVersion", "1.7"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfValidity", "Sim"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfAda", "Nao"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfFonts",
						"(MinionPro-Regular, AdobeArabic-Bold, CourierStd, Symbol)"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfRestrictions",
						"Impressao: Permitido, Copia: Permitido, Modificacao: Permitido"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfPages", "1"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfSize", "36292 Bytes"))
				.andExpect(MockMvcResultMatchers.flash().attribute("pdfLanguage", "en"));
	}

}
