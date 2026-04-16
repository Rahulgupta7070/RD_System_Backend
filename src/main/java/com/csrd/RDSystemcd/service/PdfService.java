package com.csrd.RDSystemcd.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.csrd.RDSystemcd.entity.RdPassbook;
import com.csrd.RDSystemcd.entity.RdUser;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class PdfService {

    public byte[] generatePdf(RdUser user,
                             List<RdPassbook> list,
                             double total,
                             double maturity) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // ================= BORDER =================
        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
        canvas.setLineWidth(2);
        canvas.rectangle(20, 20, 555, 802);
        canvas.stroke();

        // ================= HEADER (COLOR) =================
        Paragraph header = new Paragraph("RD FINANCE SYSTEM")
                .setFontSize(18)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);

        Div headerBox = new Div()
                .setBackgroundColor(ColorConstants.BLUE)
                .setPadding(10)
                .add(header);

        doc.add(headerBox);

        doc.add(new Paragraph("Meerut Branch").setFontSize(10));
        doc.add(new Paragraph("Date: " + java.time.LocalDate.now())
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10));

        doc.add(new Paragraph("\n"));

        // ================= USER INFO =================
        doc.add(new Paragraph("Name: " + user.getName()).setBold());
        doc.add(new Paragraph("Account No: " + user.getAccountNumber()));
        doc.add(new Paragraph("Plan: " + user.getTotalMonths() + " Months"));

        doc.add(new Paragraph("\n"));

        // ================= SUMMARY BOX =================
        Div summaryBox = new Div()
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.GRAY, 1))
                .setPadding(10);

        summaryBox.add(new Paragraph("Summary").setBold());
        summaryBox.add(new Paragraph("Total Deposit: Rs. " + total));
        summaryBox.add(new Paragraph("Interest: Rs. " + String.format("%.2f", (maturity - total))));
        summaryBox.add(new Paragraph("Maturity: Rs. " + String.format("%.2f", maturity)));
        summaryBox.add(new Paragraph("Fine: Rs. 0"));

        doc.add(summaryBox);

        doc.add(new Paragraph("\n"));

        // ================= TABLE =================
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3, 2, 2}))
                .useAllAvailableWidth();

        // HEADER COLOR
        table.addHeaderCell(new Cell().add(new Paragraph("S.No")).setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE));
        table.addHeaderCell(new Cell().add(new Paragraph("Date")).setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE));
        table.addHeaderCell(new Cell().add(new Paragraph("Amount")).setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE));
        table.addHeaderCell(new Cell().add(new Paragraph("Status")).setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE));
        table.addHeaderCell(new Cell().add(new Paragraph("Fine")).setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE));

        int i = 1;
        for (RdPassbook p : list) {

            table.addCell(String.valueOf(i++));
            table.addCell(p.getRdDate().format(formatter));
            table.addCell(p.getRdAmount().toString());
            table.addCell(p.getStatus());
            table.addCell(String.valueOf(
                    p.getFineAmount() == null ? 0 : p.getFineAmount()
            ));
        }

        doc.add(table);

        doc.add(new Paragraph("\n"));

        // ================= SIGNATURE =================
        doc.add(new Paragraph("Authorized Signature").setBold());
        doc.add(new Paragraph("________________________"));

        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("This is a system generated document")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY));

        doc.close();

        return out.toByteArray();
    }
}