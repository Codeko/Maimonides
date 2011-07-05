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

import com.codeko.apps.maimonides.*;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.beans.Beans;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Profesor extends ObjetoBDConCod implements IObjetoTabla, IEmailable {

    Integer codigo = 0;
    
    GregorianCalendar fechaTomaPosesion = null;
    String nombre = "";
    String apellido1 = "";
    String apellido2 = "";
    String puesto = "";
    String email = "";
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();

    public static Profesor getProfesor(int id) throws Exception {
        Object obj = Cache.get(Profesor.class, id);
        if (obj != null) {
            return (Profesor) obj;
        } else {
            Profesor p = new Profesor(id);
            return p;
        }
    }

    public static Profesor getProfesorPorCodigo(int codigo, AnoEscolar ano) {
        Profesor p = null;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM profesores WHERE cod=? AND ano=?");
            st.setInt(1, codigo);
            st.setInt(2, ano.getId());
            res = st.executeQuery();
            if (res.next()) {
                p = new Profesor();
                p.cargarDesdeResultSet(res);
            }
        } catch (Exception e) {
        }
        Obj.cerrar(st, res);
        return p;
    }

    @Override
    public boolean borrar() {
        boolean ret = false;
        if (getId() != null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE profesores SET fbaja=NOW() WHERE id=?");
                st.setInt(1, getId());
                ret = st.executeUpdate() > 0;
            } catch (SQLException ex) {
                Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, "Error borrando profesor: " + this, ex);
            }
        }
        return ret;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public GregorianCalendar getFechaTomaPosesion() {
        return fechaTomaPosesion;
    }

    public void setFechaTomaPosesion(GregorianCalendar fechaTomaPosesion) {
        this.fechaTomaPosesion = fechaTomaPosesion;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Profesor() {
    }

    private Profesor(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM profesores WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ningun profesor con ID " + id);
        }
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setCodigo(res.getInt("cod"));
        setNombre(res.getString("nombre"));
        setApellido1(res.getString("apellido1"));
        setApellido2(res.getString("apellido2"));
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
        setPuesto(res.getString("puesto"));
        setEmail(res.getString("email"));
        setFechaTomaPosesion(Fechas.toGregorianCalendar(res.getDate("ftoma")));
        Cache.put(getClass(), getId(), this);
    }

    public static ArrayList<Profesor> getProfesores() {
        ArrayList<Profesor> profesores = new ArrayList<Profesor>();
        try {
            if (!Beans.isDesignTime()) {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM profesores WHERE ano=? ORDER BY nombre,apellido1,apellido2");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    Profesor u = new Profesor();
                    u.cargarDesdeResultSet(res);
                    profesores.add(u);
                }
                Obj.cerrar(st, res);
            }
        } catch (Exception ex) {
            Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return profesores;
    }

    @Override
    public boolean _guardar(boolean crear) {
        boolean ret = false;
        try {
            String fbaja = "";
            //Si está marcado para crear asignamos la fecha de baja a null
            if (crear) {
                fbaja = ", fbaja=NULL ";
            }
            String sql = "UPDATE profesores SET cod=?,nombre=?,apellido1=?,apellido2=?,puesto=?,email=?,ftoma=?,ano=?" + fbaja + " WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO profesores (cod,nombre,apellido1,apellido2,puesto,email,ftoma,ano,id) VALUES(?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setString(2, getNombre());
            st.setString(3, getApellido1());
            st.setString(4, getApellido2());
            st.setString(5, getPuesto());
            st.setString(6, getEmail());
            if (getFechaTomaPosesion() != null) {
                st.setDate(7, new java.sql.Date(getFechaTomaPosesion().getTime().getTime()));
            } else {
                st.setDate(7, null);
            }
            st.setInt(8, getAnoEscolar().getId());
            st.setObject(9, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            ret = false;
            Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, "Error guardando datos de profesor: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Profesor";
    }

    @Override
    public String getDescripcionObjeto() {
        return getNombre() + " " + getApellido1() + " " + getApellido2();
    }

    public static String getNombreProfesor(int idProfesor) {
        String nombre = "-";
        try {
            Profesor m = new Profesor(idProfesor);
            nombre = m.getDescripcionObjeto();
        } catch (Exception ex) {
            Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, "Error recuperando nombre de profesor " + idProfesor, ex);
        }
        return nombre;
    }

    @Override
    public int getNumeroDeCampos() {
        return 7;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = getCodigo();
                break;
            case 1:
                val = getNombre();
                break;
            case 2:
                val = getApellido1();
                break;
            case 3:
                val = getApellido2();
                break;
            case 4:
                val = getPuesto();
                break;
            case 5:
                val = getFechaTomaPosesion();
                break;
            case 6:
                val = getEmail();
                break;
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = null;
        switch (index) {
            case 0:
                val = "Código Séneca";
                break;
            case 1:
                val = "Nombre";
                break;
            case 2:
                val = "1º Apellido";
                break;
            case 3:
                val = "2º Apellido";
                break;
            case 4:
                val = "Puesto";
                break;
            case 5:
                val = "F. Toma";
                break;
            case 6:
                val = "e-mail";
                break;
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = null;
        switch (index) {
            case 0:
                val = Integer.class;
                break;
            case 1:
                val = String.class;
                break;
            case 2:
                val = String.class;
                break;
            case 3:
                val = String.class;
                break;
            case 4:
                val = String.class;
                break;
            case 5:
                val = GregorianCalendar.class;
                break;
            case 6:
                val = String.class;
                break;
        }
        return val;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        boolean ret = false;
        switch (index) {
            case 0:
                setCodigo(Num.getInt(val));
                ret = true;
                break;
            case 1:
                setNombre(Str.noNulo(val));
                ret = true;
                break;
            case 2:
                setApellido1(Str.noNulo(val));
                ret = true;
                break;
            case 3:
                setApellido2(Str.noNulo(val));
                ret = true;
                break;
            case 4:
                setPuesto(Str.noNulo(val));
                ret = true;
                break;
            case 5:
                setFechaTomaPosesion((GregorianCalendar) val);
                ret = true;
                break;
            case 6:
                setEmail(Str.noNulo(val));
                ret = true;
                break;
        }
        if (ret) {
            ret = guardar();
        }
        return ret;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        Boolean ret = null;
        if (obj instanceof Profesor) {
            Profesor p = (Profesor) obj;
            if (p.getId() != null && getId() != null) {
                ret = getId().equals(p.getId());
            }
        }
        if (ret == null) {
            ret = super.equals(obj);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 67 * hash + (this.fechaTomaPosesion != null ? this.fechaTomaPosesion.hashCode() : 0);
        hash = 67 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        hash = 67 * hash + (this.apellido1 != null ? this.apellido1.hashCode() : 0);
        hash = 67 * hash + (this.apellido2 != null ? this.apellido2.hashCode() : 0);
        hash = 67 * hash + (this.puesto != null ? this.puesto.hashCode() : 0);
        hash = 67 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    @Override
    public String getTabla() {
        return "profesores";
    }

    @Override
    public String getNombreEmail() {
        return Str.noNulo(getNombre()) + " " + Str.noNulo(getApellido1()) + " " + Str.noNulo(getApellido2());
    }
}
