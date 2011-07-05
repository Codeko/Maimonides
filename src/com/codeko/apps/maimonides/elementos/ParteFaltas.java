package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.asistencia.ConfiguracionAsistencia;
import com.codeko.apps.maimonides.digitalizacion.CacheImagenes;
import com.codeko.apps.maimonides.digitalizacion.ConfiguracionParte;
import com.codeko.apps.maimonides.digitalizacion.DatoDigitalizacion;
import com.codeko.apps.maimonides.digitalizacion.DigitalizacionParte;
import com.codeko.apps.maimonides.digitalizacion.MensajeDigitalizacion;
import com.codeko.apps.maimonides.excepciones.NoExisteElementoException;
import com.codeko.apps.maimonides.partes.AsistenciaAlumno;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Copyright Codeko Informática 2008
 * @author Codeko
 */
public class ParteFaltas extends ObjetoBD implements IObjetoTabla {
    //TODO Mover a una clase de configuración

    public static final int FALTA_INDETERMINADA = 0;
    public static final int FALTA_ASISTENCIA = 1;
    public static final int FALTA_INJUSTIFICADA = 2;
    public static final int FALTA_EXPULSION = 3;
    public static final int FALTA_JUSTIFICADA = 4;
    public static final int FALTA_RETRASO = 5;
    public static final int FALTA_INJUSTIFICADAS_RETRASOS = 6;
    public static final int DICU_AMBOS = 2;
    public static final int DICU_SOLO = 1;
    public static final int DICU_NO = 0;
    AnoEscolar anoEscolar = null;
    String curso = null;
    ArrayList<Unidad> unidades = new ArrayList<Unidad>();
    GregorianCalendar fecha = null;
    boolean digitalizado = false;
    boolean necesitaRevision = false;
    boolean justificado = false;
    boolean procesado = false;
    boolean apoyo = false;
    GregorianCalendar fechaRevision = null;
    ArrayList<Horario> horarios = new ArrayList<Horario>();
    ArrayList<Horario> horariosDivididos = new ArrayList<Horario>();
    boolean primario = false;
    ArrayList<String> cabeceras = null;
    ArrayList<String> cabecerasCompletas = null;
    ArrayList<Alumno> alumnos = null;
    ArrayList<AsistenciaAlumno> asistencia = null;
    String textoPie = null;
    Boolean dicu = null;
    ArrayList<Materia> materias = null;
    boolean enviado = false;
    File archivoImagen = null;
    //BufferedImage imagen = null;
    boolean noTieneImagen = false;
    String profesorParte = null;
    Thread threadGuardadoImagenes = null;

    public void borrarImagen() {
        noTieneImagen = true;
        setArchivoImagen(null);
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes_imagenes WHERE parte_id=?");
            st.setInt(1, getId());
            st.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
    }

    public File getArchivoImagen() {
        if (threadGuardadoImagenes != null && threadGuardadoImagenes.isAlive()) {
            try {
                threadGuardadoImagenes.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return archivoImagen;
    }

    public void limpiar() {
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes_alumnos SET asistencia=" + FALTA_INDETERMINADA + " WHERE parte_id=?");
            st.setInt(1, getId());
            st.executeUpdate();
            st.close();
            marcarNoProcesadoNoEnviado();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st);
        }
    }

