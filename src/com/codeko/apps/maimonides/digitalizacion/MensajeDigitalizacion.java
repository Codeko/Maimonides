package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.excepciones.NoExisteElementoException;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class MensajeDigitalizacion extends ObjetoBD implements IObjetoTabla {

    public static final MaimonidesBean control = new MaimonidesBean();
    public static final int TIPO_PARTE_FALLIDO = -4;
    public static final int TIPO_IGNORADO = -3;
    public static final int TIPO_NO_EXISTE = -2;
    public static final int TIPO_ERROR = -1;
    public static final int TIPO_OK = 0;
    public static final int TIPO_ADVERTENCIA = 1;
    /** Los tipos de advertencias**/
    public static final int TIPOA_NORMAL = 0;
    public static final int TIPOA_SIN_FIRMA = 1;
    public static final int TIPOA_FALTA_Y_RETRASO = 2;
    public static final int TIPOA_DUDA_MARCA = 3;
    public static final int TIPOA_DUDA_ANULACION = 4;
    public static final int TIPOA_DUDA_SALTOS_ASISTENCIA = 5;
    public static final int TIPOA_PARTE_INCORRECTO = 6;
    public static final int TIPOA_ERROR_DIGITALIZACION = 7;
    int tipo = TIPO_ADVERTENCIA;
    String mensaje = "";
    ParteFaltas parte = null;
    Horario horario = null;
    Alumno alumno = null;
    GregorianCalendar fecha = new GregorianCalendar();
    Integer idImagen = null;
    int tipoAdvertencia = 0;
    File parteErroneo = null;
    File metadatosParte = null;
    LineaParteAlumno lineaParteAlumno = null;
    ImageIcon imagenCasilla = null;

    public static MaimonidesBean getControl() {
        return control;
    }

    public LineaParteAlumno getLineaParteAlumno() {
        if (lineaParteAlumno == null) {
            if (getParte() != null && getHorario() != null && getAlumno() != null) {
                try {
                    lineaParteAlumno = new LineaParteAlumno(getParte(), getHorario(), getAlumno());
                } catch (SQLException ex) {
                    Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return lineaParteAlumno;
    }

    public ImageIcon getImagenCasilla() {
        return imagenCasilla;
    }

    public void setImagenCasilla(ImageIcon imagenCasilla) {
        this.imagenCasilla = imagenCasilla;
    }

    public File getParteErroneo() {
        return parteErroneo;
    }

    public final void setParteErroneo(File parteErroneo) {
        this.parteErroneo = parteErroneo;
    }

    public File getMetadatosParte() {
        return metadatosParte;
    }

    public final void setMetadatosParte(File metadatosParte) {
        this.metadatosParte = metadatosParte;
    }

    public int getTipoAdvertencia() {
        return tipoAdvertencia;
    }

    public final void setTipoAdvertencia(int tipoAdvertencia) {
        this.tipoAdvertencia = tipoAdvertencia;
    }

    public Integer getIdImagen() {
        if (Num.getInt(idImagen) == 0) {
            idImagen = null;
        }
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public ParteFaltas getParte() {
        return parte;
    }

    public void setParte(ParteFaltas parte) {
        this.parte = parte;
    }

    public String getMensaje() {
        return mensaje;
    }

    public final void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getTipo() {
        return tipo;
    }

    public final void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public MensajeDigitalizacion() {
    }

    public MensajeDigitalizacion(File parteErroneo) {
        setParteErroneo(parteErroneo);
        setTipo(TIPO_PARTE_FALLIDO);
        setMensaje("Error procesando imagen de parte.");
        //Vemos si existen los meta datos asociados
        File metadatos = new File(parteErroneo.getParentFile(), parteErroneo.getName() + ".info");
        if (metadatos.exists()) {
            setMetadatosParte(metadatos);
            Properties p = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(metadatos);
                p.load(fis);
                if (p.containsKey("texto")) {
                    setMensaje(p.getProperty("texto"));
                }
            } catch (Exception ex) {
                Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(fis);
        }
    }

    public MensajeDigitalizacion(int tipo, String mensaje) {
        setTipo(tipo);
        setMensaje(mensaje);
    }

    public MensajeDigitalizacion(int tipo, int tipoAdvertencia, String mensaje) {
        setTipo(tipo);
        setTipoAdvertencia(tipoAdvertencia);
        setMensaje(mensaje);
    }

    public boolean isMostrar() {
        //TODO Configurar los tipos de mensajes que se quieren mostrar
        return getTipo() == MensajeDigitalizacion.TIPO_ADVERTENCIA;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE partes_advertencias SET parte_id=?,mensaje=?,fecha=?,horario_id=?,alumno_id=?,imagen_id,tipo WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO partes_advertencias (parte_id,mensaje,fecha,horario_id,alumno_id,imagen_id,tipo,id) VALUES(?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getParte() != null ? getParte().getId() : null);
            st.setString(2, getMensaje());
            st.setDate(3, new java.sql.Date(getFecha().getTime().getTime()));
            st.setObject(4, getHorario() != null ? getHorario().getId() : null);
            st.setObject(5, getAlumno() != null ? getAlumno().getId() : null);
            st.setObject(6, getIdImagen());
            st.setInt(7, getTipoAdvertencia());
            st.setObject(8, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, "Error guardando datos de Mensaje: " + this, ex);
        }
        return ret;
    }

    public static boolean hayMensajesPendientes(int idParte) {
        boolean ret = false;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT count(*) FROM partes_advertencias WHERE parte_id=?");
            st.setInt(1, idParte);
            res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1) > 0;
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
        return ret;
    }

    public static boolean borrarMensajesPendientes(int idParte) {
        boolean ret = false;
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes_advertencias WHERE parte_id=?");
            st.setInt(1, idParte);
            st.executeUpdate();
            control.firePropertyChange("borrarPorIdParte", null, idParte);
            ret = true;
        } catch (SQLException ex) {
            Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
        return ret;
    }

    public void cargarDesdeResultSet(ResultSet res) throws SQLException, NoExisteElementoException {
        setId(res.getInt("id"));
        setMensaje(res.getString("mensaje"));
        setFecha(Fechas.toGregorianCalendar(res.getDate("fecha")));
        Horario h = null;
        int idH = res.getInt("horario_id");
        if (idH > 0) {
            h = Horario.getHorario(idH);
        }
        setHorario(h);
        Alumno a = null;
        int idAl = res.getInt("alumno_id");
        if (idAl > 0) {
            a = Alumno.getAlumno(idAl);
        }
        setAlumno(a);
        ParteFaltas p = null;
        int idP = res.getInt("parte_id");
        if (idP > 0) {
            p = new ParteFaltas(idP);
        }
        setParte(p);
        setIdImagen(res.getInt("imagen_id"));
        setTipoAdvertencia(res.getInt("tipo"));
    }

    public BufferedImage cargarImagen() {
        BufferedImage img = null;
        //TODO Esto quitarlo ya que ya no se usa
        if (getIdImagen() != null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT imagen FROM advertencias_imagenes WHERE id=?");
                st.setInt(1, getIdImagen());
                ResultSet res = st.executeQuery();
                if (res.next()) {
                    InputStream is = res.getBinaryStream("imagen");
                    img = ImageIO.read(is);
                    is.close();
                }
                Obj.cerrar(st, res);
            } catch (Exception ex) {
                Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (getParteErroneo() != null && getParteErroneo().exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(getParteErroneo());
                img = ImageIO.read(fis);
            } catch (IOException ex) {
                Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(fis);
        } else if (getParte() != null) {
            img = getParte().getImagen();
        }
        return img;
    }

    @Override
    public String getNombreObjeto() {
        return "Mensaje digitalización";
    }

    @Override
    public String getDescripcionObjeto() {
        return getMensaje();
    }

    @Override
    public int getNumeroDeCampos() {
        return 2;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = this;
                break;
            case 1:
                val = getFecha();
                if (getParte() != null) {
                    val = getParte().getFecha();
                }
                break;
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String titulo = "";
        switch (index) {
            case 0:
                titulo = "Mensaje";
                break;
            case 1:
                titulo = "Fecha";
                break;
        }
        return titulo;
    }

    @Override
    public Class getClassAt(int index) {
        Class clase = null;
        switch (index) {
            case 0:
                clase = this.getClass();
                break;
            case 1:
                clase = String.class;
                break;
        }
        return clase;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return false;
    }

    @Override
    public String getTabla() {
        return "partes_advertencias";
    }
}
