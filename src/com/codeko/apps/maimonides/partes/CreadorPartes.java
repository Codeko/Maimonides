package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * @author Codeko
 * //TODO Limpiar y rehacer esta clase. Ver si la fusión es mas o menos efectiva que la compresión (parece que no).
 * //Habría que procesar los paquetes de horarios juntos para poder decidir, por ejemplo, si un horario dicu se manda al parte dicu o se mezcla con los normales.
 * //Si es un sólo horario dicu podemos meterlo tranquilamente en el parte dicu, si es un horario dicu mezclado con no dicu no se puede. La otra opción para estos casos es que no cree horarios dicu para asignaturas no dicu <--!
 */
public class CreadorPartes extends MaimonidesBean {

    AnoEscolar anoEscolar = null;
    GregorianCalendar fecha = null;
    ArrayList<Unidad> unidades = null;
    String curso = null;
    Integer dia = null;
    ArrayList<ParteFaltas> partesFaltas = new ArrayList<ParteFaltas>();
    ArrayList<ParteFaltas> partesFaltasCurso = new ArrayList<ParteFaltas>();
    boolean comprimir = MaimonidesApp.getApplication().getConfiguracion().isComprimirPartes();
    boolean crear = true;

    public boolean isCrear() {
        return crear;
    }

    public void setCrear(boolean crear) {
        this.crear = crear;
    }

    public boolean isComprimir() {
        return comprimir;
    }

    public void setComprimir(boolean comprimir) {
        this.comprimir = comprimir;
    }

    public ArrayList<ParteFaltas> getPartesFaltasCurso() {
        return partesFaltasCurso;
    }

