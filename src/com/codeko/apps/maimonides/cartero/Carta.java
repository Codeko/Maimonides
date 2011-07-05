package com.codeko.apps.maimonides.cartero;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.apps.maimonides.usr.Usuario;
import com.codeko.swing.CdkAutoTabla;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Archivo;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Carta extends ObjetoBD {

    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CARTA_AVISO_FALTAS = 1;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CARTA_PERDIDA_ESCOLARIDA_GLOBAL = 2;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS = 3;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CARTA_EXPULSION = 4;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CARTA_NOTIFICACION_MANUAL = 5;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CARTA_PARTE_CONVIVENCIA = 6;
    Alumno alumno = null;
    @CdkAutoTablaCol(titulo = "Notificación", editable = CdkAutoTabla.EDITABLE_NO)
    String nombre = "";
    @CdkAutoTablaCol(titulo = "Aviso", editable = CdkAutoTabla.EDITABLE_NO)
    String descripcion = "";
    GregorianCalendar fecha = new GregorianCalendar();
    int tipo = 0;
    @CdkAutoTablaCol(titulo = "Medio")
    int modo = 0;
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<String> parametros = new ArrayList<String>();
    @CdkAutoTablaCol(ignorar = true)
    File archivo = null;
    String localizador = "";
    @CdkAutoTablaCol(titulo = "Notificado por")
    Usuario usuario = MaimonidesApp.getApplication().getUsuario();

    public static String getNombreTipo(int tipo) {
        String nombre = "";
        switch (tipo) {
            case TIPO_CARTA_AVISO_FALTAS:
                nombre = "Faltas de asistencia";
                break;
            case TIPO_CARTA_PERDIDA_ESCOLARIDA_GLOBAL:
                nombre = "Pérdida de evaluación continua";
                break;
            case TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS:
                nombre = "Pérdida de evaluación continua en materias";
                break;
            case TIPO_CARTA_EXPULSION:
                nombre = "Expulsión";
                break;
            case TIPO_CARTA_NOTIFICACION_MANUAL:
                nombre = "Notificación manual";
                break;
            case TIPO_CARTA_PARTE_CONVIVENCIA:
                nombre = "Partes de convivencia";
                break;
        }
        return nombre;
    }

    public void cargarDesdeResultSet(ResultSet res) throws SQLException {
        setId(res.getInt("id"));
        setUsuario(null);
        setAlumno(Alumno.getAlumno(res.getInt("alumno_id")));
        setNombre(res.getString("nombre"));
        setDescripcion(res.getString("descripcion"));
        setFecha(Fechas.toGregorianCalendar(res.getTimestamp("fecha")));
        setTipo(res.getInt("tipo"));
        setModo(res.getInt("modo"));
        setLocalizador(res.getString("localizador"));
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            File f = File.createTempFile("mm_cartas_", ".pdf");
            fos = new FileOutputStream(f);
            is = res.getBinaryStream("archivo");
            Archivo.copiarArchivo(is, fos);
            setArchivo(f);
        } catch (Exception ex) {
            Logger.getLogger(Carta.class.getName()).log(Level.SEVERE, null, ex);
        }
        int usuarioId = res.getInt("usuario_id");
        if (usuarioId > 0) {
            Usuario u;
            try {
                u = Usuario.getUsuario(usuarioId);
                setUsuario(u);
            } catch (Exception ex) {
                Logger.getLogger(Carta.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Obj.cerrar(fos, is);
    }

    public String getLocalizador() {
        return localizador;
    }

    public void setLocalizador(String localizador) {
        this.localizador = localizador;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public File getArchivo() {
        return archivo;
    }

    public void setArchivo(File archivo) {
        this.archivo = archivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public int getModo() {
        return modo;
    }

    public void setModo(int modo) {
        this.modo = modo;
    }

    public void addModo(int modo) {
        setModo(getModo() | modo);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public ArrayList<String> getParametros() {
        return parametros;
    }

    public void addParametro(String parametro) {
        getParametros().add(parametro);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE cartas SET alumno_id=?,nombre=?,descripcion=?,fecha=?,tipo=?,modo=?,parametros=?,localizador=?,usuario_id=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO cartas(alumno_id,nombre,descripcion,fecha,tipo,modo,parametros,localizador,usuario_id,id) VALUES(?,?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, getAlumno().getId());
            st.setString(2, getNombre());
            st.setString(3, getDescripcion());
            if (getFecha() != null) {
                st.setTimestamp(4, new java.sql.Timestamp(getFecha().getTime().getTime()));
            } else {
                st.setObject(4, null);
            }
            st.setInt(5, getTipo());
            st.setInt(6, getModo());
            st.setString(7, Str.implode(getParametros(), "|"));
            st.setString(8, getLocalizador());
            if (getUsuario() == null) {
                st.setObject(9, null);
            } else {
                st.setInt(9, getUsuario().getId());
            }
            st.setObject(10, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            Obj.cerrar(st);
            if (getArchivo() != null) {
                PreparedStatement stArch = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE cartas SET archivo=? WHERE id=?");
                FileInputStream fis = new FileInputStream(getArchivo());
                stArch.setBinaryStream(1, fis);
                stArch.setInt(2, getId());
                stArch.executeUpdate();
                Obj.cerrar(stArch, fis);
            }
        } catch (Exception ex) {
            Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, "Error guardando datos de Curso: " + this, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Notificación a alumno";
    }

    @Override
    public String getDescripcionObjeto() {
        return getNombre();
    }

    public static boolean isNotificado(Alumno alumno, int tipo) {
        boolean notificado = false;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT 1 FROM cartas WHERE alumno_id=? AND tipo=?");
            st.setInt(1, alumno.getId());
            st.setInt(2, tipo);
            res = st.executeQuery();
            notificado = res.next();
        } catch (SQLException ex) {
            Logger.getLogger(Carta.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return notificado;
    }

    public static boolean isNotificado(Alumno alumno, int tipo, String parametro) {
        boolean notificado = false;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT 1 FROM cartas WHERE alumno_id=? AND tipo=? AND (parametros LIKE ? OR parametros LIKE ? OR parametros LIKE ? OR parametros LIKE ? ) ");
            st.setInt(1, alumno.getId());
            st.setInt(2, tipo);
            st.setString(3, parametro);
            st.setString(4, parametro + "|%");
            st.setString(5, "%|" + parametro + "|%");
            st.setString(6, "%|" + parametro);
            res = st.executeQuery();
            notificado = res.next();
        } catch (SQLException ex) {
            Logger.getLogger(Carta.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return notificado;
    }

    @Override
    public String getTabla() {
        return "cartas";
    }
}
