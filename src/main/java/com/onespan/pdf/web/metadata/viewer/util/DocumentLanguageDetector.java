package com.onespan.pdf.web.metadata.viewer.util;

import java.io.IOException;

import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

public class DocumentLanguageDetector {

	public static String getLanguageFromDocumentText(StringBuilder documentText) throws IOException {
		LanguageResult detectLanguageResult = LanguageDetector.getDefaultLanguageDetector().loadModels()
				.detect(documentText.toString());

		String language = detectLanguageResult.getLanguage();
		return language;
	}

}
