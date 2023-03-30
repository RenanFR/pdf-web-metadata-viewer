# Pdf Web Metadata Viewer

The project was created using [Bootify](https://bootify.io/) which is an online tool that helps with the quick start of Spring based projects. It provides automatic setup for things like frontend structure and other dependencies

## Frontend
The frontend base layout was generated using thymeleaf (Configuration located at the `LocalDevConfig` class)

```java
    @SneakyThrows
    public LocalDevConfig(final TemplateEngine templateEngine) {
        File sourceRoot = new ClassPathResource("application.yml").getFile().getParentFile();
        while (sourceRoot.listFiles((dir, name) -> name.equals("mvnw")).length != 1) {
            sourceRoot = sourceRoot.getParentFile();
        }
        final FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setPrefix(sourceRoot.getPath() + "/src/main/resources/templates/");
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setCheckExistence(true);

        templateEngine.setTemplateResolver(fileTemplateResolver);
    }
```

The `layout.html` inside the templates provides the base structure for the other pages. It has a div with the content identifier in which the specific pages html are included

```html
<div layout:fragment="content" />
```

It also has the section where the error and success alerts are displayed for the user using the variables that are set in the Controller

```html
<p th:if="${MSG_SUCCESS}" th:text="${MSG_SUCCESS}"
	class="alert alert-success mb-4" role="alert" />
<p th:if="${MSG_ERROR}" th:text="${MSG_ERROR}"
	class="alert alert-danger mb-4" role="alert" />
```

The internationalization feature is also enabled, I included Portuguese and English and the language can be changed using the lang parameter in the URL. For this to work, most of the application text was included using both languages in each `messages.properties` files and instead of hardcoded text the thymeleaf pages will reference the respective label in the properties to able to display at different languages

## Backend
The main application page has a file input in order to receive the pdf or docx document. After selecting the document file  the submit button will be enabled using a javascript snippet in the layout file. The form has also a option to choose the library from which the metadata will be extracted and the user can choose iText or PDFTron

```html
<input class="form-control" type="file" id="pdf_file"
	name="fileMultipart" th:onchange="enableSubmitIfFileWasSelected()">
```

```javascript
function enableSubmitIfFileWasSelected() {
	var pdfFileInput = document.getElementById('pdf_file');
	var submitPdfButton = document.getElementById('submit_pdf');
	if (pdfFileInput.files[0]) {
		submitPdfButton.disabled = false;
	} else {
		submitPdfButton.disabled = true;
	}
}
```

After clicking the analyze button the `PDFMetadataController` will trigger the `receiveAndAnalysePdf` method that receives the MultipartFile representing the document. In this method the user Locale will be retrieved in order to identify in which language the dynamic messages will be displayed. The next step is to call the service layer, the first step here is retrieve the extension of the file submitted by the user (We are accepting only pdf and docx), if the document contains an unaccepted extension an exception will be throw, this exception is intercepted by an `ExceptionHandler` annotated method that will send the error message using the MSG_ERROR variable (That if present is displayed in the page as a red alert label)

```java
	@PostMapping("/pdf")
	public String receiveAndAnalysePdf(@RequestParam MultipartFile fileMultipart, String libraryName,
			RedirectAttributes redirectAttributes, @RequestParam(required = false) String lang) throws Exception {

		Locale locale = lang != null ? new Locale(lang) : Locale.US;

		pdfMetadataService.process(fileMultipart, libraryName, locale,
				getAction(fileMultipart, redirectAttributes, locale));

		return "redirect:/pdf";
	}
```

The service layer interacts with the `PDFService` interface that was implemented for both iText and PDFTron libraries, the right option is selected by the class using the library parameter that was chosen by the user. It then calls the `getMetadataFromService` method that will create a `PDFMetadata` object including each metadata attribute provided by the library according to the following

- PDF version
- Number of pages in the document
- The list of Fonts used in the document text
- If the PDF is Ada compliant
  - I am verifying this based on the requirement I read in some articles that explain what is required to consider a document Ada compliant
  - For PDFTron I am able to check if the document is Tagged and has all the required Metadata (Author, Keywords, Title and Subject) 
  - For Itext I still did not found in the document how to check all of this with the SDK 
- The Pdf document permissions
  - For PDFTron there is a bug where the object responsible for fetching this information is returning null
- The language used in the document
  - For this I am using the Apache Tika library class that is able to detect the language by the document text (While the code to retrieve all the document text is PDF library dependent)

For the Docx conversion it is only currently available using PDFTron (Because for Itext it is required to purchase an Add-on). In this case if the user submit a Word document a temporary file will be created and based on this file the constructor of the PDFTron service class that receives the Word file path will be called and will create a new Pdf using the Convert class

The REST layer uses basically the same code structure but instead of adding attributes and return to the pdf page it will fetch the document metadata in the json format

```java
	@PostMapping("/pdf")
	ResponseEntity<PDFMetadata> getRawMetadata(@RequestParam MultipartFile fileMultipart, String libraryName)
			throws Exception {
		PDFMetadata metadata = pdfMetadataService.process(fileMultipart, libraryName, Locale.US,
				UnaryOperator.identity());
		return ResponseEntity.ok(metadata);
	}
```

The application has unit tests for each feature at both libraries implementation and also coverage for the Web and REST layers 

Most of the tests were done using the sample PDF used in library study tasks from the previous sprints so there is probably some bugs and many specific scenarios using different documents to cover
