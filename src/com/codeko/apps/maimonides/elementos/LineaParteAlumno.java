package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class LineaParteAlumno {
    ParteFaltas parte = null;
    Horario horario = null;
    Alumno alumno = null;
    boolean dividido = false;
    boolean firmado = false;
    int asistencia = ParteFaltas.FALTA_INDETERMINADA;
    Boolean apoyo = null;
    Integer posicion = null;

    public LineaParteAlumno() {
    }

    public LineaParteAlumno(ParteFaltas parte, Horario horario, Alumno alumno) throws SQLException {
        setParte(parte);
        setHorario(horario);
        setAlumno(alumno);
        //Ahora calculamos si esta dividido y/o firmado
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT ph.dividido,ph.firmado,pa.asistencia,pa.posicion FROM partes_alumnos AS pa JOIN partes_horarios AS ph ON ph.parte_id=pa.parte_id AND ph.horario_id=pa.horario_id WHERE pa.parte_id=? AND pa.horario_id=? AND pa.alumno_id=?");
        st.setInt(1, parte.getId());
        st.setInt(2, horario.getId());
        st.setInt(3, alumno.getId());
        ResultSet res = st.executeQuery();
        if (res.next()) {
            setDividido(res.getBoolean("dividido"));
            setFirmado(res.getBoolean("firmado"));
            setAsistencia(res.getInt("asistencia"));
            setPosicion(res.getInt("posicion"));
        }
        Obj.cerrar(st, res);
    }

    public Integer getPosicion() {
        return posicion;
    }

    public final void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Boolean isApoyo() {
        if (apoyo == null) {
            apoyo = false;
            //Tenemos que ver si este alumno para esta clase tiene apoyo
            if (getAlumno() != null && getHorario() != null) {
                try {
                    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM apoyos_alumnos WHERE alumno_id=? AND horario_id=?");
                    st.setInt(1, getAlumno().getId());
                    st.setInt(2, getHorario().getId());
                    ResultSet res = st.executeQuery();
                    apoyo = (res.next());
                    Obj.cerrar(st, res);
                } catch (SQLException ex) {
                    Logger.getLogger(LineaParteAlumno.class.getName()).log(Level.SEVERE, "Error recuperando apoyo para alumno: " + getAlumno() + " con materia " + getHorario().getMateria(), ex);
                }
            }
        }
        return apoyo;
    }

    public void setApoyo(Boolean apoyo) {
        this.apoyo = apoyo;
    }

    public boolean isDividido() {
        return dividido;
    }

    public final void setDividido(boolean dividido) {
        this.dividido = dividido;
    }

    public boolean isFirmado() {
        return firmado;
    }

    public final void setFirmado(boolean firmado) {
        this.firmado = firmado;
    }

    public Horario getHorario() {
        return horario;
    }

    public final void setHorario(Horario horario) {
        this.horario = horario;
    }

    public ParteFaltas getParte() {
        return parte;
    }

    public final void setParte(ParteFaltas parte) {
        this.parte = parte;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public final void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public int getAsistencia() {
        return asistencia;
    }

    public final void setAsistencia(int asistencia) {
        this.asistencia = asistencia;
    }

    public boolean guardarAsistencia() {
        boolean ret = false;
        PreparedStatement st = null;
        PreparedStatement stP = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes_alumnos SET asistencia=? WHERE parte_id=? AND horario_id=? AND alumno_id=?");
            st.setInt(1, getAsistencia());
            st.setInt(2, getParte().getId());
            st.setInt(3, getHorario().getId());
            st.setInt(4, getAlumno().getId());
            ret = st.executeUpdate() > 0;
            if (ret) {
                //Siempre que se actualice una asistencia el parte deja de estar enviado y procesado
                getParte().marcarNoProcesadoNoEnviado();
            }
        } catch (SQLException ex) {
            Logger.getLogger(LineaParteAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }

        Obj.cerrar(st, stP);
        return ret;
    }
}
