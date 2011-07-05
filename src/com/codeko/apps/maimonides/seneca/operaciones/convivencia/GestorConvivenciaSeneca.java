package com.codeko.apps.maimonides.seneca.operaciones.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;

import com.codeko.apps.maimonides.convivencia.Conducta;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;
import com.codeko.apps.maimonides.convivencia.TipoConducta;
import com.codeko.apps.maimonides.convivencia.TramoHorario;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jdesktop.application.Task;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class GestorConvivenciaSeneca extends MaimonidesBean {

    ClienteSeneca cliente = null;

    public ClienteSeneca getCliente() {
        return cliente;
    }

    public final void setCliente(ClienteSeneca cliente) {
        this.cliente = cliente;
    }

    public GestorConvivenciaSeneca(ClienteSeneca cli) {
        setCliente(cli);
    }

    public boolean recuperarDatosConvivenciaSeneca() throws IOException {
        boolean ok = false;
        if (getCliente().hacerLogin()) {
            firePropertyChange("message", null, "Cargando datos de convivencias...");
            //Pasamos como número de matricula 0 porque cuela, no pone los datos de alumno pero tampoco los necesitamos ahora
            HttpGet get = new HttpGet(ClienteSeneca.getUrlBase() + "Principal.jsp?rndval=344005786&C_ANNO=" + MaimonidesApp.getApplication().getAnoEscolar().getAno() + "&COD_PAGINA=19386&MODO=NUEVO&X_MATRICULA=0&N_V_=" + getCliente().getNombreVentana());
            HttpResponse response = getCliente().getCliente().execute(get);
            String txt = EntityUtils.toString(response.getEntity());
            if (getCliente().isDebugMode()) {
                System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                ok = true;
                Source s = new Source(txt);
                //Tramos horarios
                HashMap<String, String> hTramos = getValoresCombo(s, "X_TRAMO");
                Iterator<String> iTramos = hTramos.keySet().iterator();
                while (iTramos.hasNext()) {
                    String cod = iTramos.next();
                    String valor = hTramos.get(cod);
                    TramoHorario t = TramoHorario.getTramoHorario(MaimonidesApp.getApplication().getAnoEscolar(), cod);
                    if (t == null) {
                        t = new TramoHorario();
                        t.setAnoEscolar(MaimonidesApp.getApplication().getAnoEscolar());
                        t.setCodigo(cod);
                        t.setDescripcion(valor);
                        t.guardar();
                    }
                }
                guardarConductas(s, false);
                guardarConductas(s, true);
            }
            getCliente().hacerLogout();
        }
        return ok;
    }

    private void guardarConductas(Source s, boolean medida) throws IOException {
        // Tipos medidas disciplinarias el subcampo es CORCONDISP
        HashMap<String, String> hMedidas = getValoresCombo(s, medida ? "X_GRUTIPCORCONNEG" : "X_GRUTIPCONNEG");
        Iterator<String> iMedidas = hMedidas.keySet().iterator();
        while (iMedidas.hasNext()) {
            String cod = iMedidas.next();
            String valor = hMedidas.get(cod);
            TipoConducta t = TipoConducta.geTipoConducta(MaimonidesApp.getApplication().getAnoEscolar(), cod, medida ? TipoConducta.TIPO_MEDIDA : TipoConducta.TIPO_CONDUCTA);
            if (t == null) {
                t = TipoConducta.geTipoConducta(MaimonidesApp.getApplication().getAnoEscolar(), medida ? TipoConducta.TIPO_MEDIDA : TipoConducta.TIPO_CONDUCTA, valor);
                if (t == null) {
                    t = new TipoConducta();
                    t.setAnoEscolar(MaimonidesApp.getApplication().getAnoEscolar());
                    t.setTipo(medida ? TipoConducta.TIPO_MEDIDA : TipoConducta.TIPO_CONDUCTA);
                }
            }
            t.setCodigo(cod);
            t.setDescripcion(valor);
            t.guardar();
            //Ahora vemos el campo de hijos
            HashMap<String, String> hSubTipos = getValoresComboRelacionado(cod, medida);
            Iterator<String> iSubTipos = hSubTipos.keySet().iterator();
            while (iSubTipos.hasNext()) {
                String subCod = iSubTipos.next();
                String subValor = hSubTipos.get(subCod);
                if (t.getGravedad() == 0) {
                    if (subValor.indexOf("(Contraria)") != -1) {
                        t.setGravedad(1);
                        t.guardar();
                    } else if (subValor.indexOf("(Grave)") != -1) {
                        t.setGravedad(2);
                        t.guardar();
                    }
                }
                subValor = subValor.replace("(Contraria)", "").replace("(Grave)", "").trim();
                Conducta c = Conducta.getConducta(MaimonidesApp.getApplication().getAnoEscolar(), subCod, t);
                if (c == null) {
                    //Si no la encontramos por el código la buscamos por el texto
                    c = Conducta.getConducta(MaimonidesApp.getApplication().getAnoEscolar(), t, subValor);
                    if (c == null) {
                        c = new Conducta();
                        c.setAnoEscolar(MaimonidesApp.getApplication().getAnoEscolar());
                    }
                }
                c.setCodigo(subCod);
                c.setTipo(t);
                c.setDescripcion(subValor);
                c.guardar();
            }
        }
    }

    private HashMap<String, String> getValoresComboRelacionado(String valPadre, boolean medidas) throws IOException {
        HashMap<String, String> valores = new HashMap<String, String>();
        String name = "CONCONDISP";
        String infAct = "INF_1";
        String xG = "X_GRUTIPCONNEG";
        if (medidas) {
            xG = "X_GRUTIPCORCONNEG";
            infAct = "INF_2";
            name = "CORCONDISP";
        }
        String url = "PaginaActualizacion.jsp?rndval=214853051&CAMPO=" + name + "&INF_ACT_1=" + infAct + "&" + xG + "=" + valPadre + "&N_V_=" + getCliente().getNombreVentana();
        HttpGet get = new HttpGet(ClienteSeneca.getUrlBase() + url);
        HttpResponse response = getCliente().getCliente().execute(get);
        if (getCliente().isDebugMode()) {
            System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode());
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            Scanner sc = new Scanner(response.getEntity().getContent(), "latin1");
            String pre = "top.inferior.principal.cuerpo.eval(\"optionsCombo_[optionsCombo_.length]= new Option(";
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                if (l.startsWith(pre)) {
                    //'Perturbación del normal desarrollo de las actividades de clase (Contraria)','41|2');");
                    l = l.replace(pre, "").replace(");", "").trim();
                    String[] vs = l.split("','");

                    String cod = vs[1].replace('\'', ' ').replace('"', ' ').trim();
                    String valor = vs[0].substring(1);
                    if (!valor.trim().equals("")) {
                        valores.put(cod.trim(), valor.trim());
                    }
                }
            }

        }
        return valores;
    }

    private HashMap<String, String> getValoresCombo(Source s, String name) {
        HashMap<String, String> valores = new HashMap<String, String>();
        List<Element> lTramos = s.getAllElements("name", name, false);
        if (lTramos.size() == 1) {
            Element e = lTramos.get(0);
            //Ahora este elemento tiene que tener varios
            for (Element el : e.getChildElements()) {
                String cod = el.getAttributeValue("value");
                String valor = el.getTextExtractor().toString().trim();
                if (!valor.equals("")) {
                    valores.put(cod, valor);
                }
            }
        }
        return valores;
    }

    public boolean enviarParteConvivenciaSeneca(ParteConvivencia parte) {
        boolean ret = false;
        if (getCliente().hacerLogin()) {
            ret = true;
            try {
                //Primero visitamos la url anterior
                String urlIni = "Principal.jsp?rndval=507813044&COD_PAGINA=19386&MODO=NUEVO&X_MATRICULA=" + parte.getAlumno().getCodFaltas() + "&C_ANNO=" + MaimonidesApp.getApplication().getAnoEscolar().getAno() + "&N_V_=" + getCliente().getNombreVentana();
                getCliente().visitarURL(urlIni);
                String url = "Principal.jsp?rndval=490519428&COD_PAGINA=12238&MODO=NUEVO&modoTramo=descriptivo&PAG_NO_VISIBLE_=S&N_V_=" + getCliente().getNombreVentana();
                HttpPost post = new HttpPost(ClienteSeneca.getUrlBase() + url);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                //El año
                nameValuePairs.add(new BasicNameValuePair("ANNO_SEL", MaimonidesApp.getApplication().getAnoEscolar().getAno().toString()));
                //UN checksum
                nameValuePairs.add(new BasicNameValuePair("CHECKSUM_", Fechas.format(new GregorianCalendar(), "dd/MM/yyyy") + "|"));
                //Algunos datos fijos
                nameValuePairs.add(new BasicNameValuePair("MODO", "NUEVO"));
                nameValuePairs.add(new BasicNameValuePair("modoTramo", "descriptivo"));
                nameValuePairs.add(new BasicNameValuePair("X_ALUCONCON", "-1"));
                nameValuePairs.add(new BasicNameValuePair("CONCONDISP", "-1"));
                nameValuePairs.add(new BasicNameValuePair("CORCONDISP", "-1"));
                nameValuePairs.add(new BasicNameValuePair("OTRCORCONDISP", "-1"));
                nameValuePairs.add(new BasicNameValuePair("X_GRUTIPCONNEG", "-1"));
                nameValuePairs.add(new BasicNameValuePair("X_GRUTIPCORCONNEG", "-1"));

                //El profesor con formato 137076|01/09/2009  -> cod seneca profesor + fecha toma de posesion
                Profesor p = parte.getProfesor();
                String xEmpleado = p.getCodigo() + "|" + Fechas.format(p.getFechaTomaPosesion(), "dd/MM/yyyy");
                nameValuePairs.add(new BasicNameValuePair("X_EMPLEADO", xEmpleado));
                //El identificador de séneca del alumno
                nameValuePairs.add(new BasicNameValuePair("X_MATRICULA", parte.getAlumno().getCodFaltas()));
                //El tramo en el que ha ocurrido el indicente
                nameValuePairs.add(new BasicNameValuePair("X_TRAMO", parte.getTramoHorario().getCodigo()));
                //La fecha del incidente F_ALUCONCON
                nameValuePairs.add(new BasicNameValuePair("F_ALUCONCON", Fechas.format(parte.getFecha(), "dd/MM/yyyy")));
                //Ahora la descripción del incidente
                nameValuePairs.add(new BasicNameValuePair("D_ALUCONCON", getDescripcionParte(parte)));
                //Y la descripcion detallada
                nameValuePairs.add(new BasicNameValuePair("T_ALUCONCON", parte.getObservaciones()));
                //Las diferentes conductas

                for (Conducta c : parte.getConductas()) {
                    if (!c.getCodigo().trim().equals("")) {
                        nameValuePairs.add(new BasicNameValuePair("CONDESIN", c.getCodigo()));
                    }
                }

                //TODO Faltan las fechas de notificación a los tutores/padres
                // L_NOTTUT S
                // F_NOTTUT Fecha en formato estandar

                //TODO Falta si las medidas han sido efectivas
                //L_COREFE -1/S/N (-1 indeterminado, S si, N No).
                nameValuePairs.add(new BasicNameValuePair("L_COREFE", "-1"));
                //TODO Falta el campo de colaboracion de la familia
                //X_ACTFAMCORR -> -1 , COLABORA, NO_COLABORA, IMP_CORRECCION
                nameValuePairs.add(new BasicNameValuePair("X_ACTFAMCORR", "-1"));
                //Ahora tenemos que marcar si se aplica correción o no
                //Hay tres opciones para X_ESTCORCONCON:
                //1|S|N : Se aplica
                //2|N|S : no se aplica
                //3|N|N : Pendiente
                //Esto lo vemos según el estado
                String estado = "3|N|N";//Pendiente
                if (parte.getEstado() == ParteConvivencia.ESTADO_IGNORADO) {
                    estado = "2|N|S";
                    String obs = parte.getObservaciones().trim();
                    if (obs.equals("")) {
                        obs = "--";
                    }
                    nameValuePairs.add(new BasicNameValuePair("T_MOTSINCOR", obs));
                } else if (parte.getEstado() == ParteConvivencia.ESTADO_SANCIONADO) {
                    estado = "1|S|N";
                    //Ahora tenemos que asignar las medidas disciplinarias tomadas
                    for (Conducta c : parte.getMedidas()) {
                        if (!c.getCodigo().trim().equals("")) {
                            nameValuePairs.add(new BasicNameValuePair("CORDESIN", c.getCodigo()));
                        }
                    }
                    if (parte.getMedidas().isEmpty()) {
                        //Si no hay medidas pero se considera el parte sancionado
                        //Vemos si hay expulsión asignada
                        if (Num.getInt(parte.getExpulsionID()) > 0) {
                            //Si hay expulsión asignamos las medidas
                            if (!parte.asignarMedidasPorExpulsion()) {
                                //Si no se pueden asignar cancelamos el envío
                                ret = false;
                            }
                        } else {
                            //Si no hay expulsión no enviamos nada
                            ret = false;
                        }
                    }
                }
                nameValuePairs.add(new BasicNameValuePair("X_ESTCORCONCON", estado));
                System.out.println(nameValuePairs);
                //Puede que algún proceso haya anulado el envío así que lo verificamos
                if (ret) {
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = getCliente().getCliente().execute(post);
                    String texto = EntityUtils.toString(response.getEntity());
                    if (getCliente().isDebugMode()) {
                        System.out.println(post.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
                    }
                    ret = texto.indexOf("onload=\"javascript:saltaPagError()\"") == -1;
                }
            } catch (Exception ex) {
                Logger.getLogger(GestorConvivenciaSeneca.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
        }
        return ret;
    }

    private static String getDescripcionParte(ParteConvivencia parte) {
        String desc = parte.getDescripcion();
        //Si no hay descripción
        if (desc.trim().equals("")) {
            //Por defecto ponemos el tipo de conducta
            desc = "Conducta contraria";
            if (parte.getTipo() == TipoConducta.GRAVEDAD_GRAVE) {
                desc = "Conducta grave";
            }
            //Si no se ha asignado descripción se pone según primer tipo
//            Vector<TipoConducta> t = TipoConducta.getTiposConducta(parte.getTipo());
//            if (t.size() > 0) {
//                desc = t.firstElement().getDescripcion();
//            }
        }
        //En cualquier de los casos le añadimos la ID del parte para futuras
        //referencias
        //El tamaño máximo de la descripción es de 80 caracteres
        String ref = " [" + parte.getId() + "]";
        if ((desc.length() + ref.length()) > 80) {
            desc = desc.substring(0, Math.abs(80 - (desc.length() + ref.length())));
        }
        desc = desc + ref;
        return desc;
    }

    public static ArrayList<ParteConvivencia> limpiarPartesNoEnviables(ArrayList<ParteConvivencia> partes) {
        //Verificamos cada parte para ver que si se pueden enviar y los
        //quitamos de la lista
        ArrayList<ParteConvivencia> noEnviables = new ArrayList<ParteConvivencia>();
        for (ParteConvivencia p : partes) {
            if (!p.isEnviableSeneca()) {
                noEnviables.add(p);
            }
        }
        //Ahora quitamos esos no enviables del array principal
        partes.removeAll(noEnviables);
        return noEnviables;
    }

    public static ArrayList<ParteConvivencia> getPartesAEnviar() {
        boolean ignorados = ParteConvivencia.isEnviarSenecaPartesIgnorados();
        ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            String sql="SELECT * FROM conv_partes WHERE ano=? AND estado IN (" + ParteConvivencia.ESTADO_SANCIONADO + (ignorados ? "," + ParteConvivencia.ESTADO_IGNORADO : "") + ") AND ((situacion &" + ParteConvivencia.SIT_ENVIADO_SENECA + ")!=" + ParteConvivencia.SIT_ENVIADO_SENECA + ")";
            System.out.println(sql);
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                try {
                    ParteConvivencia p = new ParteConvivencia(res);
                    partes.add(p);
                } catch (Exception ex) {
                    Logger.getLogger(GestorConvivenciaSeneca.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(GestorConvivenciaSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return partes;
    }

    public static int getNumeroPartesAEnviar() {
        boolean ignorados = ParteConvivencia.isEnviarSenecaPartesIgnorados();
        int ret = 0;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT count(*) FROM conv_partes WHERE ano=? AND estado IN (" + ParteConvivencia.ESTADO_SANCIONADO + (ignorados ? "," + ParteConvivencia.ESTADO_IGNORADO : "") + ") AND (estado&" + ParteConvivencia.SIT_ENVIADO_SENECA + "!=" + ParteConvivencia.SIT_ENVIADO_SENECA + ")");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(GestorConvivenciaSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return ret;
    }

    public static Task<Collection<ParteConvivencia>, Void> getTaskEnvioPartes(Collection<ParteConvivencia> partes) {
        return new EnviarPartesConvivenciaSenecaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), partes);
    }
}

class EnviarPartesConvivenciaSenecaTask extends org.jdesktop.application.Task<Collection<ParteConvivencia>, Void> {

    Collection<ParteConvivencia> partes = null;
    ArrayList<ParteConvivencia> fallidos = new ArrayList<ParteConvivencia>();

    EnviarPartesConvivenciaSenecaTask(org.jdesktop.application.Application app, Collection<ParteConvivencia> partes) {
        super(app);
        this.partes = partes;
        if (partes.size() > 0) {
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Se va a enviar a Séneca " + partes.size() + " partes de convivencia.\n¿Continuar?", "Enviar partes de convivencia a Séneca", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op != JOptionPane.OK_OPTION) {
                cancel(true);
            } else {
                if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                    cancel(false);
                }
            }
        } else {
            cancel(true);
        }
    }

    @Override
    protected ArrayList<ParteConvivencia> doInBackground() {
        ClienteSeneca cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
        cli.setDebugMode(MaimonidesApp.isDebug());
        cli.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        GestorConvivenciaSeneca gestorConv = new GestorConvivenciaSeneca(cli);
        gestorConv.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        setMessage("Enviando partes...");
        int cont = 0;
        for (ParteConvivencia p : partes) {
            cont++;
            if (p.isEnviableSeneca()) {
                setMessage("Enviando parte " + cont + "/" + partes.size() + "...");
                setProgress(cont, 0, partes.size());
                if (gestorConv.enviarParteConvivenciaSeneca(p)) {
                    setMessage("Parte " + cont + "");
                    p.setEnviadoSeneca(true);
                    p.guardar();
                } else {
                    fallidos.add(p);
                }
            } else {
                setMessage("El parte no tiene los datos correctos para ser enviado a Séneca.");
                fallidos.add(p);
            }
        }
        cli.hacerLogout();
        return fallidos;
    }

    @Override
    protected void succeeded(Collection<ParteConvivencia> result) {
        if (result == null || result.isEmpty()) {
            setMessage("Partes enviados a Séneca correctamente.");
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Todos los partes se han enviado correctamente.", "Envío finalizado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            setMessage("Algunos partes no se han podido enviar a Séneca.");
            //TODO Mostrar más información del error
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No se han podido enviar " + result.size() + " de los " + partes.size() + " partes de asistencia.", "Error enviando partes", JOptionPane.ERROR_MESSAGE);
        }
    }
}
