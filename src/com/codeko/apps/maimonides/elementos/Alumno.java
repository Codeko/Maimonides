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
import com.codeko.apps.maimonides.alumnos.MatriculacionAlumno;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.apps.maimonides.convivencia.Expulsion;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
public class Alumno extends ObjetoBDConCod implements IObjetoTabla, IEmailable {

    Integer codigo = null;
    String codFaltas = "";
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    String nombre = "";
    Unidad unidad = null;
    Unidad unidadAnterior = null;
    String apellido1 = "";
    String apellido2 = "";
    String numeroEscolar = null;
    boolean bilingue = false;
    boolean repetidor = false;
    boolean dicu = false;
    HashMap<String, Boolean> expulsiones = new HashMap<String, Boolean>();
    Integer idUnidad = null;
    Integer idCurso = null;
    String email = "";
    String telefono = "";
    String sms = "";
    String direccion = "";
    String cp = "";
    String poblacion = "";
    String dni = "";
    String localidadNacimiento = "";
    String provinciaNacimiento = "";
    String paisNacimiento = "España";
    String nacionalidad = "Española";
    String sexo = "H";
    GregorianCalendar fechaNacimiento = null;
    String telefonoUrgencia = "";
    String expediente = "";
    Tutor tutor = null;
    Tutor tutor2 = null;
    boolean borrado = false;
    int notificar = 0;
    public static final int NOTIFICAR_IMPRESO = 1;
    public static final int NOTIFICAR_EMAIL = 2;
    public static final int NOTIFICAR_SMS = 4;
    public static final int NOTIFICAR_TELEFONO = 8;
    public static final int NOTIFICAR_PRESENCIAL = 16;
    ArrayList<String> mensajesUltimaOperacion = new ArrayList<String>();
    String observaciones = "";
    Curso objetoCurso = null;

    public boolean isBorrado() {
        return borrado;
    }

