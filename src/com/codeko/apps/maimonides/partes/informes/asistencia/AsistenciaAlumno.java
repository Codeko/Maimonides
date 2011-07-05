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


package com.codeko.apps.maimonides.partes.informes.asistencia;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;

import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class AsistenciaAlumno implements IObjetoTabla, IAlumno {

    Alumno alumno = new Alumno();
    HashMap<Integer, Integer> asistencia = new HashMap<Integer, Integer>();
    int diasDistintos = 0;

    public AsistenciaAlumno(Alumno a) {
        setAlumno(a);
    }

    public void addAsistencia(int tipo, int valor) {
        getAsistencia().put(tipo, Num.getInt(getAsistencia().get(tipo)) + valor);
    }

    public int getDiasDistintos() {
        return diasDistintos;
    }

    public void setDiasDistintos(int diasDistintos) {
        this.diasDistintos = diasDistintos;
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    private void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        getAsistencia().clear();
    }

    public HashMap<Integer, Integer> getAsistencia() {
        return asistencia;
    }

    public static AsistenciaAlumno getAsistencia(Alumno alumno, GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, MaimonidesBean bean) {
        ArrayList<AsistenciaAlumno> as = getAsistencias(alumno, null, null, fechaDesde, fechaHasta, null, bean);
        if (as.size() > 0) {
            return as.get(0);
        } else {
            return new AsistenciaAlumno(alumno);
        }
    }

    public static ArrayList<AsistenciaAlumno> getAsistencias(Alumno alumno, Curso curso, Unidad unidad, GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, ArrayList<FiltroAsistenciaAlumno> filtrosAsistencia, MaimonidesBean bean) {
        ArrayList<AsistenciaAlumno> asistencia = new ArrayList<AsistenciaAlumno>();
        StringBuilder sql = new StringBuilder("SELECT alumno_id, "
                + " sum(IF(asistencia=2,1,0)) AS I, "
                + " sum(IF(asistencia=3,1,0)) AS E, "
                + " sum(IF(asistencia=4,1,0)) AS J, "
                + " sum(IF(asistencia=5,1,0)) AS R, "
                + " sum(IF(asistencia=2 OR asistencia=5,1,0)) AS IR,count(distinct fecha) AS diasDistintos "
                + " FROM asistencia_ "
                + " WHERE ano=" + MaimonidesApp.getApplication().getAnoEscolar().getId() + " ");
        if (alumno != null) {
            sql.append(" AND alumno_id=").append(alumno.getId()).append(" ");
        }

        if (curso != null) {
            sql.append(" AND curso_id=").append(curso.getId()).append(" ");
        }

        if (unidad != null) {
            sql.append(" AND unidad_id=").append(unidad.getId()).append(" ");
        }

        if (fechaDesde != null) {
            sql.append(" AND fecha>='").append(Fechas.getFechaFormatoBD(fechaDesde)).append("' ");
        }

        if (fechaHasta != null) {
            sql.append(" AND fecha<='").append(Fechas.getFechaFormatoBD(fechaHasta)).append("' ");
        }

        sql.append(" GROUP BY alumno_id ");
        if (filtrosAsistencia != null) {
            if (filtrosAsistencia.size() > 0) {
                sql.append(" HAVING 1=1 ");
            }
            for (FiltroAsistenciaAlumno faa : filtrosAsistencia) {
                String sqlFitro = " AND (" + ParteFaltas.getCodigoTipoFalta(faa.getTipoFalta()) + " " + faa.getOperador() + " " + faa.getValor() + ") ";
                if (faa.getTipo() == FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS) {
                    sqlFitro = " AND ((" + ParteFaltas.getCodigoTipoFalta(faa.getTipoFalta()) + "/" + Configuracion.getHorasPorDia() + ")" + faa.getOperador() + " " + faa.getValor() + ") ";
                }
                sql.append(sqlFitro);
            }
        }
        sql.append(" ORDER BY curso_id,unidad_id,alumno_id ");

        PreparedStatement st = null;
        ResultSet res = null;
        try {
            Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.INFO, "SQL: {0}", sql.toString());
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql.toString());
            res = st.executeQuery();

            while (res.next()) {
                Alumno a = Alumno.getAlumno(res.getInt("alumno_id"));
                AsistenciaAlumno asis = new AsistenciaAlumno(a);
                asis.addAsistencia(ParteFaltas.FALTA_EXPULSION, res.getInt("E"));
                asis.addAsistencia(ParteFaltas.FALTA_INJUSTIFICADA, res.getInt("I"));
                asis.addAsistencia(ParteFaltas.FALTA_INJUSTIFICADAS_RETRASOS, res.getInt("IR"));
                asis.addAsistencia(ParteFaltas.FALTA_JUSTIFICADA, res.getInt("J"));
                asis.addAsistencia(ParteFaltas.FALTA_RETRASO, res.getInt("R"));
                asis.setDiasDistintos(res.getInt("diasDistintos"));
                asistencia.add(asis);
            }

        } catch (SQLException ex) {
            Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return asistencia;
    }

    public int getValor(int tipo, int tipoFalta) {
        int valor = 0;
        switch (tipo) {
            case FiltroAsistenciaAlumno.TIPO_FALTAS:
                valor = Num.getInt(getAsistencia().get(tipoFalta));
                break;
            case FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS:
                valor = Num.getInt(getAsistencia().get(tipoFalta)) / Configuracion.getHorasPorDia();
                break;
        }
        return valor;
    }

    @Override
    public int getNumeroDeCampos() {
        return 16;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
            case 1:
            case 2:
                val = getAlumno().getValueAt(index);
                break;
            case 3:
                val = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO);
                break;
            case 4:
                val = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA);
                break;
            case 5:
                val = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_JUSTIFICADA);
                break;
            case 6:
                val = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_EXPULSION);
                break;
            case 7:
                int suma = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO);
                suma += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA);
                val = suma;
                break;
            case 8:
                int sumat = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO);
                sumat += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA);
                sumat += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_JUSTIFICADA);
                sumat += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_EXPULSION);
                val = sumat;
                break;
            case 9:
                val = getValor(FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS, ParteFaltas.FALTA_RETRASO);
                break;
            case 10:
                val = getValor(FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS, ParteFaltas.FALTA_INJUSTIFICADA);
                break;
            case 11:
                val = getValor(FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS, ParteFaltas.FALTA_JUSTIFICADA);
                break;
            case 12:
                val = getValor(FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS, ParteFaltas.FALTA_EXPULSION);
                break;
            case 13:
                int sumad = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO);
                sumad += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA);
                val = sumad / Configuracion.getHorasPorDia();//Lo hacemos así para no perder precision
                break;
            case 14:
                int sumatd = getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO);
                sumatd += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA);
                sumatd += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_JUSTIFICADA);
                sumatd += getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_EXPULSION);
                val = sumatd / Configuracion.getHorasPorDia();//Lo hacemos así para no perder precision
                break;
            case 15:
                val = getDiasDistintos();
                break;

        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = "";
        switch (index) {
            case 0:
            case 1:
            case 2:
                val = getAlumno().getTitleAt(index);
                break;
            case 3:
                val = "R";
                break;
            case 4:
                val = "I";
                break;
            case 5:
                val = "J";
                break;
            case 6:
                val = "E";
                break;
            case 7:
                val = "R+I";
                break;
            case 8:
                val = "Total";
                break;
            case 9:
                val = "SD R";
                break;
            case 10:
                val = "SD I";
                break;
            case 11:
                val = "SD J";
                break;
            case 12:
                val = "SD E";
                break;
            case 13:
                val = "SD R+I";
                break;
            case 14:
                val = "SD Total";
                break;
            case 15:
                val = "Días";
                break;
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = null;
        switch (index) {
            case 0:
            case 1:
            case 2:
                val = getAlumno().getClassAt(index);
                break;
            default:
                val = Integer.class;

        }
        return val;
    }

    @Override
    public boolean setValueAt(int index, Object valor) {
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return false;
    }
}
