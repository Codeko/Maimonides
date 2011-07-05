package com.codeko.apps.maimonides.notificaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.partes.cartas.DatoCartaPerdidaEscolaridadPorMaterias;
import java.util.Collection;
import java.util.GregorianCalendar;
import javax.swing.JOptionPane;

/**
 *
 * @author codeko
 */
public class EnviarNotificacionesPerdidaEvaluacionContinuaMateriasTask extends org.jdesktop.application.Task<Boolean, Void> {

    GregorianCalendar fecha = null;
    Collection<DatoCartaPerdidaEscolaridadPorMaterias> datos = null;

    public EnviarNotificacionesPerdidaEvaluacionContinuaMateriasTask(org.jdesktop.application.Application app, GregorianCalendar fechaHasta, Collection<DatoCartaPerdidaEscolaridadPorMaterias> datos) {
        super(app);
        this.fecha = fechaHasta;
        this.datos = datos;
    }

    @Override
    protected Boolean doInBackground() {
        setMessage("Enviando...");
        setProgress(0, 0, datos.size());
        GestorNotificaciones gestor = new GestorNotificaciones();
        return gestor.enviarNotificacionesPerdidaEvaluacionContinuaMaterias(fecha, datos);
    }

    @Override
    protected void succeeded(Boolean result) {
        if (result) {
            setMessage("Cartas impresas/enviadas correctamente.");
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Cartas impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            setMessage("Ha habido algún error enviando las notificaciones.");
        }
        MaimonidesUtil.ejecutarTask(this, "actualizar");
    }
}
