package com.smartcontactmanager.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfService {
    
    private Logger logger = LoggerFactory.getLogger(PdfService.class);

    public ByteArrayInputStream createPdf() {

        logger.info("Create PDF Started....");

        String title = "Welcom to Smart Contact Manager app";
        String content = "This pltform provide the cloud space to save your conact details and better Management.";

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, os);

            // Adding PDF footer
            HeaderFooter footer = new HeaderFooter(true, new Phrase("www.SCM.com"));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorderWidthBottom(0);
            document.setFooter(footer);

            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,20);
            Paragraph titlePara = new Paragraph(title,titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);

            Font paraFont = FontFactory.getFont(FontFactory.HELVETICA,15);
            Paragraph paragraph = new Paragraph(content);
            paragraph.add(new Chunk("Ummmm, Today was so hectic day."));
            document.add(paragraph);

            document.close();

            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        }
    }
}
