package com.codeko.apps.maimonides.convivencia.informes;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.convivencia.Conducta;
import com.codeko.apps.maimonides.convivencia.Expulsion;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;
import com.codeko.apps.maimonides.convivencia.TipoConducta;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
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
public class DatoResumenConvivencia implements IAlumno {

    @CdkAutoTablaCol(peso = -3, titulo = "Alumno")
    Alumno alumno = null;
    Integer leves = 0;
    Integer graves = 0;
    Integer levesPendientes = 0;
    Integer gravesPendientes = 0;
    Integer totalLeves = 0;
    Integer totalGraves = 0;
    Integer expulsiones = 0;
    Double mediaMensual = 0d;

    public DatoResumenConvivencia(Alumno alumno) {
        setAlumno(alumno);
    }

    public Integer getExpulsiones() {
        return expulsiones;
    }

    public void setExpulsiones(Integer expulsiones) {
        this.expulsiones = expulsiones;
    }

    public Integer getGraves() {
        return graves;
    }

    public void setGraves(Integer graves) {
        this.graves = graves;
    }

    public Integer getGravesPendientes() {
        return gravesPendientes;
    }

    public void setGravesPendientes(Integer gravesPendientes) {
        this.gravesPendientes = gravesPendientes;
    }

    public Integer getLeves() {
        return leves;
    }

    public void setLeves(Integer leves) {
        this.leves = leves;
    }

    public Integer getLevesPendientes() {
        return levesPendientes;
    }

    public void setLevesPendientes(Integer levesPendientes) {
        this.levesPendientes = levesPendientes;
    }

    public Integer getTotalGraves() {
        return totalGraves;
    }

    public void setTotalGraves(Integer totalGraves) {
        this.totalGraves = totalGraves;
    }

    public Integer getTotalLeves() {
        return totalLeves;
    }

    public void setTotalLeves(Integer totalLeves) {
        this.totalLeves = totalLeves;
    }

    public Double getMediaMensual() {
        return mediaMensual;
    }

    public void setMediaMensual(Double mediaMensual) {
        this.mediaMensual = mediaMensual;
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    public final void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    @CdkAutoTablaCol(titulo = "Curso", peso = -2)
    public Curso getCurso() {
        return getAlumno().getObjetoCurso();
    }

    @CdkAutoTablaCol(titulo = "Unidad", peso = -1)
    public Unidad getUnidad() {
        return getAlumno().getUnidad();
    }

    public static ArrayList<DatoResumenConvivencia> getDatosResumenConvivencia(GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, Curso curso, Unidad unidad, Profesor profesor, Integer tipo, ArrayList<Conducta> conductas) {
        ArrayList<DatoResumenConvivencia> datos = new ArrayList<DatoResumenConvivencia>();
        boolean addConductas = (conductas != null && conductas.size() > 0);

        StringBuilder sql = new StringBuilder("SELECT cp.alumno_id,sum(IF(tipo=" + TipoConducta.GRAVEDAD_GRAVE + " && estado=" + ParteConvivencia.ESTADO_SANCIONADO + ",1,0)) AS gravesSancionadas,sum(IF(tipo=" + TipoConducta.GRAVEDAD_GRAVE + " && estado=" + ParteConvivencia.ESTADO_PENDIENTE + ",1,0)) AS gravesPendientes,sum(IF(tipo=" + TipoConducta.GRAVEDAD_LEVE + " && estado=" + ParteConvivencia.ESTADO_SANCIONADO + ",1,0)) AS levesSancionadas,sum(IF(tipo=" + TipoConducta.GRAVEDAD_LEVE + " && estado=" + ParteConvivencia.ESTADO_PENDIENTE + ",1,0)) AS levesPendientes "
                + " FROM conv_partes AS cp JOIN alumnos AS a ON a.id=cp.alumno_id "
                + (addConductas ? " JOIN conv_lineas AS cl ON cl.parte_id = cp.id " : "")
                + " WHERE cp.ano=? ");

        if (curso != null && curso.getId() != null) {
            sql.append(" AND a.curso_id=").append(curso.getId()).append(" ");
        }
        if (unidad != null && unidad.getId() != null) {
            sql.append(" AND a.unidad_id=").append(unidad.getId()).append(" ");
        }
        if (fechaDesde != null) {
            sql.append(" AND cp.fecha>='").append(Fechas.getFechaFormatoBD(fechaDesde)).append("' ");
        }
        if (fechaHasta != null) {
            sql.append(" AND cp.fecha<='").append(Fechas.getFechaFormatoBD(fechaHasta)).append("' ");
        }
        if (tipo != null) {
            sql.append(" AND cp.tipo=").append(tipo).append(" ");
        }
        if (profesor != null) {
            sql.append(" AND cp.profesor_id=").append(profesor.getId()).append(" ");
        }
        if (addConductas) {
            ArrayList<Integer> ids = new ArrayList<Integer>();
            for (Conducta c : conductas) {
                ids.add(c.getId());
            }
            String sIds = Str.implode(ids, ",", "0");
            sql.append(" AND cl.conducta_id IN (").append(sIds).append(") ");
        }
        sql.append(" GROUP BY cp.alumno_id ");
        PreparedStatement st = null;
        ResultSet res = null;
        PreparedStatement st2 = null;
        PreparedStatement st3 = null;
        ResultSet res2 = null;
        try {
            st2 = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT sum(IF(tipo=" + TipoConducta.GRAVEDAD_GRAVE + ",1,0)) AS graves,sum(IF(tipo=" + TipoConducta.GRAVEDAD_LEVE + ",1,0)) AS leves FROM conv_partes WHERE alumno_id=? GROUP BY alumno_id");
            st3 = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT fecha FROM expulsiones WHERE alumno_id=? ORDER BY fecha ASC LIMIT 0,1");
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql.toString());
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                Alumno a = Alumno.getAlumno(res.getInt("alumno_id"));
                DatoResumenConvivencia dato = new DatoResumenConvivencia(a);
                dato.setExpulsiones(Expulsion.getNumeroExpulsiones(a, new GregorianCalendar()));
                dato.setGraves(res.getInt("gravesSancionadas"));
                dato.setGravesPendientes(res.getInt("gravesPendientes"));
                dato.setLeves(res.getInt("levesSancionadas"));
                dato.setLevesPendientes(res.getInt("levesPendientes"));
                //Ahora hay que calcular el total de graves y leves independientemente de la fecha
                st2.setInt(1, a.getId());
                res2 = st2.executeQuery();
                int gr = 0;
                int lv = 0;
                if (res2.next()) {
                    gr = res2.getInt("graves");
                    lv = res2.getInt("leves");
                }
                dato.setTotalGraves(gr);
                dato.setTotalLeves(lv);
                Obj.cerrar(res2);
                st3.setInt(1, a.getId());
                res2 = st3.executeQuery();
                GregorianCalendar cal = null;
                if (res2.next()) {
                    cal = Fechas.toGregorianCalendar(res2.getDate(1));
                }
                if (cal != null) {
                    long meses = Fechas.getDiferenciaTiempoEn(new GregorianCalendar(), cal, GregorianCalendar.MONTH);
                    if (meses <= 0) {
                        meses = 1;
                    }
                    double media = dato.getExpulsiones() / meses;
                    dato.setMediaMensual(media);
                }
                Obj.cerrar(res2);
                datos.add(dato);
            }
        } catch (Exception ex) {
            Logger.getLogger(DatoResumenConvivencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return datos;
    }
}