    public ArrayList<ParteFaltas> getPartesFaltas() {
        return partesFaltas;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getDia() {
        if (dia == null) {
            dia = MaimonidesUtil.getDiaFecha(getFecha());
        }
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public void setUnidades(ArrayList<Unidad> unidades) {
        this.unidades = unidades;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public final void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public CreadorPartes(AnoEscolar anoEscolar, GregorianCalendar fecha) {
        setAnoEscolar(anoEscolar);
        setFecha(fecha);
    }

    public CreadorPartes(AnoEscolar anoEscolar) {
        setAnoEscolar(anoEscolar);
        setFecha(new GregorianCalendar());
    }

    public void recuperarPartes() {
        //Vemos si hay partes para el día de hoy
        //Si no los hay los generamos
        ParteDataSourceProvider pdsp = new ParteDataSourceProvider(getAnoEscolar(), getFecha(), this);
        pdsp.cargarPartes();
        getPartesFaltas().addAll(pdsp.getPartes());
        if (getPartesFaltas().isEmpty() && isCrear()) {
            generarPartes();
        }
    }

    private void borrarPartesVacios(Collection<ParteFaltas> partes) {
        //Ahora cogemos todos los partes y borramos los que no tengan alumnos
        ArrayList<ParteFaltas> borrar = new ArrayList<ParteFaltas>();
        for (ParteFaltas p : partes) {
            if (p.getAlumnos().isEmpty()) {
                Logger.getLogger(CreadorPartes.class.getName()).log(Level.INFO, "Borrando parte {0}: {1} por estar vacio.", new Object[]{p.getId(), p.getDescripcionObjeto()});
                p.borrar();
                borrar.add(p);
            }
        }
        partes.removeAll(borrar);
    }

    private void crearPartesDeApoyo(Collection<ParteFaltas> partes) {
        String sql = "SELECT distinct h.id AS idHorario "
                + " FROM horarios_ AS h "
                + " JOIN tramos AS t ON t.id=h.tramo_id "
                + " JOIN partes_horarios AS ph ON ph.horario_id=h.id "
                + " LEFT JOIN unidades AS u ON u.id=h.unidad_id "
                + " LEFT JOIN materias AS m ON m.id=h.materia_id "
                + " JOIN materias_alumnos As ma ON ma.materia_id=m.id "
                + " JOIN alumnos AS a ON a.unidad_id=u.id AND ma.alumno_id=a.id AND (h.dicu=" + ParteFaltas.DICU_AMBOS + " OR a.dicu=h.dicu) "
                + " JOIN partes AS p ON p.id=ph.parte_id "
                + " JOIN apoyos_alumnos AS aa ON aa.alumno_id=a.id AND aa.horario_id=h.id "
                + " JOIN alumnos_horarios AS ah ON ah.alumno_id=a.id AND ah.horario_id=h.id "
                + " WHERE a.borrado=0 AND h.ano=? AND u.curso=? AND p.fecha=? AND h.activo=1 AND ah.activo=1 "
                + " ORDER BY h.hora,h.profesor_id";
        //TODO DICU Ver tema dicu: AND h.dicu=a.dicu LISTO ELIMINAR SI NO DA PROBLEMAS
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getAnoEscolar().getId());
            st.setString(2, getCurso());
            st.setDate(3, new java.sql.Date(getFecha().getTimeInMillis()));
            ResultSet res = st.executeQuery();
            ParteFaltas parteApoyo = new ParteFaltas();
            parteApoyo.setAnoEscolar(getAnoEscolar());
            parteApoyo.setCurso(getCurso());
            parteApoyo.setFecha(getFecha());
            parteApoyo.setPrimario(false);
            parteApoyo.setApoyo(true);
            while (res.next()) {
                int horarioId = res.getInt("idHorario");
                Horario h = Horario.getHorario(horarioId);
                Unidad u = getUnidad(h.getUnidad());
                if (!parteApoyo.getUnidades().contains(u)) {
                    parteApoyo.getUnidades().add(u);
                }
                parteApoyo.getHorarios().add(h);
            }
            Obj.cerrar(st, res);
            if (parteApoyo.getUnidades().size() > 0) {
                parteApoyo.guardar();
                partes.add(parteApoyo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "Error generando partes de apoyo para el curso: " + getCurso(), ex);
        }

    }

    private void fusionarPartes(Collection<ParteFaltas> partes) {
        //Ahora probamos a fusionar los partes entre si
        FusionadorPartes fus = new FusionadorPartes();
        ArrayList<ParteFaltas> borrados = new ArrayList<ParteFaltas>();
        for (ParteFaltas p1 : partes) {
            for (ParteFaltas p2 : partes) {
                //Puede haberse eliminado el primer parte o el segundo así que comprobamos los dos
                if (p2.getId() != null && p1.getId() != null && !p2.equals(p1)) {
                    int ret = fus.fusionarParte(p1, p2);
                    //Si el segundo se ha fusionado lo quitamos de la lista
                    switch (ret) {
                        case FusionadorPartes.FUSION_ELIMINAR_PRIMERO:
                            p1.borrar();
                            borrados.add(p1);
                            p2.resetearCabeceras();
                            p2.resetearCabecerasCompletas();
                            p2.resetearListaAlumnos();
                            break;
                        case FusionadorPartes.FUSION_ELIMINAR_SEGUNDO:
                            p2.borrar();
                            borrados.add(p2);
                            p1.resetearCabeceras();
                            p1.resetearCabecerasCompletas();
                            p1.resetearListaAlumnos();
                            break;
                    }
                }
            }
        }

        for (ParteFaltas p : borrados) {
            partes.remove(p);
        }
    }

    private void comprimirPartes(Collection<ParteFaltas> partes) {
        //Ahora probamos a fusionar los partes entre si
        FusionadorPartes fus = new FusionadorPartes();
        ArrayList<ParteFaltas> borrados = new ArrayList<ParteFaltas>();
        for (ParteFaltas p1 : partes) {
            for (ParteFaltas p2 : partes) {
                //Puede haberse eliminado el primer parte o el segundo así que comprobamos los dos
                if (p2.getId() != null && p1.getId() != null && !p2.equals(p1)) {
                    int ret = fus.comprimirPartes(p1, p2);
                    //Si el segundo se ha fusionado lo quitamos de la lista
                    switch (ret) {
                        case FusionadorPartes.FUSION_ELIMINAR_PRIMERO:
                            p1.borrar();
                            borrados.add(p1);
                            p2.resetearCabeceras();
                            p2.resetearCabecerasCompletas();
                            p2.resetearListaAlumnos();
                            break;
                        case FusionadorPartes.FUSION_ELIMINAR_SEGUNDO:
                            p2.borrar();
                            borrados.add(p2);
                            p1.resetearCabeceras();
                            p1.resetearCabecerasCompletas();
                            p1.resetearListaAlumnos();
                            break;
                    }
                }
            }
        }

        for (ParteFaltas p : borrados) {
            partes.remove(p);
        }
    }

    private void generarPartes() {
        getPartesFaltas().clear();
        try {
            //TODO Implementar rollback
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT distinct curso FROM cursos WHERE ano=? AND (ISNULL(fini) OR fini<=DATE(NOW())) AND (ISNULL(ffin) OR ffin>=DATE(NOW()))  ORDER BY posicion ASC");
            st.setInt(1, getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                try {
                    generarPartes(res.getString(1));
                } catch (Exception e) {
                    Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "Error generando parte.", e);
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "Error recuperando cursos para generar los partes.", ex);
        }

    }

    public static GregorianCalendar getUltimaImpresionPartes() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(Num.getLong(MaimonidesApp.getApplication().getConfiguracion().get("partes_ultima_impresion", "0"), 0));
        return cal;
    }

    public static void setUltimaImpresionPartes(GregorianCalendar fecha) {
        if (Fechas.getDiferenciaTiempoEn(fecha, getUltimaImpresionPartes(), GregorianCalendar.DAY_OF_YEAR) > 0) {
            MaimonidesApp.getApplication().getConfiguracion().set("partes_ultima_impresion", fecha.getTimeInMillis() + "");
        }
    }

    public static boolean isParteImpresos(GregorianCalendar fecha) {
        long dif = Fechas.getDiferenciaTiempoEn(fecha, getUltimaImpresionPartes(), GregorianCalendar.DAY_OF_YEAR);
        return dif <= 0;
    }

    private void generarPartes(String curso) {
        //NORMA GENERAL: No se puede tener en consideracío si un horario es dicu o no para su asignación. ¨Pues existen horarios dicus que deben ir con horarios no dicu.
        //Un parte si puede ser dicu si sólo tiene horarios dicus pero un horario dicu de primeras puede ir en cualquier sitio o si no hay que crear un parte específico
        //para el incluso en las asignaturas que comparten con el resto de la clase
        setCurso(curso);
        setUnidades(null);
        firePropertyChange("message", null, "Generando partes de : " + curso);
        getPartesFaltasCurso().clear();
        //Ahora sacamos todos los datos
        String sql = "SELECT h.* FROM horarios_ AS h JOIN tramos AS t ON t.id=h.tramo_id LEFT JOIN unidades AS u ON u.id=h.unidad_id "
                + " WHERE h.activo=1 AND t.ano=? AND h.dia=? AND u.curso=? ORDER BY h.hora,h.profesor_id";
        try {
            //Creamos un parte base para cada unidad
            for (Unidad u : getUnidades()) {
                ParteFaltas p = new ParteFaltas();
                p.setCurso(getCurso());
                p.setFecha(getFecha());
                //Le asignamos la unidad
                p.getUnidades().add(u);
                p.setPrimario(true);
                getPartesFaltasCurso().add(p);
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getAnoEscolar().getId());
            st.setInt(2, getDia());
            st.setString(3, curso);
            ResultSet res = st.executeQuery();
            ArrayList<Horario> ultimosHorarios = new ArrayList<Horario>();
            int ultimaHora = -1;
            int ultimoProfesor = -1;
            while (res.next()) {
                //Vamos recorriendo las lineas por horas cada hora es un nuevo parte. 
                //Si la siguiente hora tiene el mismo profesor que el anterior es un parte compartido
                //Todas las horas compartidas tienen partes propios
                Horario h = new Horario();
                h.cargarDesdeResultSet(res);
                //Si no tiene alumnos lo ignoramos
                if (h.getNumeroDeAlumnos() > 0) {
                    if (ultimaHora == -1) {
                        ultimaHora = h.getHora();
                        ultimoProfesor = h.getProfesor();
                    }
                    if (h.getHora() != ultimaHora || h.getProfesor() != ultimoProfesor) {
                        //Entonces hemos cambiado y procesamos los horarios actuales
                        procesarBloqueHorarios(ultimosHorarios, getPartesFaltasCurso());
                        ultimosHorarios.clear();
                    }
                    ultimosHorarios.add(h);
                    ultimaHora = h.getHora();
                    ultimoProfesor = h.getProfesor();
                }
            }
            Obj.cerrar(st, res);
            //Procesamos los horarios que queden
            procesarBloqueHorarios(ultimosHorarios, getPartesFaltasCurso());
            //Ahora guardamos los partes
            ArrayList<IObjetoBD> ioPartes = new ArrayList<IObjetoBD>();
            ioPartes.addAll(getPartesFaltasCurso());
            guardarObjetosBD(ioPartes);
            ioPartes = null;
            //Creamos los partes de apoyos
            crearPartesDeApoyo(getPartesFaltasCurso());
            //Borramos los vacios. 
            borrarPartesVacios(getPartesFaltasCurso());
            //Fusionamos los partes iguales
            //TODO Parece que con la compresión es suficiente
            fusionarPartes(getPartesFaltasCurso());
            if (isComprimir()) {
                comprimirPartes(getPartesFaltasCurso());
            }
            //Y volvemos a borrar los vacios. 
            borrarPartesVacios(getPartesFaltasCurso());
            getPartesFaltas().addAll(getPartesFaltasCurso());
        } catch (SQLException ex) {
            Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "Error generando partes de curso:" + getCurso(), ex);
        }
    }

