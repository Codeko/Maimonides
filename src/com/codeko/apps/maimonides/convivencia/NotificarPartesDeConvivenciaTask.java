package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.cartero.CarteroAlumno;
import com.codeko.util.Str;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 * @author codeko
 */
public class NotificarPartesDeConvivenciaTask extends org.jdesktop.application.Task<Boolean, Void> {

    ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();

    public NotificarPartesDeConvivenciaTask(org.jdesktop.application.Application app, ArrayList<ParteConvivencia> partes) {
        super(app);
        this.partes = partes;
        //Ahora vemos si ya hay partes notificados
        ArrayList<ParteConvivencia> notificados = new ArrayList<ParteConvivencia>();
        for (ParteConvivencia p : partes) {
            if (p.isNotificadoTutores()) {
                notificados.add(p);
            }
        }
        if (!notificados.isEmpty()) {
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Ha seleccionado " + notificados.size() + " partes ya notificados a los Padres/Tutores.\n¿Desea notificar estos partes de todas formas?", "¿Notificar partes ya notificados?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.NO_OPTION) {
                //Eliminamos los partes notificados
                partes.removeAll(notificados);
            } else if (op != JOptionPane.YES_OPTION) {
                partes.clear();
                cancel(false);
            }
        }
    }

    @Override
    protected Boolean doInBackground() {
        if (!partes.isEmpty()) {
            setMessage("Enviando/Imprimiendo notificaciones...");
            setProgress(0, 0, partes.size());
            CarteroAlumno<ParteConvivencia> cartero = new CarteroAlumno<ParteConvivencia>("partes de convivencia", Carta.TIPO_CARTA_PARTE_CONVIVENCIA) {

                @Override
                protected void addDatosExtra(Map<String, Object> data, ParteConvivencia p, Carta carta) {
                    p.addDatosExtraImpresion(data, carta);
                }
            };
            cartero.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    if ("error".equals(evt.getPropertyName())) {
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), Str.noNulo(evt.getNewValue()), "Error", JOptionPane.ERROR_MESSAGE);
                    } else if ("progreso".equals(evt.getPropertyName())) {
                        setProgress(getProgress() + 1, 0, partes.size());
                    }
                }
            });
            cartero.setCargarAsistenciaTotal(false);//La asignamos a mano nosotros
            boolean ret = cartero.enviar(partes);
            //Ahora tenemos que marcar como enviados aquellos que se han enviado
            setMessage("Guardando estado de notificaciones...");
            for (ParteConvivencia p : cartero.getElementosEnviados()) {
                p.setInformados(p.getInformados() | ParteConvivencia.MASCARA_INFORMADO_PADRES);
                p.guardar();
            }
            return ret;
        }
        return false;
    }

    @Override
    protected void succeeded(Boolean result) {
        if (result) {
            setMessage("Cartas impresas/enviadas correctamente.");
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Notificaciones impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            setMessage("Ha habido algún error enviando las notificaciones.");
        }
    }
}
