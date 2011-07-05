package com.codeko.apps.maimonides.partes.cartas;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ConcatenadorPDF {
    //TODO Migrar a iText 5
    @SuppressWarnings("unchecked")
    public static void concatenar(File destino, Collection<File> archivos) {
        try {
            int pageOffset = 0;
            ArrayList master = new ArrayList();
            int f = 0;
            Document document = null;
            PdfCopy writer = null;
            for (File arch : archivos) {
                PdfReader reader = new PdfReader(arch.toURI().toURL());

                reader.consolidateNamedDestinations();
                // we retrieve the total number of pages
                int n = reader.getNumberOfPages();
                List bookmarks = SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0) {
                        SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                    }
                    master.addAll(bookmarks);
                }
                pageOffset += n;

                if (f == 0) {
                    // step 1: creation of a document-object
                    document = new Document(reader.getPageSizeWithRotation(1));
                    // step 2: we create a writer that listens to the document
                    writer = new PdfCopy(document, new FileOutputStream(destino));
                    // step 3: we open the document
                    document.open();
                }
                // step 4: we add content
                PdfImportedPage page;
                for (int i = 0; i < n;) {
                    ++i;
                    page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
                PRAcroForm form = reader.getAcroForm();
                if (form != null) {
                    writer.copyAcroForm(reader);
                }
                f++;
            }
            if (!master.isEmpty()) {
                writer.setOutlines(master);
            }
            // step 5: we close the document
            if (document != null) {
                document.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(ConcatenadorPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
