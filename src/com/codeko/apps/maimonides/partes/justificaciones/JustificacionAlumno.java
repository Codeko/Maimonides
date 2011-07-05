package com.codeko.apps.maimonides.partes.justificaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.partes.AsistenciaAlumno;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class JustificacionAlumno extends AsistenciaAlumno {

    public JustificacionAlumno() {
    }

    public JustificacionAlumno(GregorianCalendar fecha, AnoEscolar anoEscolar, Alumno alumno) {
        super(fecha, anoEscolar, alumno);
    }

    public static ArrayList<JustificacionAlumno> getJustificaciones(Alumno alumno) {
        ArrayList<JustificacionAlumno> jus = new ArrayList<JustificacionAlumno>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT pa.*,ph.dividido,ph.firmado,p.* FROM partes_alumnos AS pa JOIN partes AS p ON p.id=pa.parte_id JOIN partes_horarios AS ph ON ph.parte_id=pa.parte_id AND ph.horario_id=pa.horario_id WHERE pa.alumno_id=? ORDER BY p.fecha DESC ");
            st.setInt(1, alumno.getId());
            ResultSet res = st.executeQuery();
            GregorianCalendar ultimaFecha = null;
            JustificacionAlumno justif = null;
            ParteFaltas ultimoParte = null;
            while (res.next()) {
                GregorianCalendar fecha = Fechas.toGregorianCalendar(res.getDate("p.fecha"));
                if (ultimaFecha == null || !fecha.equals(ultimaFecha)) {
                    if (justif != null) {
                        justif.setParte(null);
                        jus.add(justif);
                    }
                    justif = new JustificacionAlumno();
                    justif.setAlumno(alumno);
                    justif.setFecha(fecha);
                }
                int idParte = res.getInt("parte_id");
                if (ultimoParte == null || !ultimoParte.getId().equals(idParte)) {
                    ultimoParte = new ParteFaltas();
                    ultimoParte.cargarDesdeResultSet(res, "p.");
                }
                justif.setParte(ultimoParte);
                justif.cargarLinea(res);
                ultimaFecha = fecha;
            }
            Obj.cerrar(st, res);
            if (justif != null) {
                jus.add(justif);
            }
        } catch (Exception ex) {
            Logger.getLogger(JustificacionAlumno.class.getName()).log(Level.SEVERE, "Error cargando lineas por Alumno: " + alumno, ex);
        }
        return jus;
    }

    @Override
    public Object getValueAt(int index) {
        if (index == 0) {
            return Fechas.format(getFecha(), "dd/MM/yy EEE");//getFecha();
        } else {
            return super.getValueAt(index);
        }
    }

    @Override
    public String getTitleAt(int index) {
        if (index == 0) {
            return "Fecha";
        } else {
            return super.getTitleAt(index);
        }
    }

    @Override
    public Class getClassAt(int index) {
        if (index == 0) {
            return String.class;
        } else {
            return super.getClassAt(index);
        }
    }
}
