/**
 *  Maimónides, gestión para centros escolares.
 *  Copyright Codeko and individual contributors
 *  as indicated by the @author tags.
 * 
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 * 
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *  
 *  For more information:
 *  maimonides@codeko.com
 *  http://codeko.com/maimonides
**/


package com.codeko.apps.maimonides.partes.cartas;

//No podemos trabajar con iText 5 porque jasper reports usa la version 2 que tienen rutas diferentes
//TODO Implementar un classloader propio o recompilar jasper reports con las rutas actualizadas
//import com.itextpdf.text.Document;
//import com.itextpdf.text.pdf.PRAcroForm;
//import com.itextpdf.text.pdf.PdfCopy;
//import com.itextpdf.text.pdf.PdfImportedPage;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.SimpleBookmark;
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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConcatenadorPDF {

    public static void concatenar(File destino, Collection<File> archivos) {
        try {
            int pageOffset = 0;
            ArrayList<HashMap<String,Object>> master = new ArrayList<HashMap<String,Object>>();
            int f = 0;
            Document document = null;
            PdfCopy writer = null;
            for (File arch : archivos) {
                PdfReader reader = new PdfReader(arch.toURI().toURL());

                reader.consolidateNamedDestinations();
                // we retrieve the total number of pages
                int n = reader.getNumberOfPages();
                List<HashMap<String,Object>> bookmarks = SimpleBookmark.getBookmark(reader);
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
