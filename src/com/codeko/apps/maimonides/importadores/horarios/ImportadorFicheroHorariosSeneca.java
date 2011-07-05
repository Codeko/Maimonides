package com.codeko.apps.maimonides.importadores.horarios;

import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.*;

import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.apps.maimonides.importadores.DatoXML;
import com.codeko.util.Num;
import com.codeko.util.estructuras.Par;
import com.mysql.jdbc.PreparedStatement;
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImportadorFicheroHorariosSeneca extends ImportadorHorarios {

    /**
     * Clase encargada de importar los horarios desde fichero XML. Este fichero
     * no lo genera séneca sino que es el formato usado por Séneca para importar
     * los horarios. Normalmente este fichero lo generan programas como Horw (o el
     * mismo Maimonides)
     */
    ArrayList<TramoHorario> tramos = null;
    private File archivoHorariosSeneca = null;

    public ImportadorFicheroHorariosSeneca(AnoEscolar anoEscolar, File archivoHorariosSeneca) {
        setAnoEscolar(anoEscolar);
        setArchivoHorariosSeneca(archivoHorariosSeneca);
    }

    public File getArchivoHorariosSeneca() {
        return archivoHorariosSeneca;
    }

    public final void setArchivoHorariosSeneca(File archivoHorariosSeneca) {
        firePropertyChange("archivoHorariosSeneca", this.archivoHorariosSeneca, archivoHorariosSeneca);
        this.archivoHorariosSeneca = archivoHorariosSeneca;
    }

    public ArrayList<TramoHorario> getTramos() {
        if (tramos == null) {
            tramos = new ArrayList<TramoHorario>();
            try {
                PreparedStatement stSelectTramos = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM `tramos` WHERE ano=? AND fin-inicio>30 ORDER BY inicio ASC");
                stSelectTramos.setInt(1, getAnoEscolar().getId());
                ResultSet res = stSelectTramos.executeQuery();
                while (res.next()) {
                    TramoHorario t = new TramoHorario();
                    t.cargarDesdeResultSet(res);
                    tramos.add(t);
                }
            } catch (Exception e) {
                Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, "Error cargando tramos horarios", e);
            }
        }
        return tramos;
    }

    public ArrayList<DatoXML> getDatos(Element el) {
        NodeList datos = el.getElementsByTagName("dato");
        ArrayList<DatoXML> d = new ArrayList<DatoXML>();
        for (int i = 0; i < datos.getLength(); i++) {
            Node n = datos.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elemento = (Element) n;
                d.add(getDato(elemento));
            }
        }
        return d;
    }

    public DatoXML getDato(Element elemento) {
        String nombre = elemento.getAttribute("nombre_dato");
        String valor = null;
        if (elemento.getChildNodes().getLength() > 0) {
            valor = elemento.getChildNodes().item(0).getNodeValue();
        }
        return new DatoXML(nombre, valor);
    }

    @Override
    public boolean importarHorarios() {
        boolean ret = false;
        ArrayList<Par<Profesor,ArrayList<Horario>>> horarios=new ArrayList<Par<Profesor,ArrayList<Horario>>>();
        try {
            firePropertyChange("message", null, "Procensando XML...");
            Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.INFO, "Procesando archivo de Horarios de S\u00e9neca:{0}", getArchivoHorariosSeneca());
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(getArchivoHorariosSeneca());
            //Normalizamos el texto
            doc.getDocumentElement().normalize();
            Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.INFO, "Elemento raiz es {0}", doc.getDocumentElement().getNodeName());
            //Vemos que el nodo raiz sea el correcto
            Node raiz = doc.getDocumentElement();
            if (!raiz.getNodeName().toLowerCase().equals("servicio") || !raiz.getAttributes().getNamedItem("modulo").getNodeValue().toLowerCase().equals("horarios") || !raiz.getAttributes().getNamedItem("tipo").getNodeValue().toLowerCase().equals("i")) {
                ret = false;
                setMensajeError("El fichero de horarios no es válido.");
            } else {
                //TODO Revisar documentación para ver posibles variantes no controladas
                //Primero cogemos el bloque bloque_datos
                NodeList bloquesDatos = doc.getElementsByTagName("BLOQUE_DATOS");
                if (bloquesDatos.getLength() > 0) {
                    Node bloqueDatos = bloquesDatos.item(0);
                    //Este bloque tiene dos grupo datos uno con el curso y otro con los datos
                    NodeList grupos = bloqueDatos.getChildNodes();
                    int totalGrupos = grupos.getLength();
                    Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.INFO, "Total grupos : {0}", totalGrupos);
                    ret = true;
                    for (int s = 0; s < grupos.getLength() && ret; s++) {
                        Node grupo = grupos.item(s);
                        if (grupo.getNodeType() == Node.ELEMENT_NODE) {
                            Element elementoGrupo = (Element) grupo;
                            String tipoGrupo = elementoGrupo.getAttribute("seq");
                            if (tipoGrupo.equals("ANNO_ACADEMICO")) {
                                ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                                for (DatoXML d : datos) {
                                    if (d.getNombre().equals("C_ANNO")) {
                                        int ano = Num.getInt(d.getValor());
                                        if (ano != getAnoEscolar().getAno()) {
                                            ret = false;
                                            setMensajeError("El año escolar del fichero de horarios no se corresponde con el año escolar en curso.");
                                        }
                                    }
                                }

                            } else if (tipoGrupo.startsWith("HORARIOS_REGULARES")) {
                                //Sobre este nodo vamos iterando en cada profesor
                                NodeList profesores = elementoGrupo.getChildNodes();

                                for (int i = 0; i < profesores.getLength(); i++) {
                                    Node profesor = profesores.item(i);
                                    if (profesor.getNodeType() == Node.ELEMENT_NODE) {
                                        Par<Profesor, ArrayList<Horario>> horarioProf=procesarHorarioProfesor((Element) profesor);
                                        if(horarioProf!=null){
                                            horarios.add(horarioProf);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ret = false;
                    setMensajeError("No se ha encontrado el bloque de datos en el fichero. Puede que esté mal generado.");
                }

                if (ret) {
                    ret=guardarHorarios(horarios);
                    //Ahora tenemos que corregir los problemas que genera howr en los horarios
                    //firePropertyChange("setMensaje", null, "Corrigiendo errores de Howr...");
                    //corregirErroresHorariosHowr();
                    //Ahora creamos la asignación de horarios
//                    firePropertyChange("setMensaje", null, "Asignado horarios a alumnos");
//                    MatriculacionAlumno.reasignarHorariosAlumnos();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, "Error procesando archivo de horarios de Séneca", ex);
        }
        return ret;
    }

//    public static void corregirErroresHorariosHowr() {
//        //Howr introduce en los cursos mixtos el código de una de las materias en vez de la correspondiente su curso
//        String sql = "SELECT  h.id AS idHorario,m2.id AS idMateria FROM horarios_ AS h "
//                + " JOIN materias AS m ON m.id=h.materia_id "
//                + " JOIN unidades AS u ON u.id=h.unidad_id "
//                + " JOIN materias AS m2 ON m2.nombre=m.nombre AND m2.curso_id=u.curso_id "
//                + " WHERE h.ano=? AND m.curso_id!=u.curso_id";
//        PreparedStatement st = null;
//        PreparedStatement stActu = null;
//        ResultSet res = null;
//        try {
//            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sql);
//            stActu = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("UPDATE horarios SET materia_id=? WHERE id=?");
//            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
//            res = st.executeQuery();
//            while (res.next()) {
//                int id = res.getInt("idHorario");
//                int idMateria = res.getInt("idMateria");
//                stActu.setInt(1, idMateria);
//                stActu.setInt(2, id);
//                stActu.addBatch();
//            }
//            stActu.executeBatch();
//            //Una vez corregidos los errores hay que reasignar los horarios faltantes
//            MatriculacionAlumno.asignarHorariosFaltantesAlumnos();
//        } catch (Exception ex) {
//            Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, "Error corrigiendo errores de Howr", ex);
//        }
//        Obj.cerrar(st, res, stActu);
//    }

    private Par<Profesor, ArrayList<Horario>> procesarHorarioProfesor(Element profesor) {
        boolean ok = true;
        Profesor profesorActivo = null;
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        //Ahora recorremos todos los nodos tenemos dos tipos datos y grupos datos
        //Los datos nos referencian al profesor y el grupo_datos sus diferentes horarios
        NodeList subs = profesor.getChildNodes();
        for (int x = 0; x < subs.getLength() && ok; x++) {
            Node n = subs.item(x);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ne = (Element) n;
                if (n.getNodeName().toLowerCase().equals("dato")) {
                    if (ne.getAttribute("nombre_dato").toLowerCase().equals("x_empleado")) {
                        //Es el profesor y le asignamos el dato
                        DatoXML d = getDato(ne);
                        int codProf = Num.getInt(d.getValor());
                        profesorActivo = Profesor.getProfesorPorCodigo(codProf, getAnoEscolar());
                        if (profesorActivo == null) {
                            ok = false;
                        }
                    }
                } else if (n.getNodeName().toLowerCase().equals("grupo_datos")) {
                    Horario h = procesarHorario(ne);
                    if (h != null) {
                        horarios.add(h);
                    }
                }
            }
        }
        if (ok) {
            Par<Profesor, ArrayList<Horario>> dato= new Par<Profesor, ArrayList<Horario>>(profesorActivo,horarios);
            return dato;
        }
        return null;
    }

    private Horario procesarHorario(Element el) {
        Horario h = new Horario();
        ArrayList<DatoXML> datos = (getDatos(el));
        h.setAnoEscolar(getAnoEscolar());
        h.setDicu(ParteFaltas.DICU_AMBOS);//TODO ¿Como lo sabemos?,¿Necesitamos saberlo?
        for (DatoXML d : datos) {
            /**
            <dato nombre_dato="F_INICIO">01/09/2009</dato>
            <dato nombre_dato="F_FIN">31/08/2010</dato>
             */
            if (d.getNombre().equals("N_DIASEMANA")) {
                h.setDia(Num.getInt(d.getValor()));
            } else if (d.getNombre().equals("X_TRAMO")) {
                int cod = Num.getInt(d.getValor());
                TramoHorario t = null;
                int hora = 0;
                for (int i = 0; i < getTramos().size(); i++) {
                    TramoHorario th = getTramos().get(i);
                    if (th.getCodigo().equals(cod)) {
                        t = th;
                        hora = i + 1;
                    }
                }
                if (t == null) {
                    try {
                        t = new TramoHorario(getAnoEscolar(), Num.getInt(d.getValor()));
                        h.setTramo(t.getId());
                        h.setHora(t.getHoraHorarios());
                    } catch (Exception ex) {
                        Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
                        //TODO Implementar error
                    }
                } else {
                    h.setTramo(t.getId());
                    h.setHora(hora);
                }

            } else if (d.getNombre().equals("X_DEPENDENCIA")) {
                try {
                    //TODO Implementar cache en la carga por codigo
                    if (d.getValor() != null) {
                        Dependencia dep = new Dependencia(getAnoEscolar(), d.getValor());
                        h.setDependencia(dep.getId());
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (d.getNombre().equals("X_UNIDAD")) {
                try {
                    //TODO Implementar cache en la carga por codigo
                    Unidad ud = new Unidad(getAnoEscolar(), Num.getInt(d.getValor()));
                    if (ud != null) {
                        h.setUnidad(ud.getId());
                    } else {
                        //TODO Implementar error
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (d.getNombre().equals("X_ACTIVIDAD")) {
                try {
                    //TODO Implementar cache en la carga por codigo
                    Actividad ac = new Actividad(getAnoEscolar(), Num.getInt(d.getValor()));
                    if (ac != null) {
                        h.setActividad(ac.getId());
                    } else {
                        //TODO Implementar error
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (d.getNombre().equals("X_OFERTAMATRIG")) {
                //es el código de curso pero inicialmente no es necesario para el horario
            } else if (d.getNombre().equals("X_MATERIAOMG")) {
                try {
                    //TODO Implementar cache en la carga por codigo
                    Materia m = new Materia(getAnoEscolar(), Num.getInt(d.getValor()));
                    if (m != null) {
                        h.setMateria(m.getId());
                    }
                    //Si no hay materia es que es una actividad
                } catch (Exception ex) {
                    Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return h;
    }
//    public void reordenarCursosUnidad() {
//        ArrayList<Unidad> unidades = new ArrayList<Unidad>();
//        try {
//            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE ano=? AND curso2_id IS NOT NULL");
//            st.setInt(1, getAnoEscolar().getId());
//            ResultSet res = st.executeQuery();
//            while (res.next()) {
//                Unidad u = new Unidad();
//                u.cargarDesdeResultSet(res);
//                unidades.add(u);
//            }
//            Obj.cerrar(st, res);
//        } catch (SQLException ex) {
//            Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        for (Unidad u : unidades) {
//            try {
//                Curso a = Curso.getCurso(u.getIdCurso());
//                Curso b = Curso.getCurso(u.getIdCurso2());
//                if (b.getNumeroDeAlumnosUnidad(u.getId()) > a.getNumeroDeAlumnosUnidad(u.getId())) {
//                    u.setIdCurso(b.getId());
//                    u.setIdCurso2(a.getId());
//                    u.guardar();
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(ImportadorFicheroHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
}
