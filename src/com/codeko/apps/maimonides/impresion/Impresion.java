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
package com.codeko.apps.maimonides.impresion;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.impresion.informes.Informes;
import com.codeko.apps.maimonides.impresion.informes.custom.CustomInformes;
import com.codeko.apps.maimonides.partes.ParteDataSourceProvider;
import com.codeko.apps.maimonides.partes.ParteGenericoDataSourceProvider;
import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Impresion extends MaimonidesBean {

    private static Impresion impresion = null;

    private Impresion() {
    }

    public synchronized static Impresion getImpresion() {
        if (impresion == null) {
            impresion = new Impresion();
        }
        return impresion;
    }

    public void imprimirPartes(final MaimonidesBean bean, AnoEscolar anoEscolar, GregorianCalendar fecha) {
        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                bean.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        ParteDataSourceProvider pdsp = new ParteDataSourceProvider(anoEscolar, fecha, this);
        firePropertyChange("message", null, "Cargando datos de partes...");
        pdsp.setAplicarFiltrosImpresion(true);
        pdsp.cargarPartes();
        imprimir(pdsp);
    }

    public void imprimirPartes(final MaimonidesBean bean, AnoEscolar anoEscolar, GregorianCalendar fecha, String curso) {
        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                bean.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        ParteDataSourceProvider pdsp = new ParteDataSourceProvider(anoEscolar, fecha, this);
        firePropertyChange("message", null, "Cargando datos de partes...");
        pdsp.setAplicarFiltrosImpresion(true);
        pdsp.cargarPartes(curso);
        imprimir(pdsp);
    }

    public void imprimirPartes(final MaimonidesBean bean, AnoEscolar anoEscolar, GregorianCalendar fecha, ParteFaltas parte) {
        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                bean.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        ParteDataSourceProvider pdsp = new ParteDataSourceProvider(anoEscolar, fecha, this);
        firePropertyChange("message", null, "Cargando datos de partes...");
        pdsp.cargarPartes(parte);
        imprimir(pdsp);
    }

    public void imprimirPartesGenericos(final MaimonidesBean bean, AnoEscolar anoEscolar, GregorianCalendar fecha, Collection<Unidad> unidades) {
        if (unidades.size() > 0) {
            this.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    bean.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            ParteGenericoDataSourceProvider pdsp = new ParteGenericoDataSourceProvider(anoEscolar, fecha, this, unidades);
            firePropertyChange("message", null, "Cargando datos de partes...");
            imprimir(pdsp);
        } else {
            firePropertyChange("message", null, "No hay datos que imprimir.");
        }
    }

    private void imprimir(ParteGenericoDataSourceProvider pdsp) {
        try {
            firePropertyChange("progress", 0, -1);
            firePropertyChange("message", null, "Generando plantilla...");
            JasperReport jasperReport = Impresion.getReport("parte_generico.jrxml");
            JRDataSource jrds = pdsp.create(jasperReport);
            firePropertyChange("message", null, "Rellenando datos...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<Object,Object>(), jrds);
            if (MaimonidesApp.getApplication().getConfiguracion().isImprimirEnPDF()) {
                firePropertyChange("message", null, "Generando PDF...");
                File salida = File.createTempFile("partes_", ".pdf");
                JasperExportManager.exportReportToPdfFile(jasperPrint, salida.getAbsolutePath());
                pdsp.dispose(jrds);
                Desktop.getDesktop().open(salida);
            } else {
                firePropertyChange("message", null, "Enviando datos a impresora...");
                JasperPrintManager.printReport(jasperPrint, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(Impresion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void imprimir(ParteDataSourceProvider pdsp) {
        try {
            firePropertyChange("progress", 0, -1);
            firePropertyChange("message", null, "Generando plantilla...");
            JasperReport jasperReport = Impresion.getReport("parte.jrxml");
            JRDataSource jrds = pdsp.create(jasperReport);
            firePropertyChange("message", null, "Rellenando datos...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<Object,Object>(), jrds);
            if (MaimonidesApp.getApplication().getConfiguracion().isImprimirEnPDF()) {
                firePropertyChange("message", null, "Generando PDF...");
                File salida = File.createTempFile("partes_", ".pdf");
                JasperExportManager.exportReportToPdfFile(jasperPrint, salida.getAbsolutePath());
                pdsp.dispose(jrds);
                Desktop.getDesktop().open(salida);
            } else {
                firePropertyChange("message", null, "Enviando datos a impresora...");
                JasperPrintManager.printReport(jasperPrint, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(Impresion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static JasperReport getReport(String name) throws JRException {
        return JasperCompileManager.compileReport(getResource(name));
    }

    private static InputStream readResource(File f) {
        InputStream res = null;
        if (f != null && f.exists() && f.canRead()) {
            try {
                res = new FileInputStream(f);
            } catch (IOException ex) {
                Logger.getLogger(Impresion.class.getName()).log(Level.SEVERE, "Error leyendo " + f, ex);
            }
        }
        return res;
    }

    public static InputStream getResource(String name) {
        InputStream res = null;
        //Vemos si existe en la carpeta configurada
        res=readResource(new File(MaimonidesApp.getApplication().getConfiguracion().getCarpetaTemplates(), name));
        if (res == null) {
            //Vemos si existe en la carpeta de usuario
            res=readResource(new File(Configuracion.getSubCarpertaUsuarioMaimonides(Configuracion.CARPETA_INFORMES), name));
        }
        if (res == null) {
            //Buscamos en la carpeta de instalacion
            res=readResource(new File(Configuracion.CARPETA_INFORMES, name));
        }
        if (res == null) {
            //Buscamos en el los informes propios dentro del jar (por si se ha distribuido un jar con 
            //informes personalizados)
            res = CustomInformes.class.getResourceAsStream(name);
        }
        //Finalmente cogemos los propios de la aplicacion
        if (res == null) {
            res = Informes.class.getResourceAsStream(name);
        }
        return res;
    }
}
