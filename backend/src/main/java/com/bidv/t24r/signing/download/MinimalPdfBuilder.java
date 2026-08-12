package com.bidv.t24r.signing.download;

import com.bidv.t24r.signing.record.SignedRecord;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MinimalPdfBuilder {
    private MinimalPdfBuilder() {
    }

    static byte[] build(SignedRecord record) {
        return build("T24R Signed Record", List.of(
                "Business key: " + record.getBusinessKey(),
                "Customer: " + record.getCustomerId(),
                "Account: " + record.getAccountNo(),
                "Payload hash: " + record.getPayloadHash(),
                "Certificate: " + record.getCertificateSerial(),
                "Signed at: " + record.getSignedAt()
        ));
    }

    public static byte[] build(String title, List<String> lines) {
        StringBuilder stream = new StringBuilder("BT\n/F1 12 Tf\n72 760 Td\n");
        stream.append("(").append(pdfEscape(title)).append(") Tj\n0 -24 Td\n");
        for (String line : lines) {
            stream.append("(").append(pdfEscape(line)).append(") Tj\n0 -18 Td\n");
        }
        stream.append("ET\n");
        String content = stream.toString();

        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");
        objects.add("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        objects.add("5 0 obj\n<< /Length " + content.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + content + "endstream\nendobj\n");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (String object : objects) {
            offsets.add(pdf.length());
            pdf.append(object);
        }
        int xrefOffset = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Root 1 0 R /Size ").append(objects.size() + 1).append(" >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static String pdfEscape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
