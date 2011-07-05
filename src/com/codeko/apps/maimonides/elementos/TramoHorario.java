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


package com.codeko.apps.maimonides.elementos;


import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
public final class TramoHorario extends ObjetoBDConCod {

    Integer codigo = null;
    int jornada = -1;
    int hora = -1;
    int hini = 0;
    int hfin = 0;
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();

    public static TramoHorario getTramoHorario(int id) throws Exception {
        Object obj = Cache.get(TramoHorario.class, id);
        if (obj != null) {
            return (TramoHorario) obj;
        } else {
            TramoHorario p = new TramoHorario(id);
            return p;
        }
    }

    public static TramoHorario getTramoHorario(AnoEscolar ano, String codigo) {
        TramoHorario t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM tramos WHERE ano=? AND cod=?");
            st.setInt(1, ano.getId());
            st.setString(2, codigo);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new TramoHorario();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public TramoHorario() {
    }

    public TramoHorario(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM tramos WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ningún tramo horario con ID " + id + ".");
        }
    }

    public TramoHorario(AnoEscolar ano, int codigo) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM tramos WHERE cod=? AND ano=?");
        st.setInt(1, codigo);
        st.setInt(2, ano.getId());
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ningún tramo horario con COD " + codigo + " en el año escolar " + ano + " [" + ano.getId() + "]");
        }
    }

    public boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret=true;
        try {
            setId(res.getInt("id"));
            setCodigo(res.getInt("cod"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
            setHora(res.getInt("hora"));
            setHini(res.getInt("inicio"));
            setHfin(res.getInt("fin"));
            setJornada(res.getInt("jornada"));
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            ret=false;
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    private int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getHoraHorarios(){
        int ret=getHora();
        if(ret>0){
            if(ret==4){
                ret=0;
            }else if(ret>4){
                ret=ret-1;
            }
        }
        return ret;
    }

    public int getHfin() {
        return hfin;
    }

    public void setHfin(int hfin) {
        this.hfin = hfin;
    }

    public int getHini() {
        return hini;
    }

    public void setHini(int hini) {
        this.hini = hini;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public int getJornada() {
        return jornada;
    }

    public void setJornada(int jornada) {
        this.jornada = jornada;
    }

    @Override
    public boolean _guardar(boolean crear) {
        boolean ret = false;
        try {
            String sql = "UPDATE tramos SET cod=?,ano=?,hora=?,inicio=?,fin=?,jornada=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO tramos (cod,ano,hora,inicio,fin,jornada,id) VALUES(?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setInt(3, getHora());
            st.setInt(4, getHini());
            st.setInt(5, getHfin());
            st.setInt(6, getJornada());
            st.setObject(7, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, "Error guardando datos de Tramo Horario: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Tramo horario";
    }

    @Override
    public String getDescripcionObjeto() {
        return getId() + "[" + getHora() + "]";
    }

    public static TramoHorario getTramoParaHoraEstandar(AnoEscolar ano, int hora) {
        TramoHorario tramo = null;
        //Para recuperar un tramo por su hora estandar se ignoran los recreos y se coge por orden (ordenado por hora de inicio claro).
        String sql = "SELECT * FROM `tramos` WHERE ano=? AND fin-inicio>30 ORDER BY inicio ASC LIMIT ?,1";
//        Le quitamos uno a la hora ya que si queremos la 4 por ejemplo nos interesa que el limit sea 3,1
        hora--;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, ano.getId());
            st.setInt(2, hora);
            res = st.executeQuery();
            if (res.next()) {
                tramo = new TramoHorario();
                tramo.cargarDesdeResultSet(res);
            }
        } catch (Exception ex) {
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, null, ex);
            tramo = null;
        }
        Obj.cerrar(st, res);
        return tramo;
    }

    @Override
    public String getTabla() {
        return "tramos";
    }

    public static ArrayList<TramoHorario> getTramosHorarios() {
        ArrayList<TramoHorario> tramos = new ArrayList<TramoHorario>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM tramos WHERE ano=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                TramoHorario t = new TramoHorario();
                if (t.cargarDesdeResultSet(res)) {
                    tramos.add(t);
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tramos;
    }
}
