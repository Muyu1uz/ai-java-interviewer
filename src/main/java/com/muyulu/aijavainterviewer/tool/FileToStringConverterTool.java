package com.muyulu.aijavainterviewer.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson. annotation.JsonPropertyDescription;
import lombok.extern.slf4j. Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image. BufferedImage;
import java. io.ByteArrayInputStream;
import java. io.File;
import java.io.IOException;
import java.io. InputStream;
import java.net. URL;
import java.nio. file.Files;
import java. nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util. function.Function;

@Component
@Slf4j
@Description("将PDF或图像文件转换为文本字符串，支持本地文件路径、URL和MultipartFile上传")
public class FileToStringConverterTool implements Function<FileToStringConverterTool.Request, String> {

    private final Tesseract tesseract;

    public FileToStringConverterTool(
            @Value("${tesseract.datapath:}") String tessDataPath,
            @Value("${tesseract.language:eng}") String tessLanguage
    ) {
        this.tesseract = new Tesseract();
        if (tessDataPath != null && !tessDataPath.isBlank()) {
            this.tesseract.setDatapath(tessDataPath);
        }
        if (tessLanguage != null && ! tessLanguage.isBlank()) {
            this.tesseract.setLanguage(tessLanguage);
        }
    }

    public record Request(
            @JsonProperty(value = "filePath")
            @JsonPropertyDescription("文件路径或URL，支持PDF、PNG、JPG、JPEG格式（当multipartFile为null时使用）")
            String filePath,

            @JsonProperty(value = "multipartFile")
            @JsonPropertyDescription("上传的文件对象，优先级高于filePath")
            MultipartFile multipartFile,

            @JsonProperty(value = "extractionType")
            @JsonPropertyDescription("提取类型：TEXT(纯文本)、MARKDOWN(保留格式)、OCR(图像识别)")
            String extractionType
    ) {}

    @Override
    public String apply(Request request) {
        MultipartFile multipartFile = request. multipartFile();
        String filePath = request.filePath();
        String extractionType = request. extractionType() != null ? request.extractionType().toUpperCase() : "TEXT";

        if (multipartFile != null && !multipartFile.isEmpty()) {
            log.info("Converting uploaded file to string: {} with type: {}", multipartFile.getOriginalFilename(), extractionType);
            return convertMultipartFileToString(multipartFile, extractionType);
        } else if (filePath != null && !filePath.isBlank()) {
            log.info("Converting file to string: {} with type: {}", filePath, extractionType);
            return file2Content(filePath, extractionType);
        } else {
            return "请提供文件路径或上传文件";
        }
    }

    // 新增：处理MultipartFile的方法
    public String convertMultipartFileToString(MultipartFile multipartFile, String extractionType) {
        try {
            String normalizedType = extractionType != null ? extractionType.toUpperCase() : "TEXT";
            String fileName = multipartFile.getOriginalFilename();
            String extension = getFileExtension(fileName != null ? fileName : "").toLowerCase();

            return switch (extension) {
                case "pdf" -> convertMultipartPdfToString(multipartFile, normalizedType);
                case "png", "jpg", "jpeg", "gif", "bmp" -> convertMultipartImageToString(multipartFile, normalizedType);
                default -> "不支持的文件格式: " + extension;
            };
        } catch (Exception e) {
            log.error("上传文件转换失败", e);
            return "文件转换失败: " + e.getMessage();
        }
    }

    // 新增：处理MultipartFile PDF转换
    private String convertMultipartPdfToString(MultipartFile multipartFile, String extractionType) throws IOException {
        if ("OCR".equals(extractionType)) {
            return convertMultipartPdfUsingOcr(multipartFile);
        }
        return convertMultipartPdfUsingPdfBox(multipartFile, extractionType);
    }

    // 新增：处理MultipartFile图像转换
    private String convertMultipartImageToString(MultipartFile multipartFile, String extractionType) throws IOException {
        if ("OCR".equals(extractionType)) {
            return convertMultipartImageUsingOcr(multipartFile);
        } else if ("TEXT".equals(extractionType) || "MARKDOWN".equals(extractionType)) {
            return convertMultipartImageToBase64(multipartFile);
        } else {
            return "图像文件需要OCR才能提取文本内容";
        }
    }

