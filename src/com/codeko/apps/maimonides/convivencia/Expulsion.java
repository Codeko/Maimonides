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


package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.security.InvalidParameterException;
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
public class Expulsion extends ObjetoBD {

    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    @CdkAutoTablaCol(ignorar = true)
    Alumno alumno = null;
    @CdkAutoTablaCol(titulo = "Fecha")
    GregorianCalendar fecha = new GregorianCalendar();
    @CdkAutoTablaCol(titulo = "Días")
    Integer dias = 1;
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<ParteConvivencia> partes = null;

    public Expulsion() {
    }

    public Expulsion(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM expulsiones WHERE id=? ");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new InvalidParameterException("No existe ninguna expulsion con ID " + id);
        }
        Obj.cerrar(st, res);
    }

    public static Expulsion getExpulsion(Integer id) throws Exception {
        return new Expulsion(id);
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
        setAlumno(Alumno.getAlumno(res.getInt("alumno_id")));
        setDias(res.getInt("dias"));
        setFecha(Fechas.toGregorianCalendar(res.getDate("fecha")));
    }

    public ArrayList<ParteConvivencia> getPartes() {
        if (partes == null) {
            partes = new ArrayList<ParteConvivencia>();
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT * FROM conv_partes WHERE expulsion_id=?");
                st.setInt(1, getId());
                res = st.executeQuery();
                while (res.next()) {
                    ParteConvivencia parte = new ParteConvivencia();
                    parte.cargarDesdeResultSet(res);
                    partes.add(parte);
                }
            } catch (Exception ex) {
                Logger.getLogger(Expulsion.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, res);
        }
        return partes;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public Integer getDias() {
        return dias;
    }

    public void setDias(Integer dias) {
        this.dias = dias;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    /**
     *  Calcula si un alumno esta expulsado. Se sabe si un alumno está expulsado por el número de dias escolares entre la fecha de expulsión y la de parte.
     * Un día escolar es el numero de partes con fecha distinta entre dos fechas.
     * @param anoEscolar Año escola
     * @param alumno Alumno
     * @param fecha Fecha en la que se quiere saber si el alumno estça expulsado
     * @return true si está expulsado en la fecha del parte, false si no lo está
     */
    public static Boolean isAlumnoExpulsado(Alumno alumno, GregorianCalendar fecha) {
        boolean ret = false;
        try {
            //Tenemos que ver si hay expulsiones para ese alumno
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM expulsiones WHERE alumno_id=? AND fecha<=?");
            st.setInt(1, alumno.getId());
            st.setDate(2, new java.sql.Date(fecha.getTimeInMillis()));
            ResultSet res = st.executeQuery();
            while (res.next() && !ret) {
                //Si hay expulsiones tenemos que ver si son válidas
                //Para eso tenemos que contar los partes desde la fecha de expulsión hasta ahora
                GregorianCalendar fechaExpulsion = Fechas.toGregorianCalendar(res.getDate("fecha"));
                if (fechaExpulsion != null) {
                    //Como la fecha de expulsión esta incluida tenemos que quitarle un día
                    fechaExpulsion.add(GregorianCalendar.DATE, -1);
                    //Y vemos la diferencia en días
                    int dias = res.getInt("dias");
                    long diasTranscurridos = Fechas.getDiferenciaTiempoEn(fecha, fechaExpulsion, GregorianCalendar.DATE);
                    ret = diasTranscurridos <= dias;
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Expulsion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static int getNumeroExpulsiones(Alumno alumno, GregorianCalendar fecha) {
        int ret = 0;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            //Tenemos que ver si hay expulsiones para ese alumno
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT count(*) FROM expulsiones WHERE alumno_id=? AND fecha<=?");
            st.setInt(1, alumno.getId());
            st.setDate(2, new java.sql.Date(fecha.getTimeInMillis()));
            res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Expulsion.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return ret;
    }

    public GregorianCalendar getFechaRegreso() {
        GregorianCalendar regreso = (GregorianCalendar) getFecha().clone();
        regreso.add(GregorianCalendar.DAY_OF_MONTH, getDias());
        return regreso;
    }

    public GregorianCalendar getFechaUltimoDiaExpulsion() {
        GregorianCalendar regreso = (GregorianCalendar) getFecha().clone();
        regreso.add(GregorianCalendar.DAY_OF_MONTH, getDias() - 1);
        return regreso;
    }

    public static Expulsion getUltimaExpulsionPorFechaDeRegreso(Alumno alumno) {
        Expulsion e = null;
        PreparedStatement st = null;
        ResultSet res = null;
        String sql = "SELECT ADDDATE(fecha,dias) AS fechaEntrada ,e.* FROM expulsiones AS e WHERE e.alumno_id=? ORDER BY fechaEntrada DESC LIMIT 0,1";
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, alumno.getId());
            res = st.executeQuery();
            if (res.next()) {
                e = new Expulsion();
                e.cargarDesdeResultSet(res);
            }
        } catch (Exception ex) {
            Logger.getLogger(Expulsion.class.getName()).log(Level.SEVERE, null, ex);
            e = null;
        }
        Obj.cerrar(st, res);
        return e;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE expulsiones SET ano=?,alumno_id=?,fecha=?,dias=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO expulsiones (ano,alumno_id,fecha,dias,id) VALUES(?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, getAnoEscolar().getId());
            st.setInt(2, getAlumno().getId());
            st.setDate(3, new java.sql.Date(getFecha().getTimeInMillis()));
            st.setInt(4, getDias());
            st.setObject(5, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
            ret = true;
        } catch (SQLException ex) {
            Logger.getLogger(Conducta.class.getName()).log(Level.SEVERE, "Error guardando datos de expulsion: " + this, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Expulsión";
    }

    @Override
    public String getDescripcionObjeto() {
        GregorianCalendar calFin = (GregorianCalendar) getFecha().clone();
        calFin.add(GregorianCalendar.DAY_OF_MONTH, getDias() - 1);
        return "Del " + Fechas.format(getFecha()) + " al " + Fechas.format(calFin) + " (" + getDias() + " días)";
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    @Override
    public String getTabla() {
        return "expulsiones";
    }
}