    public void setBorrado(boolean borrado) {
        this.borrado = borrado;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCodFaltas() {
        return codFaltas;
    }

    public void setCodFaltas(String codFaltas) {
        this.codFaltas = codFaltas;
    }

    public String getExpediente() {
        return expediente;
    }

    public void setExpediente(String expediente) {
        this.expediente = expediente;
    }

    public GregorianCalendar getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(GregorianCalendar fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getLocalidadNacimiento() {
        return localidadNacimiento;
    }

    public void setLocalidadNacimiento(String localidadNacimiento) {
        this.localidadNacimiento = localidadNacimiento;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getPaisNacimiento() {
        return paisNacimiento;
    }

    public void setPaisNacimiento(String paisNacimiento) {
        this.paisNacimiento = paisNacimiento;
    }

    public String getProvinciaNacimiento() {
        return provinciaNacimiento;
    }

    public void setProvinciaNacimiento(String provinciaNacimiento) {
        this.provinciaNacimiento = provinciaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTelefonoUrgencia() {
        return telefonoUrgencia;
    }

    public void setTelefonoUrgencia(String telefonoUrgencia) {
        this.telefonoUrgencia = telefonoUrgencia;
    }

    public Tutor getTutor() {
        if (tutor == null) {
            tutor = new Tutor(getId() != null ? getId() : 0, 1);
        }
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    public Tutor getTutor2() {
        if (tutor2 == null) {
            tutor2 = new Tutor(getId() != null ? getId() : 0, 2);
        }
        return tutor2;
    }

    public void setTutor2(Tutor tutor2) {
        this.tutor2 = tutor2;
    }

    public String getObservaciones() {
        if (observaciones == null) {
            observaciones = "";
        }
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public static Alumno getAlumno(int id) throws SQLException {
        Object obj = Cache.get(Alumno.class, id);
        if (obj != null) {
            return (Alumno) obj;
        } else {
            //Al asignar el alumno desde ID se asigna automáticamente a la cache
            Alumno a = new Alumno(id);
            return a;
        }
    }

    public static Alumno getAlumno(String nombre) {
        Alumno a = null;
        PreparedStatement stSelect = null;
        ResultSet res = null;
        try {
            //TODO En esta consula deberçia enviarse un and curso_id IN (cursos de las unidades con ese nombre)
            //TODO Quizás habría que buscar primero en la cache
            stSelect = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM `alumnos` WHERE ano=? AND CONCAT(apellido1,' ',apellido2,', ',nombre)=?");
            stSelect.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            stSelect.setString(2, nombre);
            //TODO Habría que ver si hay más de un alumno con este nombre
            res = stSelect.executeQuery();
            if (res.next()) {
                a = new Alumno();
                a.cargarDesdeResultSet(res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(stSelect, res);
        }
        return a;
    }

    public static Alumno getAlumnoDesdeNumEscolar(String numEscolar) {
        Alumno a = null;
        PreparedStatement stSelect = null;
        ResultSet res = null;
        try {
            stSelect = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM `alumnos` WHERE ano=? AND numescolar=?");
            stSelect.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            stSelect.setString(2, numEscolar);
            res = stSelect.executeQuery();
            if (res.next()) {
                a = new Alumno();
                a.cargarDesdeResultSet(res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error buscando alumno por numero escolar: " + numEscolar, ex);
        } finally {
            Obj.cerrar(stSelect, res);
        }
        return a;
    }

    public static Alumno getAlumnoDesdeCodFaltas(String codFaltas) {
        Alumno a = null;
        PreparedStatement stSelect = null;
        ResultSet res = null;
        try {
            codFaltas = Str.noNulo(codFaltas).trim();
            stSelect = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM `alumnos` WHERE ano=? AND codFaltas=?");
            stSelect.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            stSelect.setString(2, codFaltas);
            res = stSelect.executeQuery();
            if (res.next()) {
                a = new Alumno();
                a.cargarDesdeResultSet(res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error buscando alumno por código de falta: " + codFaltas, ex);
        } finally {
            Obj.cerrar(stSelect, res);
        }
        return a;
    }

    public static Alumno getAlumnoDesdeCodigo(Integer codigo) {
        Alumno a = null;
        PreparedStatement stSelect = null;
        ResultSet res = null;
        try {
            stSelect = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM `alumnos` WHERE ano=? AND cod=?");
            stSelect.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            stSelect.setInt(2, codigo);
            res = stSelect.executeQuery();
            if (res.next()) {
                a = new Alumno();
                a.cargarDesdeResultSet(res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error buscando alumno por codigo: " + codigo, ex);
        } finally {
            Obj.cerrar(stSelect, res);
        }
        return a;
    }

    public ArrayList<String> getMensajesUltimaOperacion() {
        return mensajesUltimaOperacion;
    }

    public void setMensajesUltimaOperacion(ArrayList<String> mensajesUltimaOperacion) {
        this.mensajesUltimaOperacion = mensajesUltimaOperacion;
    }

    @Override
    public boolean borrar() {
        boolean ret = false;
        if (getId() != null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE alumnos SET borrado=1 WHERE id=?");
                st.setInt(1, getId());
                ret = st.executeUpdate() > 0;
                if (ret) {
                    firePropertyChange("borrado", null, true);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, "Error borrando alumno: " + this, ex);
            }
        }
        return ret;
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
        this.objetoCurso = null;
    }

    public void setCurso(Curso curso) {
        if (curso != null) {
            setIdCurso(curso.getId());
            this.objetoCurso = curso;
        } else {
            setIdCurso(null);
        }

    }

    public Curso getObjetoCurso() {
        if (this.objetoCurso == null) {
            try {
                this.objetoCurso = Curso.getCurso(getIdCurso());
            } catch (Exception ex) {
                Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.objetoCurso;
    }

    public boolean isDicu() {
        return dicu;
    }

    public void setDicu(boolean dicu) {
        this.dicu = dicu;
    }

    public boolean isExpulsado(GregorianCalendar fecha) {
        boolean expulsado = false;
        //Primero vemos si ya tenemos información sobre la expulsión
        String key = anoEscolar.getId() + "-" + Fechas.format(fecha);
        if (expulsiones.containsKey(key)) {
            expulsado = expulsiones.get(key);
        } else {
            expulsado = Expulsion.isAlumnoExpulsado(this, fecha);
            expulsiones.put(key, expulsado);
        }
        return expulsado;
    }

    public Integer getIdUnidad() {
        if (idUnidad == null && unidad != null) {
            idUnidad = unidad.getId();
        }
        return idUnidad;
    }

    public void setIdUnidad(Integer idUnidad) {
        this.idUnidad = idUnidad;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public boolean isBilingue() {
        return bilingue;
    }

    public void setBilingue(boolean bilingue) {
        this.bilingue = bilingue;
    }

    public boolean isRepetidor() {
        return repetidor;
    }

    public void setRepetidor(boolean repetidor) {
        this.repetidor = repetidor;
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

    public Unidad getUnidad() {
        if (unidad == null && idUnidad != null) {
            try {
                setUnidad(idUnidad);
            } catch (Exception ex) {
                Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error cargando unidad " + idUnidad, ex);
            }
        }
        return unidad;
    }

    public void setUnidad(int idUnidad) throws Exception {
        setUnidad(Unidad.getUnidad(idUnidad));
    }

    public void setUnidad(Unidad unidad) {
        setUnidadAnterior(this.unidad);
        this.unidad = unidad;
        setIdUnidad(null);
    }

    public Unidad getUnidadAnterior() {
        return unidadAnterior;
    }

    public void setUnidadAnterior(Unidad unidadAnterior) {
        this.unidadAnterior = unidadAnterior;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroEscolar() {
        return numeroEscolar;
    }

    public void setNumeroEscolar(String numeroEscolar) {
        this.numeroEscolar = numeroEscolar;
    }

    public String getNombreFormateado() {
        return getApellido1() + " " + getApellido2() + ", " + getNombre();
    }

    public String getNombreApellidos() {
        return getNombre() + " " + getApellido1() + " " + getApellido2();
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNotificar() {
        return notificar;
    }

    public void setNotificar(int notificacion) {
        this.notificar = notificacion;
    }

    public void setNotificar(boolean impreso, boolean email, boolean sms, boolean telefono, boolean presencial) {
        setNotificar(impreso, NOTIFICAR_IMPRESO);
        setNotificar(email, NOTIFICAR_EMAIL);
        setNotificar(sms, NOTIFICAR_SMS);
        setNotificar(telefono, NOTIFICAR_TELEFONO);
        setNotificar(presencial, NOTIFICAR_PRESENCIAL);
    }

    public void setNotificar(boolean notificar, int campo) {
        if (!notificar) {
            setNotificar(getNotificar() & ~campo);
        } else {
            setNotificar(getNotificar() | campo);
        }
    }

    public boolean isNotificar(int campo) {
        return (getNotificar() & campo) == campo;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Alumno() {
    }

    private Alumno(int id) throws SQLException {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM alumnos WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ningun alumno con ID " + id);
        }
    }

    @Override
    public boolean _guardar(boolean crearEliminados) {
        boolean ret = false;
        getMensajesUltimaOperacion().clear();
        try {
            String sql = "UPDATE alumnos SET cod=?,ano=?,nombre=?,apellido1=?,apellido2=?,unidad_id=?,curso_id=?,numescolar=?,bilingue=?,repetidor=?,dicu=?,email=?,telefono=?,sms=?,direccion=?,cp=?,poblacion=?,notificar=?,obs=?,dni=?,fnacimiento=?,loc_nacimiento=?,prov_nacimiento=?,pais_nacimiento=?,nacionalidad=?,sexo=?,telefono_urgencia=?,expediente=?,codFaltas=?,borrado=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO alumnos (cod,ano,nombre,apellido1,apellido2,unidad_id,curso_id,numescolar,bilingue,repetidor,dicu,email,telefono,sms,direccion,cp,poblacion,notificar,obs,dni,fnacimiento,loc_nacimiento,prov_nacimiento,pais_nacimiento,nacionalidad,sexo,telefono_urgencia,expediente,codFaltas,borrado,id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, Num.getInt(getCodigo()));
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, Str.noNulo(getNombre()));
            st.setString(4, Str.noNulo(getApellido1()));
            st.setString(5, Str.noNulo(getApellido2()));
            if (getUnidad() != null) {
                st.setInt(6, getUnidad().getId());
            } else {
                st.setObject(6, null);
            }
            if (Num.getInt(getIdCurso()) == 0) {
                setIdCurso(null);
            }
            st.setObject(7, getIdCurso());
            st.setString(8, Str.noNulo(getNumeroEscolar()));
            st.setBoolean(9, isBilingue());
            st.setBoolean(10, isRepetidor());
            st.setBoolean(11, isDicu());
            st.setString(12, Str.noNulo(getEmail()));
            st.setString(13, Str.noNulo(getTelefono()));
            st.setString(14, Str.noNulo(getSms()));
            st.setString(15, Str.noNulo(getDireccion()));
            st.setString(16, Str.noNulo(getCp()));
            st.setString(17, Str.noNulo(getPoblacion()));
            st.setInt(18, getNotificar());
            st.setString(19, Str.noNulo(getObservaciones()));
            st.setString(20, Str.noNulo(getDni()));
            if (getFechaNacimiento() != null) {
                st.setDate(21, new java.sql.Date(getFechaNacimiento().getTimeInMillis()));
            } else {
                st.setObject(21, null);
            }
            st.setString(22, Str.noNulo(getLocalidadNacimiento()));
            st.setString(23, Str.noNulo(getProvinciaNacimiento()));
            st.setString(24, Str.noNulo(getPaisNacimiento()));
            st.setString(25, Str.noNulo(getNacionalidad()));
            st.setString(26, getSexo());
            st.setString(27, Str.noNulo(getTelefonoUrgencia()));
            st.setString(28, Str.noNulo(getExpediente()));
            st.setString(29, Str.noNulo(getCodFaltas()));
            st.setBoolean(30, isBorrado());
            st.setObject(31, getId());
            ret = st.executeUpdate() > 0;
            if (!ret) {
                Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "No se ha podido insertar el alumno o no hay ning\u00fan alumno con ID {0}", getId());
            }
            boolean nuevaCreación = false;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
                if (getTutor() != null) {
                    getTutor().setAlumno(getId());
                }
                if (getTutor2() != null) {
                    getTutor2().setAlumno(getId());
                }
                nuevaCreación = true;

            }
            Obj.cerrar(st);
            //Ahora vemos si hay tutores
            if (getTutor() != null) {
                getTutor().guardar();
            }
            if (getTutor2() != null) {
                getTutor2().guardar();
            }
            //Ahora vemos si se ha cambiado la unidad
            if (ret && getUnidadAnterior() != null && !getUnidad().equals(getUnidadAnterior())) {
                //Entonces es que hemos cambiado de unidad y hay que rehacer la matriculación
                getMensajesUltimaOperacion().addAll(MatriculacionAlumno.reasignarHorariosAlumno(this));
            } else if (nuevaCreación) {
                //Si es insercción le asignamos los horarios (actividades básicamente)
                MatriculacionAlumno.asignarHorariosAlumnos(this);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error guardando datos de Alumno: " + this, ex);
        }
        if (ret) {
            //Si se ha guardado bien lo asignamos a la chache
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    public final boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = false;
        try {
            setBorrado(res.getBoolean("borrado"));
            setId(res.getInt("id"));
            setCodigo(res.getInt("cod"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
            setNombre(res.getString("nombre"));
            setApellido1(res.getString("apellido1"));
            setApellido2(res.getString("apellido2"));
            try {
                setUnidad(res.getInt("unidad_id"));
            } catch (Exception e) {
                Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error cargando unidad de alumno desde resultset", e);
            }
            try {
                setIdCurso(res.getInt("curso_id"));
            } catch (Exception e) {
                Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error cargando curso de alumno desde resultset", e);
            }
            setNumeroEscolar(res.getString("numescolar"));
            setBilingue(res.getBoolean("bilingue"));
            setRepetidor(res.getBoolean("repetidor"));
            setDicu(res.getBoolean("dicu"));
            setEmail(res.getString("email"));
            setTelefono(res.getString("telefono"));
            setSms(res.getString("sms"));
            setDireccion(res.getString("direccion"));
            setCp(res.getString("cp"));
            setPoblacion(res.getString("poblacion"));
            setNotificar(res.getInt("notificar"));
            setObservaciones(res.getString("obs"));
            setDni(res.getString("dni"));
            setLocalidadNacimiento(res.getString("loc_nacimiento"));
            setProvinciaNacimiento(res.getString("prov_nacimiento"));
            setPaisNacimiento(res.getString("pais_nacimiento"));
            setNacionalidad(res.getString("nacionalidad"));
            setSexo(res.getString("sexo"));
            setTelefonoUrgencia(res.getString("telefono_urgencia"));
            setExpediente(res.getString("expediente"));
            setCodFaltas(res.getString("codFaltas"));
            java.sql.Date fn = res.getDate("fnacimiento");
            if (fn != null) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(fn.getTime());
                setFechaNacimiento(cal);
            }
            Tutor t1 = new Tutor(getId(), 1);
            t1.cargarDesdeResultSet(res);
            if (t1.hayDatos()) {
                setTutor(t1);
            }
            Tutor t2 = new Tutor(getId(), 2);
            t2.cargarDesdeResultSet(res);
            if (t2.hayDatos()) {
                setTutor2(t2);
            }
            ret = true;
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, "Error cargando alumno desde resultset", ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Alumno";
    }

    @Override
    public String getDescripcionObjeto() {
        return getNombre() + " " + getApellido1() + " " + getApellido2();
    }

    @Override
    public String toString() {
        return getNombreFormateado();
    }

    @Override
    public int getNumeroDeCampos() {
        //TODO Añadir campo curso
        return 7;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = "";
        switch (index) {
            case 0:
                val = getCodigo();
                break;
            case 1:
                val = getNombreFormateado();
                break;
            case 2:
                val = getUnidad();
                break;
            case 3:
                val = getNumeroEscolar();
                break;
            case 4:
                val = isBilingue();
                break;
            case 5:
                val = isRepetidor();
                break;
            case 6:
                val = isDicu();
                break;
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String titulo = "";
        switch (index) {
            case 0:
                titulo = "Código";
                break;
            case 1:
                titulo = "Nombre";
                break;
            case 2:
                titulo = "Unidad";
                break;
            case 3:
                titulo = "N.Escolar";
                break;
            case 4:
                titulo = "Bilingüe";
                break;
            case 5:
                titulo = "Repetidor";
                break;
            case 6:
                titulo = "D.I.C.U.";
                break;
        }
        return titulo;
    }

    @Override
    public Class getClassAt(int index) {
        Class clase = null;
        switch (index) {
            case 0:
                clase = Integer.class;
                break;
            case 1:
                clase = Alumno.class;
                break;
            case 2:
                clase = Unidad.class;
                break;
            case 3:
                clase = String.class;
                break;
            case 4:
                clase = Boolean.class;
                break;
            case 5:
                clase = Boolean.class;
                break;
            case 6:
                clase = Boolean.class;
                break;
        }
        return clase;
    }

    @Override
    public boolean setValueAt(int index, Object valor) {
        boolean ret = false;
        switch (index) {
            case 0:

                break;
            case 1:

                break;
            case 2:
                setUnidad((Unidad) valor);
                ret = true;
                break;
            case 3:

                break;
            case 4:
                setBilingue((Boolean) valor);
                ret = true;
                break;
            case 5:
                setRepetidor((Boolean) valor);
                ret = true;
                break;
            case 6:
                setDicu((Boolean) valor);
                ret = true;
                break;
        }
        if (ret) {
            ret = guardar();
            if (getMensajesUltimaOperacion().size() > 0) {
                firePropertyChange("mensajesUltimaOperacion", null, this);
            }
        }
        return ret;
    }

    @Override
    public boolean esCampoEditable(int index) {
        boolean editable = false;
        switch (index) {
            case 2:
            case 4:
            case 5:
            case 6:
                editable = true;
                break;
            default:
        }
        return editable;
    }

    @Override
    public boolean equals(Object obj) {
        int idPropia = Num.getInt(getId());
        int idObj = 0;
        if (obj instanceof Alumno) {
            idObj = Num.getInt(((Alumno) obj).getId());
        }
        return idPropia != 0 && idObj == idPropia;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 97 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        hash = 97 * hash + (this.unidad != null ? this.unidad.hashCode() : 0);
        hash = 97 * hash + (this.apellido1 != null ? this.apellido1.hashCode() : 0);
        hash = 97 * hash + (this.apellido2 != null ? this.apellido2.hashCode() : 0);
        hash = 97 * hash + (this.numeroEscolar != null ? this.numeroEscolar.hashCode() : 0);
        hash = 97 * hash + (this.bilingue ? 1 : 0);
        hash = 97 * hash + (this.repetidor ? 1 : 0);
        hash = 97 * hash + (this.expulsiones != null ? this.expulsiones.hashCode() : 0);
        hash = 97 * hash + (this.idUnidad != null ? this.idUnidad.hashCode() : 0);
        return hash;
    }

    public static String getCampoOrdenNombre(String prefijo) {
        if (prefijo == null || prefijo.trim().equals("")) {
            prefijo = "";
        } else {
            if (!prefijo.endsWith(".")) {
                prefijo = prefijo + ".";
            }
        }
        //SI Se cambia esto hay que cambiar el procedimiento almacenado tambien
        return "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(CONCAT(" + prefijo + "apellido1,' '," + prefijo + "apellido2,', '," + prefijo + "nombre)),'á','a'),'é','e'),'í','i'),'ó','o'),'ú','u')";
    }

    public boolean existe() {
        boolean ret = false;
        if (getId() != null && getId() > 0) {
            ret = true;
        } else {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM alumnos WHERE borrado=0 AND ano=? AND nombre=? AND apellido1=? AND apellido2=? AND curso_id=?");
                st.setInt(1, getAnoEscolar().getId());
                st.setString(2, getNombre());
                st.setString(3, getApellido1());
                st.setString(4, getApellido2());
                st.setInt(5, getIdCurso());
                ResultSet res = st.executeQuery();
                ret = res.next();
                if (ret) {
                    cargarDesdeResultSet(res);
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(Alumno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;

    }

    @Override
    public String getTabla() {
        return "alumnos";
    }

    public static ArrayList<Alumno> getAlumnos() {
        ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT * FROM alumnos WHERE borrado=0 AND ano=?");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                Alumno a = new Alumno();
                a.cargarDesdeResultSet(res);
                alumnos.add(a);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return alumnos;
    }

    @Override
    public String getNombreEmail() {
        return getNombreApellidos();
    }
}