    // 新增：MultipartFile PDF OCR处理
    private String convertMultipartPdfUsingOcr(MultipartFile multipartFile) throws IOException {
        try (PDDocument document = PDDocument.load(multipartFile. getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder result = new StringBuilder();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, 300);
                result.append(executeOcr(pageImage)).append("\n\n");
            }
            return result.toString();
        } catch (TesseractException e) {
            throw new IOException("PDF OCR失败", e);
        }
    }

    // 新增：MultipartFile图像OCR处理
    private String convertMultipartImageUsingOcr(MultipartFile multipartFile) throws IOException {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IOException("无法读取图像内容");
            }
            return executeOcr(image);
        } catch (TesseractException e) {
            throw new IOException("图像OCR失败", e);
        }
    }

    // 新增：MultipartFile PDF文本提取
    private String convertMultipartPdfUsingPdfBox(MultipartFile multipartFile, String extractionType) throws IOException {
        try (PDDocument document = PDDocument.load(multipartFile.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();

            if ("MARKDOWN".equals(extractionType)) {
                stripper.setSortByPosition(true);
                stripper.setLineSeparator("\n");
            }

            return stripper.getText(document);
        }
    }

    // 新增：MultipartFile图像转Base64
    private String convertMultipartImageToBase64(MultipartFile multipartFile) throws IOException {
        byte[] imageBytes = multipartFile.getBytes();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = multipartFile.getContentType();

        if (mimeType == null || mimeType.isBlank()) {
            mimeType = getMimeType(multipartFile.getOriginalFilename());
        }

        return String.format("data:%s;base64,%s", mimeType, base64);
    }

    // 保留原有的文件路径处理方法
    public String file2Content(String filePath, String extractionType) {
        try {
            String normalizedType = extractionType != null ?  extractionType.toUpperCase() : "TEXT";
            String extension = getFileExtension(filePath).toLowerCase();
            return switch (extension) {
                case "pdf" -> convertPdfToString(filePath, normalizedType);
                case "png", "jpg", "jpeg", "gif", "bmp" -> convertImageToString(filePath, normalizedType);
                default -> "不支持的文件格式: " + extension;
            };
        } catch (Exception e) {
            log.error("文件转换失败", e);
            return "文件转换失败: " + e.getMessage();
        }
    }

    private String convertPdfToString(String filePath, String extractionType) throws IOException {
        if ("OCR".equals(extractionType)) {
            return convertPdfUsingOcr(filePath);
        }
        return convertPdfUsingPdfBox(filePath, extractionType);
    }

    private String convertImageToString(String filePath, String extractionType) throws IOException {
        if ("OCR".equals(extractionType)) {
            return convertImageUsingOcr(filePath);
        } else if ("TEXT".equals(extractionType) || "MARKDOWN".equals(extractionType)) {
            return convertImageToBase64(filePath);
        } else {
            return "图像文件需要OCR才能提取文本内容";
        }
    }

    private String convertPdfUsingOcr(String filePath) throws IOException {
        try (PDDocument document = loadPdfDocument(filePath)) {
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder result = new StringBuilder();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, 300);
                result.append(executeOcr(pageImage)).append("\n\n");
            }
            return result.toString();
        } catch (TesseractException e) {
            throw new IOException("PDF OCR失败", e);
        }
    }

    private String convertImageUsingOcr(String filePath) throws IOException {
        BufferedImage image;
        if (isUrl(filePath)) {
            try (InputStream inputStream = new URL(filePath).openStream()) {
                image = ImageIO.read(inputStream);
            }
        } else {
            image = ImageIO.read(new File(filePath));
        }
        if (image == null) {
            throw new IOException("无法读取图像内容");
        }
        try {
            return executeOcr(image);
        } catch (TesseractException e) {
            throw new IOException("图像OCR失败", e);
        }
    }

    private String executeOcr(BufferedImage image) throws TesseractException {
        synchronized (tesseract) {
            return tesseract.doOCR(image);
        }
    }

    private String convertPdfUsingPdfBox(String filePath, String extractionType) throws IOException {
        try (PDDocument document = loadPdfDocument(filePath)) {
            PDFTextStripper stripper = new PDFTextStripper();

            if ("MARKDOWN".equals(extractionType)) {
                stripper.setSortByPosition(true);
                stripper.setLineSeparator("\n");
            }

            return stripper.getText(document);
        }
    }

    private String convertImageToBase64(String filePath) throws IOException {
        byte[] imageBytes;

        if (isUrl(filePath)) {
            imageBytes = downloadFileFromUrl(filePath);
        } else {
            imageBytes = Files.readAllBytes(Paths.get(filePath));
        }

        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = getMimeType(filePath);

        return String.format("data:%s;base64,%s", mimeType, base64);
    }

    private PDDocument loadPdfDocument(String filePath) throws IOException {
        if (isUrl(filePath)) {
            byte[] pdfBytes = downloadFileFromUrl(filePath);
            return PDDocument.load(pdfBytes);
        } else {
            return PDDocument. load(new File(filePath));
        }
    }

    private byte[] downloadFileFromUrl(String url) throws IOException {
        try (InputStream inputStream = new URL(url).openStream()) {
            return inputStream. readAllBytes();
        }
    }

    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "";
        }
        int lastDotIndex = filePath.lastIndexOf('.');
        return lastDotIndex > 0 ? filePath. substring(lastDotIndex + 1) : "";
    }

    private boolean isUrl(String path) {
        return path != null && (path.startsWith("http://") || path.startsWith("https://"));
    }

    private String getMimeType(String filePath) {
        if (filePath == null) {
            return "application/octet-stream";
        }
        String extension = getFileExtension(filePath).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}