    private int getNumeroHorasParteNoCreado(ParteFaltas p) {
        int count = 0;
        for (Horario h : p.getHorarios()) {
            count += h.getNumeroDeAlumnos();
        }
        return count;
    }

    private ArrayList<ParteFaltas> getPartesUnidad(Unidad u, ArrayList<ParteFaltas> partes) {
        ArrayList<ParteFaltas> partesUnidad = new ArrayList<ParteFaltas>();
        for (ParteFaltas p : partes) {
            if (p.getUnidades().size() == 1 && p.getUnidades().get(0).equals(u)) {
                partesUnidad.add(p);
            }
        }
        return partesUnidad;
    }

    private ParteFaltas getPartePrincipal(Integer idUnidad, ArrayList<ParteFaltas> partes) {
        ParteFaltas partePrincipal = null;
        //Buscamos el parte para esa unidad por si ya está creado
        for (ParteFaltas p : partes) {
            if (p.isPrimario() && p.getUnidades().get(0).getId().equals(idUnidad)) {
                partePrincipal = p;
                break;
            }
        }
        return partePrincipal;
    }

    private ParteFaltas getParteDestino(Horario horario, ArrayList<ParteFaltas> partes) {
        ParteFaltas parteDestino = null;
        //Buscamos el parte para esa unidad por si ya está creado
        for (ParteFaltas p : partes) {
            //Ahora tenemos que verificar que se pueda añadir a este parte
            if (esCompatible(p, horario)) {
                parteDestino = p;
                break;
            }
        }
        return parteDestino;
    }

