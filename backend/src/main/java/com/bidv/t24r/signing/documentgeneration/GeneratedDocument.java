package com.bidv.t24r.signing.documentgeneration;

import java.util.List;

public record GeneratedDocument(
        byte[] pdfBytes,
        byte[] jsonBytes,
        byte[] xmlBytes,
        List<String> pdfLines
) {
}