    public void quitarIndeterminados() {
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes_alumnos SET asistencia=" + FALTA_ASISTENCIA + " WHERE parte_id=? AND asistencia=" + FALTA_INDETERMINADA + "");
            st.setInt(1, getId());
            st.executeUpdate();
            st.close();
            marcarNoProcesadoNoEnviado();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st);
        }
    }

    public void marcarNoProcesadoNoEnviado() {
        //Siempre que se actualice una asistencia el parte deja de estar enviado y procesado
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes SET enviado=0,procesado=0 WHERE id=? ");
            st.setInt(1, getId());
            st.executeUpdate();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st);
        }
    }

    public void setArchivoImagen(File archivoImagen) {
        this.archivoImagen = archivoImagen;
    }

    public synchronized BufferedImage getImagen() {
        return CacheImagenes.getImagenParte(this, 1);
    }

    public BufferedImage getImagen_() {
        BufferedImage imagen = null;
        long t = System.currentTimeMillis();
        if (getArchivoImagen() == null && getId() != null && !noTieneImagen) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM partes_imagenes WHERE parte_id=?");
                st.setInt(1, getId());
                ResultSet res = st.executeQuery();
                if (res.next()) {
                    try {
                        InputStream is = res.getBinaryStream("imagen");
                        if (is != null) {
                            imagen = ImageIO.read(is);
                            long t2 = System.currentTimeMillis();
                            Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Se ha tardado {0}ms en descargar la imagen.", (t2 - t));
                            guardarArchivoImagen(imagen);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error cargando imagen de parte:" + getId(), ex);
                    }
                } else {
                    noTieneImagen = true;
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error recuperando imagen de parte.", ex);
            }
        }
        if (imagen == null && getArchivoImagen() != null && getArchivoImagen().exists()) {
            Logger.getLogger(ParteFaltas.class.getName()).info("Cargando imagen desde archivo");
            try {
                imagen = ImageIO.read(getArchivoImagen());
            } catch (IOException ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return imagen;
    }

    public boolean hayImagen() {
        boolean hay = false;
        if (!noTieneImagen) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT 0 FROM partes_imagenes WHERE parte_id=?");
                st.setInt(1, getId());
                ResultSet res = st.executeQuery();
                if (res.next()) {
                    hay = true;
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error recuperando imagen de parte.", ex);
            }
        }
        return hay;
    }

    public String getProfesorParte() {
        if (profesorParte == null) {
            ArrayList<Integer> profs = new ArrayList<Integer>();
            for (Horario h : getHorarios()) {
                Integer idP = h.getProfesor();
                if (!profs.contains(idP)) {
                    profs.add(idP);
                }
            }

            if (profs.size() < 3) {
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < 2 && i < profs.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    try {
                        sb.append(Profesor.getProfesor(profs.get(i)));
                    } catch (Exception ex) {
                        Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                profesorParte = sb.toString();
            } else {
                profesorParte = "";//TODO Sería cosa de poner las iniciales
            }

        }
        return profesorParte;
    }

    public synchronized void guardarArchivoImagen(final BufferedImage imagen) {
        if (threadGuardadoImagenes != null && threadGuardadoImagenes.isAlive()) {
            try {
                threadGuardadoImagenes.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        threadGuardadoImagenes = new Thread() {

            @Override
            public void run() {
                String ext = ConfiguracionParte.getConfiguracion().getExtensionImagenes();
                try {
                    File tmpImg = File.createTempFile("img_parte", ext);
                    ImageIO.write(imagen, ext, tmpImg);
                    setArchivoImagen(tmpImg);
                } catch (IOException ex) {
                    Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        threadGuardadoImagenes.start();
    }

    public boolean isEnviado() {
        return enviado;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
    }

    public Boolean isDicu() {
        //TODO Si esto se deja así quitar la variable dicu.
        //Todos los horarios deben ser dicu
        // boolean dicu = false;
        if (getHorarios().size() > 0) {
            dicu = true;
            for (Horario h : getHorarios()) {
                if (!h.isDicu()) {
                    dicu = false;
                    break;
                }
            }
        } else {
            dicu = false;
        }
        return dicu;
    }

    public void resetearListaAlumnos() {
        alumnos = null;
    }

    public void resetearCabeceras() {
        cabeceras = null;
    }

    public void resetearCabecerasCompletas() {
        cabecerasCompletas = null;
    }

    public void resetearAsistencia() {
        asistencia = null;
    }

    public static Color getColorTipoFalta(Object tipo) {
        Color c = null;
        if (Num.esNumero(tipo)) {
            int num = Num.getInt(tipo);
            switch (num) {
                case FALTA_ASISTENCIA:
                    c = Color.decode("#CCFFFF");
                    break;
                case FALTA_EXPULSION:
                    c = Color.ORANGE;
                    break;
                case FALTA_INDETERMINADA:
                    c = Color.WHITE;
                    break;
                case FALTA_INJUSTIFICADA:
                    c = Color.decode("#FFFF99");
                    break;
                case FALTA_JUSTIFICADA:
                    c = Color.decode("#CCFFCC");
                    break;
                case FALTA_RETRASO:
                    c = Color.decode("#99CC99");
                    break;
            }
        } else {
            String valor = Str.noNulo(tipo).trim();
            if (valor.equals("")) {
                c = Color.WHITE;
            } else if (valor.equals("A")) {
                c = Color.decode("#CCFFFF");
            } else if (valor.equals("E")) {
                c = Color.ORANGE;
            } else if (valor.equals("I")) {
                c = Color.decode("#FFFF99");
            } else if (valor.equals("J")) {
                c = Color.decode("#CCFFCC");
            } else if (valor.equals("R")) {
                c = Color.decode("#99CC99");
            }
        }
        return c;
    }

    public static String getCodigoTipoFalta(int tipo) {
        String c = null;
        switch (tipo) {
            case FALTA_ASISTENCIA:
                c = "A";
                break;
            case FALTA_EXPULSION:
                c = "E";
                break;
            case FALTA_INDETERMINADA:
                c = "?";
                break;
            case FALTA_INJUSTIFICADA:
                c = "I";
                break;
            case FALTA_JUSTIFICADA:
                c = "J";
                break;
            case FALTA_RETRASO:
                c = "R";
                break;
            case FALTA_INJUSTIFICADAS_RETRASOS:
                c = "IR";
                break;
        }
        return c;
    }

    public static String getNombreTipoFalta(Object tipo) {
        String c = null;
        if (Num.esNumero(tipo)) {
            int num = Num.getInt(tipo);
            switch (num) {
                case FALTA_ASISTENCIA:
                    c = "Asistencia";
                    break;
                case FALTA_EXPULSION:
                    c = "Expulsado";
                    break;
                case FALTA_INDETERMINADA:
                    c = "Indeterminado";
                    break;
                case FALTA_INJUSTIFICADA:
                    c = "Falta Injustificada";
                    break;
                case FALTA_JUSTIFICADA:
                    c = "Falta Justificada";
                    break;
                case FALTA_RETRASO:
                    c = "Retraso";
                    break;
            }
        } else {
            String valor = Str.noNulo(tipo).trim();
            if (valor.equals("")) {
                c = "Indeterminado";
            } else if (valor.equals("A")) {
                c = "Asistencia";
            } else if (valor.equals("E")) {
                c = "Expulsado";
            } else if (valor.equals("I")) {
                c = "Falta Injustificada";
            } else if (valor.equals("J")) {
                c = "falta Justificada";
            } else if (valor.equals("R")) {
                c = "Retraso";
            }
        }
        return c;
    }

    public ParteFaltas() {
    }

    public ParteFaltas(int id) throws SQLException, NoExisteElementoException {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM partes WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new NoExisteElementoException("No existe el parte de faltas " + id);
        }

    }

    public ArrayList<Alumno> getAlumnos() {
        if (alumnos == null) {
            alumnos = new ArrayList<Alumno>();
            String sql = "SELECT distinct a.* FROM alumnos AS a "
                    + "JOIN partes_alumnos As pa ON pa.alumno_id=a.id  "
                    + "JOIN unidades As u ON u.id=a.unidad_id  "
                    + "WHERE pa.parte_id=? "
                    + "ORDER BY pa.posicion ";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getId());
                res = st.executeQuery();
                while (res.next()) {
                    Alumno a = new Alumno();
                    a.cargarDesdeResultSet(res);
                    alumnos.add(a);
                }

            } catch (SQLException ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error recupernado alumnos", ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return alumnos;
    }

    public boolean isApoyo() {
        return apoyo;
    }

    public void setApoyo(boolean apoyo) {
        this.apoyo = apoyo;
    }

    public boolean isPrimario() {
        return primario;
    }

    public void setPrimario(boolean primario) {
        this.primario = primario;
    }

    public ArrayList<Horario> getHorariosDivididos() {
        return horariosDivididos;
    }

    public void setHorariosDivididos(ArrayList<Horario> horariosDivididos) {
        this.horariosDivididos = horariosDivididos;
    }

    public GregorianCalendar getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(GregorianCalendar fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public boolean isJustificado() {
        return justificado;
    }

    public void setJustificado(boolean justificado) {
        this.justificado = justificado;
    }

    public boolean isProcesado() {
        return procesado;
    }

    public void setProcesado(boolean procesado) {
        this.procesado = procesado;
    }

    public AnoEscolar getAnoEscolar() {
        if (anoEscolar == null) {
            anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
        }
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public ArrayList<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(ArrayList<Horario> horarios) {
        this.horarios = horarios;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public boolean isDigitalizado() {
        return digitalizado;
    }

    public void setDigitalizado(boolean digitalizado) {
        this.digitalizado = digitalizado;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public boolean isNecesitaRevision() {
        return necesitaRevision;
    }

    public void setNecesitaRevision(boolean necesitaRevision) {
        this.necesitaRevision = necesitaRevision;
    }

    public ArrayList<Unidad> getUnidades() {
        return unidades;
    }

    public void setUnidades(ArrayList<Unidad> unidades) {
        this.unidades = unidades;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        PreparedStatement st = null;
        try {
            String sql = "UPDATE partes SET ano=?,curso=?,unidad_id=?,descripcion=?,fecha=?,digitalizado=?,necesita_revision=?,fecha_revision=?,justificado=?,procesado=?,primario=?,apoyo=?,enviado=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO partes (ano,curso,unidad_id,descripcion,fecha,digitalizado,necesita_revision,fecha_revision,justificado,procesado,primario,apoyo,enviado,id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            st.setInt(1, getAnoEscolar().getId());
            st.setString(2, getCurso());
            if (getUnidades().size() == 1) {
                st.setInt(3, getUnidades().get(0).getId());
            } else {
                st.setObject(3, null);
            }
            st.setString(4, getDescripcionObjeto());
            st.setDate(5, new java.sql.Date(getFecha().getTime().getTime()));
            st.setBoolean(6, isDigitalizado());
            st.setBoolean(7, isNecesitaRevision());
            if (getFechaRevision() != null) {
                st.setDate(8, new java.sql.Date(getFechaRevision().getTime().getTime()));
            } else {
                st.setObject(8, null);
            }
            st.setBoolean(9, isJustificado());
            st.setBoolean(10, isProcesado());
            st.setBoolean(11, isPrimario());
            st.setBoolean(12, isApoyo());
            st.setBoolean(13, isEnviado());
            st.setObject(14, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
                //Ahora borramos las lineas y las volvemos a crear
                //TODO Confirmar que esto realmente solo es necesario en la creación.
                guardarLineasHorarios();
                guardarLineasUnidades();
                guardarLineasAlumnos();
            }
            guardarImagen();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error guardando parte de faltas:\n" + this, ex);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return ret;
    }

    public final boolean cargarDesdeResultSet(ResultSet res) {
        return cargarDesdeResultSet(res, "");
    }

    public boolean cargarDesdeResultSet(ResultSet res, String prefijo) {
        boolean ret = false;
        try {
            setId(res.getInt(prefijo + "id"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt(prefijo + "ano")));
            setCurso(res.getString(prefijo + "curso"));
            setFecha(Fechas.toGregorianCalendar(res.getDate(prefijo + "fecha")));
            setDigitalizado(res.getBoolean(prefijo + "digitalizado"));
            setNecesitaRevision(res.getBoolean(prefijo + "necesita_revision"));
            setFechaRevision(Fechas.toGregorianCalendar(res.getDate(prefijo + "fecha_revision")));
            setJustificado(res.getBoolean(prefijo + "justificado"));
            setProcesado(res.getBoolean(prefijo + "procesado"));
            setPrimario(res.getBoolean(prefijo + "primario"));
            setApoyo(res.getBoolean(prefijo + "apoyo"));
            setEnviado(res.getBoolean(prefijo + "enviado"));
            //Ahora cargamos las lineas de horarios normales y divididos
            cargarHorarios();
            cargarUnidades();
            ret = true;
        } catch (Exception ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Parte de faltas";
    }

    public String getNombreParte() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCurso());
        sb.append(" ");
        boolean primero = true;
        setUnidades(Unidad.ordenarPorPosicion(getUnidades()));
        for (Unidad u : getUnidades()) {
            if (primero) {
                primero = false;
            } else {
                sb.append("/");
            }
            sb.append(u.getGrupo());
        }
        return sb.toString();
    }

    @Override
    public String getDescripcionObjeto() {
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        setUnidades(Unidad.ordenarPorPosicion(getUnidades()));
        sb.append(getUnidades().get(0).getCurso());
        sb.append(" ");
        for (Unidad u : getUnidades()) {
            if (primero) {
                primero = false;
            } else {
                sb.append("/");
            }
            sb.append(u.getGrupo());
        }
        if (isApoyo()) {
            sb.append(" Apoyo");
        } else {
            if (!isPrimario()) {
                ArrayList<Integer> horas = getHorasDistintas(getHorarios());
                Collections.sort(horas);
                sb.append(" [");
                for (Integer i : horas) {
                    sb.append(i);
                }
                sb.append("]");
                ArrayList materiasDistintas = getMateriasHorariosDistintas(getHorarios(), horas);
                sb.append(" ");
                boolean primeraMat = true;
                for (Object obj : materiasDistintas) {
                    if (!primeraMat) {
                        sb.append("-");
                    } else {
                        primeraMat = false;
                    }
                    if (obj instanceof Materia) {
                        Materia m = (Materia) obj;
                        sb.append(m.getCodigoMateria());
                    } else if (obj instanceof Actividad) {
                        Actividad a = (Actividad) obj;
                        sb.append(a.getCodigoActividad());
                    }
                }
            }
        }
        if (isDicu()) {
            sb.append(" - D.I.C.U.");
        }
        return sb.toString();
    }

    public ArrayList<Materia> getMaterias() {
        if (materias == null) {
            materias = new ArrayList<Materia>();
            ArrayList<Integer> horas = getHorasDistintas(getHorarios());
            Collections.sort(horas);
            ArrayList<Materia> codMaterias = getMateriasDistintias(getHorarios(), horas);
            for (Materia m : codMaterias) {
                materias.add(m);
            }
        }
        return materias;
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    public String toTexto() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(getDescripcionObjeto());
        sb.append("\nHoras:");
        for (Horario h : getHorarios()) {
            sb.append("\n\t");
            sb.append(h.getHora());
            sb.append(": ");
            try {
                Materia m = Materia.getMateria(h.getMateria());
                sb.append(m.getDescripcion());
                sb.append("(");
                sb.append(m.getId()).append(":").append(m.getCodigo());
                sb.append(")");
            } catch (Exception ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error cargando materia: " + h.getMateria(), ex);
            }
        }
        sb.append("\nHoras Divididas:");
        for (Horario h : getHorariosDivididos()) {
            sb.append("\n\t");
            sb.append(h.getHora());
            sb.append(": ");
            try {
                Materia m = Materia.getMateria(h.getMateria());
                sb.append(m.getDescripcion());
                sb.append("(");
                sb.append(m.getId()).append(":").append(m.getCodigo());
                sb.append(")");
            } catch (Exception ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error cargando materia: " + h.getMateria(), ex);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private void addSeparadorCabecera(int cont, StringBuilder sb) {
        if (cont == 1) {
            if (getHorarios().size() > 0) {
                sb.append("-");
            } else {
                sb.append("/");
            }
        } else if (cont > 1) {
            sb.append("/");
        }
    }

    public ArrayList<String> getCabeceras() {
        //TODO Ante cambios de horarios habría que rehacer las cabeceras
        if (cabeceras == null) {
            cabeceras = new ArrayList<String>();
            ArrayList<Horario> hors = new ArrayList<Horario>(getHorarios());
            hors.addAll(getHorariosDivididos());
            for (int i = 1; i < 7; i++) {
                if (isApoyo()) {
                    cabeceras.add(i + "ª");
                } else {
                    //Para cada hora buscamos en todos los horarios
                    StringBuilder sb = new StringBuilder();
                    //En cada cabecera no queremos que se repitan las materias
                    ArrayList<Materia> listaMaterias = new ArrayList<Materia>();
                    ArrayList<Integer> actividades = new ArrayList<Integer>();
                    int cont = 0;
//                Juntamos horarios y horarios divididos
                    for (Horario h : hors) {
                        if (h.getHora() == i) {
                            //Vemos si la materia no es nula
                            if (h.getMateria() != null && h.getMateria() > 0) {
                                Materia m = h.getObjetoMateria();
                                if (!hayMateriaEquivalente(listaMaterias, m)) {
                                    listaMaterias.add(m);
                                    addSeparadorCabecera(cont, sb);
                                    sb.append(Materia.getCodigoMateria(h.getMateria()));
                                    cont++;
                                }
                            } else if (h.getActividad() != null) {
                                if (!actividades.contains(h.getActividad())) {
                                    actividades.add(h.getActividad());
                                    addSeparadorCabecera(cont, sb);
                                    sb.append(Actividad.getCodigoActividad(h.getActividad()));
                                    cont++;
                                }
                            }
                        }
                    }
                    listaMaterias = null;
                    actividades = null;

                    String cab = sb.toString();
                    if (cab.equals("")) {
                        cab = "-";
                    }
                    cabeceras.add(cab);
                }
            }
            hors = null;
        }
        return cabeceras;
    }

    private static boolean hayMateriaEquivalente(Collection lista, Materia materia) {
        for (Object m : lista) {
            if (materia.esEquivalente(m)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getCabecerasCompletas() {
        //TODO Ante cambios de horarios habría que rehacer las cabeceras
        //TODO Esta funcion debería estar fundida con la de getCabeceras
        if (cabecerasCompletas == null) {
            cabecerasCompletas = new ArrayList<String>();
            ArrayList<Horario> hors = new ArrayList<Horario>(getHorarios());
            hors.addAll(getHorariosDivididos());
            for (int i = 1; i < 7; i++) {
                if (isApoyo()) {
                    cabecerasCompletas.add(i + "ª Hora");
                } else {
                    //Para cada hora buscamos en todos los horarios
                    StringBuilder sb = new StringBuilder();
                    //En cada cabecera no queremos que se repitan las materias
                    ArrayList<Materia> listaMaterias = new ArrayList<Materia>();
                    ArrayList<Integer> actividades = new ArrayList<Integer>();
                    int cont = 0;
//                Juntamos horarios y horarios divididos
                    for (Horario h : hors) {
                        if (h.getHora() == i) {
                            //Vemos si la materia no es nula
                            if (h.getMateria() != null && h.getMateria() > 0) {
                                Materia m = h.getObjetoMateria();
                                if (!hayMateriaEquivalente(listaMaterias, m)) {
                                    listaMaterias.add(m);
                                    addSeparadorCabecera(cont, sb);
                                    sb.append(Materia.getNombreMateria(h.getMateria()));
                                    cont++;
                                }
                            } else if (h.getActividad() != null) {
                                if (!actividades.contains(h.getActividad())) {
                                    actividades.add(h.getActividad());
                                    addSeparadorCabecera(cont, sb);
                                    sb.append(Actividad.getNombreActividad(h.getActividad()));
                                    cont++;
                                }
                            }
                        }
                    }
                    listaMaterias = null;
                    actividades = null;

                    String cab = sb.toString();
                    if (cab.equals("")) {
                        cab = "-";
                    }
                    cabecerasCompletas.add(cab);
                }
            }
            hors = null;
        }
        return cabecerasCompletas;
    }

    public void asignarPosicionAlumnos() throws SQLException, SQLException {
        //Ahora de todos hay que asignar el Nº
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("CALL asignarPosicionLineasParte(?)");
        st.setInt(1, getId());
        st.executeUpdate();
        st.close();
    }

    private void cargarHorarios() {
        getHorarios().clear();
        getHorariosDivididos().clear();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM partes_horarios WHERE parte_id=?");
            st.setInt(1, getId());
            res = st.executeQuery();
            while (res.next()) {
                int idHorario = res.getInt("horario_id");
                boolean dividido = res.getBoolean("dividido");
                Horario h = Horario.getHorario(idHorario);
                if (dividido) {
                    getHorariosDivididos().add(h);
                } else {
                    getHorarios().add(h);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error cargando horarios de parte de faltas " + getId(), ex);
        } finally {
            Obj.cerrar(st, res);
        }
    }

    private void cargarUnidades() {
        getUnidades().clear();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM partes_unidades WHERE parte_id=? ");
            st.setInt(1, getId());
            res = st.executeQuery();
            while (res.next()) {
                int sqlUnidad = res.getInt("unidad_id");
                if (sqlUnidad > 0) {
                    getUnidades().add(Unidad.getUnidad(sqlUnidad));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error cargando unidades de parte de faltas " + getId(), ex);
        } finally {
            Obj.cerrar(st, res);
        }
    }

    private void guardarImagen() {
        if (getArchivoImagen() != null && getArchivoImagen().exists()) {
            try {
                ByteArrayOutputStream buffer_img = new ByteArrayOutputStream();
                ImageIO.write(getImagen(), "png", buffer_img);
                InputStream fis = new ByteArrayInputStream(buffer_img.toByteArray());
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("REPLACE partes_imagenes SET parte_id=?,imagen=?");
                st.setInt(1, getId());
                st.setBinaryStream(2, fis);
                st.executeUpdate();
                Obj.cerrar(fis, st, buffer_img);
            } catch (Exception ex) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error guardando imagen del parte.", ex);
            }
        }
    }

    private void guardarLineasAlumnos() throws SQLException {
        String sql = "INSERT INTO partes_alumnos(parte_id,horario_id,alumno_id,asistencia,posicion,estado) SELECT ph.parte_id AS parte_id,h.id AS horario_id,a.id AS alumno_id," + FALTA_INDETERMINADA + ",0,0"
                + " FROM horarios AS h"
                + " JOIN partes_horarios AS ph ON ph.horario_id=h.id"
                + " LEFT JOIN unidades AS u ON u.id=h.unidad_id "
                + " LEFT JOIN materias AS m ON m.id=h.materia_id  "
                + " JOIN materias_alumnos As ma ON ma.materia_id=m.id "
                + " JOIN alumnos AS a ON a.unidad_id=u.id AND ma.alumno_id=a.id AND (h.dicu=" + ParteFaltas.DICU_AMBOS + " OR a.dicu=h.dicu)"
                + " LEFT JOIN apoyos_alumnos AS aa ON aa.alumno_id=a.id AND aa.horario_id=h.id "
                + " JOIN alumnos_horarios AS ah ON ah.alumno_id=a.id AND ah.horario_id=h.id "
                + " WHERE a.borrado=0 AND ph.parte_id=? AND aa.alumno_id " + (isApoyo() ? " IS NOT NULL " : " IS NULL ") + " AND ah.activo=1 AND ph.dividido=0 "
                + " ORDER BY h.hora,h.profesor_id";
        //TODO DICU Ver tema dicu: AND h.dicu=IF(h.dicu=1,a.dicu,0)
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, getId());
        st.executeUpdate();
        st.close();

        //Ahora tenemos que añadir las actividades
        sql = "INSERT INTO partes_alumnos(parte_id,horario_id,alumno_id,asistencia,posicion,estado) SELECT ph.parte_id AS parte_id,h.id AS horario_id,a.id AS alumno_id," + FALTA_INDETERMINADA + ",0,0 "
                + " FROM horarios AS h"
                + " JOIN tramos AS t ON t.id=h.tramo_id  "
                + " JOIN partes_horarios AS ph ON ph.horario_id=h.id"
                + " LEFT JOIN unidades AS u ON u.id=h.unidad_id "
                + " JOIN alumnos AS a ON a.unidad_id=u.id AND (h.dicu=" + ParteFaltas.DICU_AMBOS + " OR a.dicu=h.dicu) "
                + " JOIN alumnos_horarios AS ah ON ah.alumno_id=a.id AND ah.horario_id=h.id AND ah.activo=1 "
                + " WHERE a.borrado=0 AND ph.parte_id=? AND h.materia_id IS NULL "
                + " ORDER BY h.hora,h.profesor_id";
        st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, getId());
        st.executeUpdate();
        st.close();
        asignarPosicionAlumnos();
    }

    public void insertarLineaAlumno(Alumno alumno, Horario horario) throws SQLException {
        String sql = "INSERT INTO partes_alumnos(parte_id,horario_id,alumno_id,asistencia,posicion,estado) VALUES(?,?,?," + FALTA_INDETERMINADA + ",0,0)";
        //TODO DICU Ver tema dicu: AND h.dicu=IF(h.dicu=1,a.dicu,0) NO SE POEUE ESTA ESTOU SQUI
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, getId());
        st.setInt(2, horario.getId());
        st.setInt(3, alumno.getId());
        st.executeUpdate();
        st.close();
        asignarPosicionAlumnos();
    }

    private void guardarLineasHorarios() throws SQLException {
        //TODO Si sólo se ejecuta al crearse no es necesario el delete. En otro caso cuidado con el repalce
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes_horarios WHERE parte_id=?");
        st.setInt(1, getId());
        st.executeUpdate();
        st.close();
        //Y Añadimos las lineas
        PreparedStatement stLin = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("REPLACE partes_horarios SET parte_id=?,horario_id=?,dividido=?,firmado=?");
        stLin.setInt(1, getId());
        for (Horario h : getHorarios()) {
            stLin.setInt(2, h.getId());
            stLin.setBoolean(3, false);
            stLin.setBoolean(4, false);
            stLin.executeUpdate();
        }
        for (Horario h : getHorariosDivididos()) {
            stLin.setInt(2, h.getId());
            stLin.setBoolean(3, true);
            stLin.setBoolean(4, false);
            stLin.executeUpdate();
        }
        stLin.close();
    }

    private void guardarLineasUnidades() throws SQLException {
        //TODO Si sólo se ejecuta al crearse no es necesario el delete. En otro caso cuidado con el repalce
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes_unidades WHERE parte_id=?");
        st.setInt(1, getId());
        st.executeUpdate();
        st.close();
        PreparedStatement stLin = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("REPLACE partes_unidades SET parte_id=?,unidad_id=?");
        stLin.setInt(1, getId());
        for (Unidad u : getUnidades()) {
            stLin.setInt(2, u.getId());
            stLin.addBatch();
        }
        stLin.executeBatch();
        stLin.close();
    }

    public String getTextoPie() {
        if (textoPie == null) {
            StringBuilder sb = new StringBuilder("Asignaturas de este parte:\n");
            for (int i = 1; i < 7; i++) {
                StringBuilder linea = new StringBuilder(i + "ª Hora: ");
                boolean hayDatos = false;
                for (Horario h : getHorarios()) {
                    if (h.getHora().intValue() == i) {
                        if (hayDatos) {
                            linea.append(" / ");
                        }
                        if (h.getMateria() != null && h.getMateria() > 0) {
                            linea.append(h.getObjetoMateria().getDescripcion());
                        } else {
                            linea.append(h.getObjetoActividad().getDescripcion());
                        }
                        try {
                            Unidad u = Unidad.getUnidad(h.getUnidad());
                            linea.append(" [");
                            linea.append(u.getGrupo());
                            linea.append("]");
                        } catch (Exception ex) {
                            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error recuperando unidad id: " + h.getUnidad(), ex);
                        }
                        hayDatos = true;
                    }
                }
                if (hayDatos) {
                    sb.append(linea.toString());
                    if (i % 3 == 0) {
                        sb.append("\n");
                    } else {
                        sb.append("  ");
                    }
                }
            }
            textoPie = sb.toString();

        }
        return textoPie;
    }

    public static ArrayList<Integer> getHorasDistintas(Collection<Horario> horarios) {
        ArrayList<Integer> horas = new ArrayList<Integer>();
        for (Horario h : horarios) {
            if (!horas.contains(h.getHora())) {
                horas.add(h.getHora());
            }
        }
        return horas;
    }

    public static ArrayList<Materia> getMateriasDistintias(Collection<Horario> horarios, Collection<Integer> horas) {
        ArrayList<Materia> materias = new ArrayList<Materia>();
        for (Integer i : horas) {
            for (Horario h : horarios) {
                if (h.getHora() == i) {
                    if (h.getMateria() != null && h.getMateria() > 0) {
                        Materia m = h.getObjetoMateria();
                        if (!hayMateriaEquivalente(materias, m)) {
                            materias.add(m);
                        }
                    }
                }
            }
        }
        return materias;
    }

    public static ArrayList<Object> getMateriasHorariosDistintas(Collection<Horario> horarios, Collection<Integer> horas) {
        ArrayList<Object> materiasHorarios = new ArrayList<Object>();
        for (Integer i : horas) {
            for (Horario h : horarios) {
                if (h.getHora() == i) {
                    if (h.getMateria() != null && h.getMateria() > 0) {
                        Materia m = h.getObjetoMateria();
                        if (!hayMateriaEquivalente(materiasHorarios, m)) {
                            materiasHorarios.add(m);
                        }
                    } else {
                        if (!materiasHorarios.contains(h.getActividad())) {
                            materiasHorarios.add(h.getObjetoActividad());
                        }
                    }
                }
            }
        }
        return materiasHorarios;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof ParteFaltas) {
            ParteFaltas p = (ParteFaltas) obj;
            if (p.getId() != null) {
                if (this.getId() != null) {
                    ret = this.getId().equals(p.getId());
                }
            } else {
                //Si no tenemos codigoBarras solo lo consideramos igual si es el mismo objeto.
                ret = super.equals(obj);
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 23 * hash + (this.curso != null ? this.curso.hashCode() : 0);
        hash = 23 * hash + (this.unidades != null ? this.unidades.hashCode() : 0);
        hash = 23 * hash + (this.fecha != null ? this.fecha.hashCode() : 0);
        hash = 23 * hash + (this.digitalizado ? 1 : 0);
        hash = 23 * hash + (this.necesitaRevision ? 1 : 0);
        hash = 23 * hash + (this.justificado ? 1 : 0);
        hash = 23 * hash + (this.procesado ? 1 : 0);
        hash = 23 * hash + (this.apoyo ? 1 : 0);
        hash = 23 * hash + (this.fechaRevision != null ? this.fechaRevision.hashCode() : 0);
        hash = 23 * hash + (this.horarios != null ? this.horarios.hashCode() : 0);
        hash = 23 * hash + (this.horariosDivididos != null ? this.horariosDivididos.hashCode() : 0);
        hash = 23 * hash + (this.primario ? 1 : 0);
        hash = 23 * hash + (this.cabeceras != null ? this.cabeceras.hashCode() : 0);
        hash = 23 * hash + (this.cabecerasCompletas != null ? this.cabecerasCompletas.hashCode() : 0);
        hash = 23 * hash + (this.alumnos != null ? this.alumnos.hashCode() : 0);
        hash = 23 * hash + (this.textoPie != null ? this.textoPie.hashCode() : 0);
        return hash;
    }
    ArrayList<String> erroresDigitalizacion = new ArrayList<String>();
    ArrayList<MensajeDigitalizacion> advertenciasDigitalizacion = new ArrayList<MensajeDigitalizacion>();

    public ArrayList<String> getErroresDigitalizacion() {
        return erroresDigitalizacion;
    }

    public ArrayList<MensajeDigitalizacion> getAdvertenciasDigitalizacion() {
        return advertenciasDigitalizacion;
    }

    public boolean aplicarDigitalizacion(DigitalizacionParte digitalizacionParte, boolean forzar) {
        Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Aplicando digitalizaci\u00f3n de parte:{0}", digitalizacionParte.getCodigoBarras());
        getErroresDigitalizacion().clear();
        getAdvertenciasDigitalizacion().clear();
        boolean ret = true;
        //TODO Si está marcado como forzar hay que vaciar el parte y borrar las anteriores advertencias
        if (isDigitalizado() && !forzar) {
            ret = false;
            getErroresDigitalizacion().add("El parte ya está digitalizado.");
        } else if (getId().equals(digitalizacionParte.getIdParte())) {
            //Ahora necesitamos las lineas de alumnos
            //Ahora vamos recorriendo cada alumno y asignandole la asistencia
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Aplicando asistencia de parte:{0}", digitalizacionParte.getCodigoBarras());
            int sumFila = ((digitalizacionParte.getPagina() - 1) * digitalizacionParte.getConf().getNumFilas());
            int numFilasParte = getAsistencia().size();
            int numFilasDigitalizacion = digitalizacionParte.getDatos().size();
            //Verificamos que el total digitalizado sea el total de lineas del parte o el máximo que cabe en un parte
            if ((numFilasDigitalizacion + sumFila) != numFilasParte && numFilasDigitalizacion != digitalizacionParte.getConf().getNumFilas()) {
                MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_ERROR_DIGITALIZACION, "Parte: " + this + " se han digitalizado " + numFilasDigitalizacion + " filas teniendo el parte " + numFilasParte + " filas.\nPosiblemente el parte no se haya escaneado correctamente o se haya escaneado parcialmente, considere anular la digitalización de este.");
                msg.setParte(this);
                getAdvertenciasDigitalizacion().add(msg);
            }
            for (DatoDigitalizacion d : digitalizacionParte.getDatos()) {
                firePropertyChange("message", null, "Fila " + d.getFila());
                int fila = d.getFila() + sumFila + 1;
                //Ahora tenemos que buscar la asistencia del alumno con esa fila
                AsistenciaAlumno asis = null;
                for (AsistenciaAlumno a : getAsistencia()) {
                    if (a.getPosicion().equals(fila)) {
                        asis = a;
                        break;
                    }
                }
                if (asis != null) {
                    //Ahora aplicamos la asistencia al alumno
                    Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Asistencia de : {0}", asis.getAlumno().getNombreFormateado());
                    for (int i = 1; i < 7; i++) {
                        LineaParteAlumno l = asis.getLineaHora(i);

                        if (l != null && !l.isDividido()) {
                            int tipoAsistencia = getAsistenciaDigitalizacion(asis, i, d, l);
//                            //vemos si se debe controlar la asistencia de expulsados
//                            int accionExpulsados = ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados();
//
//                            if (accionExpulsados != ConfiguracionAsistencia.AAE_DEJAR_ASISTENCIA) {
//                                boolean expulsado = l.getAlumno().isExpulsado(l.getParte().getFecha());
//                                if (expulsado && tipoAsistencia != ParteFaltas.FALTA_EXPULSION) {
//                                    if (accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION) {
//                                        tipoAsistencia = ParteFaltas.FALTA_EXPULSION;
//                                    } else if(accionExpulsados == ConfiguracionAsistencia.AAE_ADVERTENCIA){
//                                        MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_NORMAL, "Parte: " + this + " " + i + "ª Hora.\nEn la asistencia de '" + l.getAlumno().getNombreFormateado() + "' (Nº " + fila + ") esta marcado un valor diferente a falta de asistencia estando el alumno expulsado.");
//                                        msg.setParte(this);
//                                        msg.setHorario(l.getHorario());
//                                        msg.setAlumno(l.getAlumno());
//                                        getAdvertenciasDigitalizacion().add(msg);
//                                    }
//                                }
//                            }
                            l.setAsistencia(tipoAsistencia);
                            l.guardarAsistencia();
                        } else if (isValorAsistencia(i, d)) {
                            if (l == null) {
                                MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_PARTE_INCORRECTO, "Parte: " + this + " " + i + "ª Hora.\nEn la asistencia de '" + asis.getAlumno().getNombreFormateado() + "' (Nº " + fila + ") hay una marca de asistencia cuando el alumno no tenía clase a esa hora.");
                                msg.setParte(this);
                                msg.setAlumno(asis.getAlumno());
                                getAdvertenciasDigitalizacion().add(msg);
                            } else if (l.isDividido()) {
                                MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_PARTE_INCORRECTO, "Parte: " + this + " " + i + "ª Hora.\nEn la asistencia de '" + l.getAlumno().getNombreFormateado() + "' (Nº " + fila + ") hay una marca de asistencia cuando el alumno estaba en otra asignatura.");
                                msg.setParte(this);
                                msg.setHorario(l.getHorario());
                                msg.setAlumno(l.getAlumno());
                                getAdvertenciasDigitalizacion().add(msg);
                            }
                        }
                    }
                } else {
                    Logger.getLogger(ParteFaltas.class.getName()).log(Level.WARNING, "No hay asistencia de alumno para fila {0} en parte: {1}:{2}", new Object[]{fila, getId(), digitalizacionParte.getCodigoBarras()});
                }
            }

            Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Aplicando firmas de parte:{0}", digitalizacionParte.getCodigoBarras());
            procesarFirmasParte(digitalizacionParte);
            //Sólo se marca como digitalizado cuando están todas las lineas
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Verificando si esta completamente digitalizado el parte:{0}", digitalizacionParte.getCodigoBarras());
            boolean digit = true;
            for (AsistenciaAlumno asis : getAsistencia()) {
                boolean hacerBreak = false;
                for (int i = 1; i < 7 && !hacerBreak; i++) {
                    LineaParteAlumno l = asis.getLineaHora(i);
                    if (l != null && !l.isDividido()) {
                        if (l.getAsistencia() == FALTA_INDETERMINADA) {
                            digit = false;
                            hacerBreak = true;
                        }
                    }
                }
                if (hacerBreak) {
                    break;
                }
            }
            setDigitalizado(digit);
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Guardando parte :{0}", digitalizacionParte.getCodigoBarras());
            guardar();
            if (isDigitalizado()) {
                //Revisamos que no haya posibles dudas
                revisarErroresIntuitivos();
            }
            ret = true;
        } else {
            ret = false;
            getErroresDigitalizacion().add("No coinciden los códigos de partes:\n\tEl código del parte digitalizado es " + digitalizacionParte.getCodigoBarras() + " el del parte es " + getId());
        }
        return ret;
    }

    private void revisarErroresIntuitivos() {
        firePropertyChange("message", null, "Verificando integridad lógica de faltas");
        //tenemos que revisar cada columna y cada fila. 
        ArrayList<AsistenciaAlumno> asis = getAsistencia();
        //Primero revisamos las filas
        for (AsistenciaAlumno a : asis) {
            ArrayList<Integer> saltos = a.getHorasSaltosAsistencia();
            if (saltos.size() > 0) {
                MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_DUDA_SALTOS_ASISTENCIA, "Parte: " + this + ". \nPosiblemente no se haya marcado la falta de asistencia del alumno " + a.getAlumno().getNombreFormateado() + " a ciertas horas " + saltos.toString() + ".");
                msg.setParte(this);
                msg.setAlumno(a.getAlumno());
                getAdvertenciasDigitalizacion().add(msg);
            }

        }
    }

    private boolean isValorAsistencia(int hora, DatoDigitalizacion dato) {
        //Vemos si hay falta
        int posHora = (hora * 2) - 2;
        //Primero vemos si hay retraso
        int retraso = dato.getColumnas().get(posHora + 1);
        int falta = dato.getColumnas().get(posHora);
        return falta > 0 || retraso > 0;
    }

    private int getAsistenciaDigitalizacion(AsistenciaAlumno asis, int hora, DatoDigitalizacion dato, LineaParteAlumno lineaParteAlumno) {
        Alumno alumno = asis.getAlumno();
        boolean expulsado = alumno.isExpulsado(getFecha());
        int fila = asis.getPosicion();
        int a = FALTA_INDETERMINADA;
        int aOrig = a;
        //Vemos si hay falta
        int posHora = (hora * 2) - 2;
        //Primero vemos si hay retraso
        int retraso = dato.getColumnas().get(posHora + 1);
        int falta = dato.getColumnas().get(posHora);
        int accionExpulsados = ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados();
        boolean mostrarAdvertenciaExpulsion = false;
        if (retraso > 0) {
            a = FALTA_RETRASO;
            //Aunque sea retraso vemos si hay falta
            if (falta > 0) {
                //Si el alumno está expulsado y hay que asignarle expulsión
                if (expulsado && (accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION || accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA)) {
                    a = FALTA_EXPULSION;
                    MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_IGNORADO, "Parte: " + this + " " + hora + "ª Hora.\nEn la asistencia de '" + alumno.getNombreFormateado() + "' (Nº " + fila + ") esta marcado falta y retraso. Al estar el alumno expulsado se asume falta por expulsión.");
                    msg.setParte(this);
                    msg.setHorario(lineaParteAlumno.getHorario());
                    msg.setAlumno(lineaParteAlumno.getAlumno());
                    getAdvertenciasDigitalizacion().add(msg);
                }
                if (!expulsado || (expulsado && (accionExpulsados == ConfiguracionAsistencia.AAE_ADVERTENCIA || accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA))) {
                    //Hay retraso y falta. Se asume retraso pero marcamos la advertencia
                    MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_FALTA_Y_RETRASO, "Parte: " + this + " " + hora + "ª Hora.\nEn la asistencia de '" + alumno.getNombreFormateado() + "' (Nº " + fila + ") esta marcado falta y retraso. Se asume " + ParteFaltas.getNombreTipoFalta(a) + ".");
                    msg.setParte(this);
                    msg.setHorario(lineaParteAlumno.getHorario());
                    msg.setAlumno(lineaParteAlumno.getAlumno());
                    getAdvertenciasDigitalizacion().add(msg);
                }
            } else if (expulsado) {
                if (accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION || accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA) {
                    aOrig = a;
                    a = FALTA_EXPULSION;
                }
                if (accionExpulsados == ConfiguracionAsistencia.AAE_ADVERTENCIA || accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA) {
                    mostrarAdvertenciaExpulsion = true;
                }
            }
        } else {
            //Si no hay retraso miramos si hay falta. Si hubiese falta y retraso se tiene en cuenta la falta.
            if (falta > 0) {
                if (expulsado) {
                    a = FALTA_EXPULSION;
                } else {
                    a = FALTA_INJUSTIFICADA;
                }
            } else {
                a = FALTA_ASISTENCIA;
                if (expulsado) {
                    if (accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION || accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA) {
                        aOrig = a;
                        a = FALTA_EXPULSION;
                    }
                    if (accionExpulsados == ConfiguracionAsistencia.AAE_ADVERTENCIA || accionExpulsados == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA) {
                        mostrarAdvertenciaExpulsion = true;
                    }
                }
            }
        }
        if (mostrarAdvertenciaExpulsion) {
            String extra = "";
            if (a != aOrig) {
                extra = " Se asume " + ParteFaltas.getNombreTipoFalta(a) + ".";
            }
            MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_NORMAL, "Parte: " + this + " " + hora + "ª Hora.\nEn la asistencia de '" + alumno.getNombreFormateado() + "' (Nº " + fila + ") esta marcado " + ParteFaltas.getNombreTipoFalta(aOrig) + " estando el alumno expulsado." + extra);
            msg.setParte(this);
            msg.setHorario(lineaParteAlumno.getHorario());
            msg.setAlumno(lineaParteAlumno.getAlumno());
            getAdvertenciasDigitalizacion().add(msg);
        }
        //En ambos casos revisamos los códigos
        if (falta == DigitalizacionParte.ANULADO_DUDOSO || retraso == DigitalizacionParte.ANULADO_DUDOSO) {
            String strMarca = "";
            if (falta == DigitalizacionParte.ANULADO_DUDOSO) {
                strMarca = "falta";
            }
            if (retraso == DigitalizacionParte.ANULADO_DUDOSO) {
                if (strMarca.equals("")) {
                    strMarca = "retraso";
                } else {
                    strMarca = "y retraso";
                }
            }
            MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_DUDA_ANULACION, "Parte: " + this + " " + hora + "ª Hora.\nLa marca de " + strMarca + " de '" + alumno.getNombreFormateado() + "' (Nº " + fila + ") parece anulada pero necesita confirmación.");
            msg.setParte(this);
            msg.setHorario(lineaParteAlumno.getHorario());
            msg.setAlumno(lineaParteAlumno.getAlumno());
            getAdvertenciasDigitalizacion().add(msg);
        } else if (falta == DigitalizacionParte.MANCHADO || retraso == DigitalizacionParte.MANCHADO) {
            String strMarca = "";
            if (falta == DigitalizacionParte.MANCHADO) {
                strMarca = "falta";
            }
            if (retraso == DigitalizacionParte.MANCHADO) {
                if (strMarca.equals("")) {
                    strMarca = "retraso";
                } else {
                    strMarca = "y retraso";
                }
            }
            MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_DUDA_MARCA, "Parte: " + this + " " + hora + "ª Hora.\nLa marca de " + strMarca + " de '" + alumno.getNombreFormateado() + "' (Nº " + fila + ") parece marcada pero necesita confirmación.");
            msg.setParte(this);
            msg.setHorario(lineaParteAlumno.getHorario());
            msg.setAlumno(lineaParteAlumno.getAlumno());
            getAdvertenciasDigitalizacion().add(msg);
        }
        return a;
    }

    public ArrayList<AsistenciaAlumno> getAsistencia() {
        if (asistencia == null) {
            firePropertyChange("message", null, "Cargando asistencia de alumnos...");
            asistencia = new ArrayList<AsistenciaAlumno>();
            for (Alumno a : getAlumnos()) {
                AsistenciaAlumno asis = new AsistenciaAlumno(this, a);
                asistencia.add(asis);
            }
        }
        return asistencia;
    }

    public ArrayList<AsistenciaAlumno> getAsistenciaInjustificada() {
        if (asistencia == null) {
            asistencia = new ArrayList<AsistenciaAlumno>();
            for (Alumno a : getAlumnos()) {
                AsistenciaAlumno asis = new AsistenciaAlumno(this, a);
                asistencia.add(asis);
            }
        }
        return asistencia;
    }

    private void procesarFirmasParte(DigitalizacionParte digitalizacionParte) {
        //Ahora procesamos las firmas
        ArrayList<Integer> horas = getHorasDistintas(getHorarios());
        ArrayList<Integer> firma = digitalizacionParte.getPie().getColumnas();
        PreparedStatement st = null;
        try {
            //Marcamos los horarios como firmados
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes_horarios SET firmado=1 WHERE parte_id=? AND horario_id=?");
            st.setInt(1, getId());
            for (Integer hora : horas) {
                //Vemos si esta firmado
                if (hora <= firma.size() && firma.get(hora - 1).intValue() > 0) {
                    for (Horario h : getHorarios()) {
                        if (h.getHora().equals(hora)) {
                            st.setInt(2, h.getId());
                            st.addBatch();
                        }
                    }
                } else {
                    MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, MensajeDigitalizacion.TIPOA_SIN_FIRMA, "Parte: " + this + " " + hora + "ª Hora.\nNo está firmado.");
                    msg.setParte(this);
                    getAdvertenciasDigitalizacion().add(msg);
                }
            }
            st.executeBatch();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getNumeroDeCampos() {
        return 5;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = getId();
                break;
            case 1:
                val = getFecha();
                break;
            case 2:
                val = getDescripcionObjeto();
                break;
            case 3:
                val = getCurso();
                break;
            case 4:
                val = getProfesorParte();
                break;
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = "";
        switch (index) {
            case 0:
                val = "Código";
                break;
            case 1:
                val = "Fecha";
                break;
            case 2:
                val = "Parte de asistencia";
                break;
            case 3:
                val = "Curso";
                break;
            case 4:
                val = "Profesor";
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
                val = GregorianCalendar.class;
                break;
            case 2:
                val = String.class;
                break;
            case 3:
                val = String.class;
                break;
            case 4:
                val = String.class;
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

    @Override
    public String getTabla() {
        return "partes";
    }
}