    private boolean esCompatible(ParteFaltas p, Horario horario) {
        boolean compatible = false;
        if (p.isDicu() == horario.isDicu()) {
            if (horario.isDicu() && p.isPrimario()) {
                return compatible;
            }
            for (Horario h : p.getHorarios()) {
                if (h.getHora().equals(horario.getHora()) && sonIguales(h, horario)) {
                    compatible = true;
                    break;
                }
            }
        }
        return compatible;
    }

    private void procesarHorario(Horario horario, ArrayList<ParteFaltas> partes) {
        //Buscamos el parte por si ya está creado
        ParteFaltas partePrincipal = getPartePrincipal(horario.getUnidad(), partes);
        ParteFaltas parteDestino = getParteDestino(horario, partes);
        //Si no hemos encontrado parte lo creamos
//        if (partePrincipal == null) {
//            //TODO Este caso no debería darse nunca
//            Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "No había parte primario generado para horario:" + horario + " de curso: " + getCurso() + " año: " + getAnoEscolar());
//            partePrincipal = new ParteFaltas();
//            partePrincipal.setPrimario(true);
//            partePrincipal.setCurso(getCurso());
//            partePrincipal.setFecha(getFecha());
//            //Le asignamos la unidad
//            partePrincipal.getUnidades().add(getUnidad(horario.getUnidad()));
//            partes.add(partePrincipal);
//        }

        //Si el parte de destino existe le añadimos los horarios y listo
        if (parteDestino != null) {
            parteDestino.getHorarios().add(horario);
            //Si es distinto al principal añadimos los horarios como divididos
            if (!parteDestino.equals(partePrincipal)) {
                partePrincipal.getHorariosDivididos().add(horario);
            }

        } else {
            boolean creadoNuevo = false;
            //Si no se ha encontrado un parte de destino hay que crear uno nuevo
            //Si el horario es dicu no se va a intentar cambiar por ninguno en el principal
            //así que creamos directamente el nuevo
            if (horario.isDicu()) {
                creadoNuevo = true;
                ParteFaltas parteNuevo = new ParteFaltas();
                parteNuevo.setCurso(getCurso());
                parteNuevo.setFecha(getFecha());
                parteNuevo.getUnidades().add(getUnidad(horario.getUnidad()));
                parteNuevo.getHorarios().add(horario);
                partes.add(parteNuevo);
            } else {
                //Si no vemos si este horario se adapta mejor al parte principal
                for (Horario h : partePrincipal.getHorarios()) {
                    //En el momento que uno es igual el resto de su hora deben serlo
                    if (h.getHora().equals(horario.getHora()) && !sonIguales(h, horario)) {
                        creadoNuevo = true;
                        //Logger.getLogger(CreadorPartes.class.getName()).info("Hay dos horarios distintos para la misma hora en un parte primario:\n1º: " + h + "\n2º:" + horario);
                        //Y creamos uno nuevo
                        //vemos cual tiene mas alumnos para poner ese en el principal
                        int alActual = h.getNumeroDeAlumnos();
                        int alNuevo = horario.getNumeroDeAlumnos();
                        ArrayList<Horario> anadir = new ArrayList<Horario>();
                        if (alNuevo > alActual) {
                            //Entonces tenermos que mover todos los horarios del principal al nuevo
                            for (Horario hp : partePrincipal.getHorarios()) {
                                if (hp.getHora().equals(horario.getHora())) {
                                    anadir.add(hp);
                                }
                            }
                            //Lo quitamos de la lista de horarios actuales de esa hora en cuestion
                            partePrincipal.getHorarios().removeAll(anadir);
                            //Lo añadimos como secundario
                            partePrincipal.getHorariosDivididos().addAll(anadir);
                            //Y el nuevo lo añadimos
                            partePrincipal.getHorarios().add(horario);
                        } else {
                            //Lo añadimos como secundario al primario
                            partePrincipal.getHorariosDivididos().add(horario);
                            anadir.add(horario);
                        }

                        ParteFaltas parteNuevo = new ParteFaltas();
                        parteNuevo.setCurso(getCurso());
                        parteNuevo.setFecha(getFecha());
                        //Le asignamos la unidad
                        for (Horario hh : anadir) {
                            if (!parteNuevo.getUnidades().contains(getUnidad(hh.getUnidad()))) {
                                parteNuevo.getUnidades().add(getUnidad(hh.getUnidad()));
                            }
                        }
                        parteNuevo.getHorarios().addAll(anadir);
                        partes.add(parteNuevo);
                        break;
                    }
                }
            }
            if (!creadoNuevo) {
                partePrincipal.getHorarios().add(horario);
            }
        }
    }

