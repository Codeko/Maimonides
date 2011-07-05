package com.codeko.apps.maimonides.horarios.impresion;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.impresion.Impresion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;

/**
 *
 * @author codeko
 */
public class ImpresionHorarios extends MaimonidesBean {

    public void imprimirHorarios(final MaimonidesBean bean, ArrayList<HorarioImprimible> datos,boolean formatoMultiple) {
        if (bean != null) {
            this.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    bean.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
        }
        HorarioDataSource hds=new HorarioDataSource();
        hds.setHorarios(datos);
        HorarioDataSourceProvider pdsp = new HorarioDataSourceProvider(hds);
        imprimir(pdsp,formatoMultiple);
    }

    private void imprimir(HorarioDataSourceProvider pdsp,boolean formatoMultiple) {
        try {
            firePropertyChange("progress", 0, -1);
            firePropertyChange("message", null, "Generando plantilla...");
            String plantilla=Configuracion.CARPETA_INFORMES+"/horario.jrxml";
            if(formatoMultiple){
                plantilla=Configuracion.CARPETA_INFORMES+"/horario_multiple.jrxml";
            }
            JasperReport jasperReport = Impresion.getReport(plantilla);
            JRDataSource jrds = pdsp.create(jasperReport);
            firePropertyChange("message", null, "Rellenando datos...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), jrds);
            if (MaimonidesApp.getApplication().getConfiguracion().isImprimirEnPDF()) {
                firePropertyChange("message", null, "Generando PDF...");
                File salida = File.createTempFile("horario_", ".pdf");
                JasperExportManager.exportReportToPdfFile(jasperPrint, salida.getAbsolutePath());
                pdsp.dispose(jrds);
                Desktop.getDesktop().open(salida);
            } else {
                firePropertyChange("message", null, "Enviando datos a impresora...");
                JasperPrintManager.printReport(jasperPrint, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(ImpresionHorarios.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