    private boolean sonIguales(Horario h1, Horario h2) {
        boolean iguales = true;
        if (h1 != null && h2 != null) {
            iguales = (h1.getHora().equals(h2.getHora()));
            if (iguales) {
                iguales = h1.getActividad().equals(h2.getActividad());
            }
//            if (iguales) {
//                iguales = h1.isDicu() == h2.isDicu();
//            }
            if (iguales) {
                if (h1.getMateria() != null && h2.getMateria() != null && h1.getMateria() > 0 && h2.getMateria() > 0) {
                    iguales = h1.getObjetoMateria().esEquivalente(h2.getObjetoMateria());
                } else {
                    //Si alguna materia es nula las dos deben serlo para que sean iguales
                    iguales = Num.getInt(h1.getMateria()) == 0 && Num.getInt(h2.getMateria()) == 0;
                }
            }
            if (iguales) {
                iguales = h1.getProfesor().equals(h2.getProfesor());
            }
        } else {
            iguales = false;
            Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "Algunos de los dos partes es nulo:\n\t{0}\n\t{1}", new Object[]{h1, h2});
        }
        return iguales;
    }

    private void procesarHorarioDividido(Horario horario, ArrayList<ParteFaltas> partes) {
        //Buscamos el parte por si ya está creado
        ParteFaltas parte = null;
        //Buscamos el parte para esa unidad
        for (ParteFaltas p : partes) {
            if (p.isPrimario() && p.getUnidades().get(0).getId().equals(horario.getUnidad())) {
                parte = p;
            }
        }
        //Le añadimos al parte el horario
        if (parte != null) {
            parte.getHorariosDivididos().add(horario);
        } else {
            //TODO Este caso no debería darse nunca 
            Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, "No hab\u00eda parte primario generado para horario:{0} de curso: {1} a\u00f1o: {2}", new Object[]{horario, getCurso(), getAnoEscolar()});
        }
    }

    private void procesarHorariosDivididos(ArrayList<Horario> horarios, ArrayList<ParteFaltas> partes) {
        //Tenemos que buscar el parte de cada horario y añadirselo
        for (Horario h : horarios) {
            procesarHorarioDividido(h, partes);
        }
    }

    private ArrayList<Horario> ordenarHorariosPorAlumnos(ArrayList<Horario> horarios) {
        ArrayList<Horario> ordenados = new ArrayList<Horario>(horarios.size());
        for (Horario h : horarios) {
            int num = h.getNumeroDeAlumnos();
            boolean insertado = false;
            for (int i = 0; i < ordenados.size() && !insertado; i++) {
                Horario ho = ordenados.get(i);
                if (num > ho.getNumeroDeAlumnos()) {
                    ordenados.add(i, h);
                    insertado = true;
                }
            }
            //Si no lo hemos insertado lo añadimos al final
            if (!insertado) {
                ordenados.add(h);
            }
        }
        return ordenados;
    }

    private boolean sonEquivalentes(Object obj1, Object obj2) {
        if (obj1 instanceof Materia && obj2 instanceof Materia) {
            return ((Materia) obj1).esEquivalente((Materia) obj2);
        } else if (obj1 instanceof Actividad && obj2 instanceof Actividad) {
            return ((Actividad) obj1).equals(obj2);
        } else {
            return false;
        }
    }

    private void procesarBloqueHorarios(ArrayList<Horario> horarios, ArrayList<ParteFaltas> partes) {
        //Si el horario es simple es un horario normal y se busca su parte y se le asigna
        if (horarios.size() > 0) {
            if (horarios.size() == 1) {
                Horario h = horarios.get(0);
                procesarHorario(h, partes);
            } else {
                //Si hay más de un elemento pueden pasar 2 cosas:
                // Que sea una división de una misma clase en cuyo caso un horario va al parte principal y otro se separa
                // Que esten mezcladas distintas clases en cuyo caso es un parte para esa mezcla
                // Realmente hay una tercera opción pero que no hay datos para resolverla y es que haya distintas clases pero se quieran crear distintos partes
                // Si Hay por ejemplo tres unidades se puede querer crear 2 partes en vez de 3 uno con una unidad y parte de la otra (A+B) y otro con la otra unidad y el resto de la primera (C+B)
                // Y una cuarta opcion en la que parte de un curso se incorpora a otro que está en su asigantura normal

                //Vemos si son diferentes
                boolean sonDiferentes = false;//Si tienen unidades diferentes
                boolean noSonIguales = false;// Si tienen unidades iguales y ademas igual materia y profesor
                int ultimaUnidad = -1;
                //int ultimaMateria = -1;
                Object ultimaMat = null;
                int ultimoProfesor = -1;
                //De camino guardamos las unidades por si las necesitamos más adelante si son diferentes
                LinkedHashSet<Unidad> uds = new LinkedHashSet<Unidad>();
                for (Horario h : horarios) {
                    if (ultimaUnidad != -1 && ultimaUnidad != h.getUnidad()) {
                        sonDiferentes = true;
                    }
                    if (!sonDiferentes && !noSonIguales) {
                        if (ultimoProfesor != -1 && ultimoProfesor != h.getProfesor()) {
                            noSonIguales = true;
                        }
                        if (!noSonIguales && ultimaMat != null && !sonEquivalentes(ultimaMat, h.getObjetoMateriaOActividad())) {
                            noSonIguales = true;
                        }
                    }
                    ultimaUnidad = h.getUnidad();
                    ultimaMat = h.getObjetoMateriaOActividad();
                    ultimoProfesor = h.getProfesor();
                    uds.add(getUnidad(h.getUnidad()));
                }
                //Si lo son creamos el parte individual
                if (sonDiferentes) {
                    //Tenemos que crear un parte nuevo y añadirle las unidades y los horarios
                    ParteFaltas parte = new ParteFaltas();
                    parte.setCurso(getCurso());
                    parte.setFecha(getFecha());
                    //Le asignamos las unidades
                    parte.getUnidades().addAll(uds);
                    //Y los horarios
                    parte.getHorarios().addAll(horarios);
                    //Y lo añadimos a los partes
                    partes.add(parte);
                    // Logger.getLogger(CreadorPartes.class.getName()).info("Añadido parte proceso normal. Son Diferentes (" + horarios + "):" + parte);
                    procesarHorariosDivididos(horarios, partes);
                } else {
                    if (noSonIguales) {
                        //Si son iguales el que tenga más alumnos se queda en el parte principal y el resto cada uno crea su propio parte
                        ArrayList<Horario> ordenados = ordenarHorariosPorAlumnos(horarios);
                        //Una vez ordenados el primero se añade normalmente
                        Horario h = ordenados.get(0);
                        procesarHorario(h, partes);
                        // Y El resto crean su propio horario
                        for (int x = 1; x < ordenados.size(); x++) {
                            Horario horarioActual = ordenados.get(x);
                            ParteFaltas parte = new ParteFaltas();
                            parte.setCurso(getCurso());
                            parte.setFecha(getFecha());
                            //Le asignamos las unidades
                            parte.getUnidades().add(getUnidad(horarioActual.getUnidad()));
                            //Y los horarios
                            parte.getHorarios().add(horarioActual);
                            //Lo añadimos a los secundarios del parte principal de la unidad
                            procesarHorarioDividido(horarioActual, partes);
                            //Y lo añadimos a los partes
                            partes.add(parte);
                            // Logger.getLogger(CreadorPartes.class.getName()).info("Añadido parte proceso norma no son diferentes (" + horarioActual + "):" + parte);
                        }
                    } else {
                        //Entonces tenemos que añadir ambos horarios al parte
                        for (Horario h : horarios) {
                            procesarHorario(h, partes);
                        }
                    }
                }
            }
        }
    }
    //TODO Tanto esta como la otra funcion se irian implementado la cache en IObjetoBD

    public Unidad getUnidad(int id) {
        for (Unidad u : getUnidades()) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    private ArrayList<Unidad> getUnidades() {
        if (unidades == null) {
            unidades = new ArrayList<Unidad>();
            String sql = "SELECT * FROM unidades WHERE ano=? AND curso=? ORDER BY posicion";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getAnoEscolar().getId());
                st.setString(2, getCurso());
                res = st.executeQuery();
                while (res.next()) {
                    Unidad u = new Unidad();
                    u.cargarDesdeResultSet(res);
                    unidades.add(u);
                }
            } catch (SQLException ex) {
                Logger.getLogger(CreadorPartes.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return unidades;
    }
}
