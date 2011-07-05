package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.*;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImportadorDatosGeneralesSeneca extends MaimonidesBean {

    private File archivoHorariosSeneca = null;
    private AnoEscolar anoEscolar = null;
    private int importar = 0;
    public static final int PROFESORES = 1;
    public static final int DEPENDENCIAS = 2;
    public static final int CURSOS = 4;
    public static final int ACTIVIDADES = 8;
    public static final int TRAMOS = 16;
    public static final int UNIDADES = 32;
    public static final int MATERIAS = 64;
    public static final int TODO = PROFESORES | DEPENDENCIAS | CURSOS | ACTIVIDADES | TRAMOS | UNIDADES | MATERIAS;

    public int getImportar() {
        return importar;
    }

    public final void setImportar(int importar) {
        this.importar = importar;
    }

    public boolean isImportar(int tipo) {
        return (getImportar() & tipo) == tipo;
    }

    public static boolean isImportar(int tipoActual, int tipoComprobar) {
        return (tipoActual & tipoComprobar) == tipoComprobar;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }

    public ImportadorDatosGeneralesSeneca(AnoEscolar anoEscolar, File archivoHorariosSeneca) {
        this(anoEscolar, archivoHorariosSeneca, TODO);
    }

    public ImportadorDatosGeneralesSeneca(AnoEscolar anoEscolar, File archivoHorariosSeneca, int importar) {
        setAnoEscolar(anoEscolar);
        setArchivoHorariosSeneca(archivoHorariosSeneca);
        setImportar(importar);
    }

    public File getArchivoHorariosSeneca() {
        return archivoHorariosSeneca;
    }

    public final void setArchivoHorariosSeneca(File archivoHorariosSeneca) {
        firePropertyChange("archivoHorariosSeneca", this.archivoHorariosSeneca, archivoHorariosSeneca);
        this.archivoHorariosSeneca = archivoHorariosSeneca;
    }

    public ArrayList<DatoXML> getDatos(Element el) {
        NodeList datos = el.getElementsByTagName("dato");
        ArrayList<DatoXML> d = new ArrayList<DatoXML>();
        for (int i = 0; i < datos.getLength(); i++) {
            Node n = datos.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elemento = (Element) n;
                String nombre = elemento.getAttribute("nombre_dato");
                String valor = null;
                if (elemento.getChildNodes().getLength() > 0) {
                    valor = elemento.getChildNodes().item(0).getNodeValue();
                }
                d.add(new DatoXML(nombre, valor));
            }
        }
        return d;
    }

    public boolean importarDatosGeneralesSeneca() throws Exception {
        boolean ret = false;
        firePropertyChange("message", null, "Procensando XML...");
        Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.INFO, "Procesando archivo para Horarios de S\u00e9neca:{0}", getArchivoHorariosSeneca());
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(getArchivoHorariosSeneca());
        } catch (Exception ex) {
            Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Error procesando fichero XML de Séneca:\n" + ex.getLocalizedMessage());
        }

        //Normalizamos el texto
        doc.getDocumentElement().normalize();
        Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.INFO, "Elemento raiz es {0}", doc.getDocumentElement().getNodeName());
        NodeList grupos = doc.getElementsByTagName("grupo_datos");
        int totalGrupos = grupos.getLength();
        Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.INFO, "Total grupos : {0}", totalGrupos);
        int ano = -1;
        GregorianCalendar fini = null;
        GregorianCalendar ffin = null;
        ArrayList<IObjetoBD> unidades = new ArrayList<IObjetoBD>();
        ArrayList<IObjetoBD> profesores = new ArrayList<IObjetoBD>();
        ArrayList<Integer> codProfesores = new ArrayList<Integer>();
        ArrayList<IObjetoBD> tramos = new ArrayList<IObjetoBD>();
        ArrayList<IObjetoBD> materias = new ArrayList<IObjetoBD>();
        ArrayList<IObjetoBD> vCursos = new ArrayList<IObjetoBD>();
        HashMap<Integer, Curso> cursos = new HashMap<Integer, Curso>();

        ArrayList<IObjetoBD> actividades = new ArrayList<IObjetoBD>();
        ArrayList<IObjetoBD> dependencias = new ArrayList<IObjetoBD>();

        for (int s = 0; s < grupos.getLength(); s++) {
            Node grupo = grupos.item(s);
            if (grupo.getNodeType() == Node.ELEMENT_NODE) {
                Element elementoGrupo = (Element) grupo;
                String tipoGrupo = elementoGrupo.getAttribute("seq");
                if (tipoGrupo.equals("ANNO_ACADEMICO")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("C_ANNO")) {
                            ano = Num.getInt(d.getValor());
                        } else if (d.getNombre().equals("F_INIHORREG") && d.getValor() != null) {
                            Date fecha = Fechas.parse(d.getValor(), "dd/MM/yyyy");
                            if (fecha != null) {
                                fini = new GregorianCalendar();
                                fini.setTime(fecha);
                            }
                        } else if (d.getNombre().equals("F_FINHORREG") && d.getValor() != null) {
                            Date fecha = Fechas.parse(d.getValor(), "dd/MM/yyyy");
                            if (fecha != null) {
                                ffin = new GregorianCalendar();
                                ffin.setTime(fecha);
                            }
                        }
                    }
                    if (getAnoEscolar() == null) {
                        AnoEscolar a = new AnoEscolar();
                        a.setAno(ano);
                        ret = a.guardar();
                        setAnoEscolar(a);
                    }
                } else if (tipoGrupo.startsWith("CURSO_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    int codigo = -1;
                    String nombre = "";
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_OFERTAMATRIG")) {
                            codigo = Num.getInt(d.getValor());
                        } else if (d.getNombre().equals("D_OFERTAMATRIG")) {
                            nombre = d.getValor();
                        }
                    }
                    Curso c = new Curso(getAnoEscolar());
                    c.setCodigo(codigo);
                    c.setDescripcion(nombre);
                    c.setCurso(nombre.charAt(0) + "");
                    //La posición la da el nombre CURSO_#
                    c.setPosicion(Num.getInt(Num.limpiar(tipoGrupo)) * 10);
                    //Ahora añadimos el nuevo curso
                    cursos.put(codigo, c);
                    vCursos.add(c);
                } else if (tipoGrupo.startsWith("MATERIA_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    Materia m = new Materia();
                    m.setAnoEscolar(getAnoEscolar());
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_OFERTAMATRIG")) {
                            //Aquí estamos asignando el código de curso pero lo cambiaremos mas adelante por la id
                            int codCurso = Num.getInt(d.getValor());
                            if (!cursos.containsKey(codCurso)) {
                                //TODO Esto no es normal y se da mucho. Se dan ids de cursos que no existen. Ver documentacion.
                                Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.WARNING, "La materia: {0} tiene un curso no existente: {1}", new Object[]{m.getDescripcion(), codCurso});
                            } else {
                                m.setCurso(cursos.get(codCurso));
                            }
                        } else if (d.getNombre().equals("X_MATERIAOMG")) {
                            m.setCodigo(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("D_MATERIAC")) {
                            m.setDescripcion(d.getValor());
                        }
                    }
                    materias.add(m);

                } else if (tipoGrupo.startsWith("TRAMO_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    TramoHorario m = new TramoHorario();
                    m.setHora(Num.getInt(Num.limpiar(tipoGrupo)));
                    m.setAnoEscolar(getAnoEscolar());
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_TRAMO")) {
                            m.setCodigo(Num.getInt(d.getValor()));
                        } //De primeras la hora la cogemos del seq porque en este campo se pueden meter cosas como 'Tarde' y 'Tarde2'
                        //                            else if (d.getNombre().equals("T_HORCEN")) {
                        //                                m.setHora(Num.getInt(d.getValor()));
                        //                            }
                        else if (d.getNombre().equals("N_INICIO")) {
                            m.setHini(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("N_FIN")) {
                            m.setHfin(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("X_PLAJORESCCEN")) {
                            m.setJornada(Num.getInt(d.getValor()));
                        }
                    }
                    tramos.add(m);
                } else if (tipoGrupo.startsWith("UNIDAD_CURSO_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    Unidad u = new Unidad();
                    //La posición la da el nombre UNIDAD_CURSO_#
                    u.setPosicion(Num.getInt(Num.limpiar(tipoGrupo)));
                    u.setAnoEscolar(getAnoEscolar());
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_UNIDAD")) {
                            u.setCodigo(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("X_OFERTAMATRIG")) {
                            //Aquí de primeras estamos asociando con codigo de curso no con ID. Mas adelante cuando se guarden los cursos se cambiara
                            u.setIdCurso(Num.getInt(d.getValor()));
                            //Ahora buscamos la descripción del curso
                            if (cursos.containsKey(u.getIdCurso())) {
                                u.setDescripcion(cursos.get(u.getIdCurso()).getDescripcion());
                            }
                        } else if (d.getNombre().equals("T_NOMBRE")) {
                            String nombre = d.getValor();
                            u.setCursoGrupo(nombre);
                            u.setNombreOriginal(nombre);
                            //La letra de la unidad suele ser el último caracter
                            u.setGrupo(("" + nombre.charAt(nombre.length() - 1)).toUpperCase());
                            //Si no es una letra de la A a la Z la desasignamos
                            if (u.getGrupo().charAt(0) < 'A' || u.getGrupo().charAt(0) > 'Z') {
                                u.setGrupo("-");
                            }
                            //Ahora tenemos que asignar el curso y el grupo
                            int pos = nombre.indexOf("-");
                            if (pos == -1) {
                                pos = nombre.trim().indexOf(" ");
                            }
                            if (pos == -1) {
                                pos = nombre.trim().indexOf(".");
                            }
                            if (pos == -1) {
                                u.setCurso("-");
                            } else {
                                //Quitamos los posibles numeros del curso
                                String curso = nombre.substring(0, pos);
                                StringBuilder cadena = new StringBuilder("");
                                for (int i = 0; i < curso.length(); i++) {
                                    char car = curso.charAt(i);
                                    if (!Num.esNumero(car)) {
                                        cadena.append(car);
                                    }
                                }
                                u.setCurso(cadena.toString());
                            }
                        }
                    }
                    if (!unidades.contains(u)) {
                        unidades.add(u);
                    } else {
                        //Si ya existe vemos si tiene el mismo curso
                        for (IObjetoBD iBD : unidades) {
                            if (iBD instanceof Unidad) {
                                Unidad u2 = (Unidad) iBD;
                                //Si tienen distinto curso la misma unidad es que es una unidad mixta por lo que añadimos el curso de la segunda al curso2 de la primera
                                if (u2.getCodigo().intValue() == u.getCodigo().intValue() && u2.getIdCurso().intValue() != u.getIdCurso().intValue()) {
                                    u2.setIdCurso2(u.getIdCurso());
                                    //Y le cambiamos el nombre a mixto
                                    String nombre = u2.getDescripcion();
                                    int pos = nombre.indexOf("(");
                                    if (pos != -1) {
                                        nombre = nombre.substring(0, pos).trim();
                                    }
                                    nombre = nombre + " (Mixto)";
                                    u2.setDescripcion(nombre);
                                }
                            }
                        }
                    }
                } else if (tipoGrupo.startsWith("PROFESOR_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    Profesor p = new Profesor();
                    p.setAnoEscolar(getAnoEscolar());
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_EMPLEADO")) {
                            p.setCodigo(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("NOMBRE")) {
                            p.setNombre(d.getValor());
                        } else if (d.getNombre().equals("APELLIDO1")) {
                            p.setApellido1(d.getValor());
                        } else if (d.getNombre().equals("APELLIDO2")) {
                            p.setApellido2(d.getValor());
                        } else if (d.getNombre().equals("D_PUESTO")) {
                            p.setPuesto(d.getValor());
                        } else if (d.getNombre().equals("F_TOMAPOS")) {
                            Date fecha = Fechas.parse(d.getValor(), "dd/MM/yyyy");
                            if (fecha != null) {
                                GregorianCalendar cal = new GregorianCalendar();
                                cal.setTime(fecha);
                                p.setFechaTomaPosesion(cal);
                            }
                        }
                    }
                    if (codProfesores.contains(p.getCodigo())) {
                        Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.WARNING, "El profesor: {0} ya exist\u00eda. Ignorado.", p);
                    } else {
                        profesores.add(p);
                        codProfesores.add(p.getCodigo());
                    }

                } else if (tipoGrupo.startsWith("DEPENDENCIA_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    Dependencia dep = new Dependencia();
                    dep.setAnoEscolar(getAnoEscolar());
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_DEPENDENCIA")) {
                            dep.setCodigo(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("D_DEPENDENCIA")) {
                            dep.setNombre(d.getValor());
                        }
                    }
                    dependencias.add(dep);
                } else if (tipoGrupo.startsWith("ACTIVIDAD_")) {
                    ArrayList<DatoXML> datos = (getDatos(elementoGrupo));
                    Actividad act = new Actividad();
                    act.setAnoEscolar(getAnoEscolar());
                    for (DatoXML d : datos) {
                        if (d.getNombre().equals("X_ACTIVIDAD")) {
                            act.setCodigo(Num.getInt(d.getValor()));
                        } else if (d.getNombre().equals("D_ACTIVIDAD")) {
                            act.setDescripcion(d.getValor());
                        } else if (d.getNombre().equals("L_REGULAR")) {
                            act.setEsRegular(d.getValor().equals("S"));
                        } else if (d.getNombre().equals("L_REQUNIDAD")) {
                            act.setNecesitaUnidad(d.getValor().equals("S"));
                        } else if (d.getNombre().equals("L_REQMATERIA")) {
                            act.setNecesitaMateria(d.getValor().equals("S"));
                        }
                    }
                    actividades.add(act);
                }
            }
        }
        //Verificamos que el año sea correcto
        if (getAnoEscolar() == null || ano != getAnoEscolar().getAno()) {
            ret = false;
            firePropertyChange("message", null, "El año escolar del archivo de horarios es " + ano + " mientras que el asignado es " + getAnoEscolar().getAno() + ". Abortando importación.");
            Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.SEVERE, "Los a\u00f1os escolares no coinciden: Actual={0} En fichero de horarios={1}. Abortando importaci\u00f3n.", new Object[]{getAnoEscolar().getAno(), ano});
            throw new Exception("El año escolar del archivo de horarios es " + ano + " mientras que el asignado es " + getAnoEscolar().getAno() + ". Abortando importación.");
        } else {
            if (isImportar(PROFESORES)) {
                firePropertyChange("message", null, "Guardando profesores...");
                ret = guardarObjetosBD(profesores, true);
            }
            if (ret && isImportar(DEPENDENCIAS)) {
                firePropertyChange("message", null, "Guardando dependencias...");
                ret = guardarObjetosBD(dependencias, true);
            }
            if (ret && isImportar(CURSOS)) {
                firePropertyChange("message", null, "Guardando cursos...");
                ret = guardarObjetosBD(vCursos, true);
            }
            if (ret && isImportar(ACTIVIDADES)) {
                firePropertyChange("message", null, "Guardando actividades...");
                ret = guardarObjetosBD(actividades, true);
            }
            if (ret && isImportar(TRAMOS)) {
                firePropertyChange("message", null, "Guardando tramos horarios...");
                ret = guardarObjetosBD(tramos, true);
            }
            if (ret && isImportar(UNIDADES)) {
                firePropertyChange("message", null, "Guardando unidades...");
                //Antes de guardar las unidades tenemos que asignarle la id de curso (ahora mismo esta asignado código de curso)
                for (IObjetoBD oBD : unidades) {
                    Unidad u = (Unidad) oBD;
                    //Buscamos su curso
                    for (IObjetoBD coBD : vCursos) {
                        Curso c = (Curso) coBD;
                        if (c.getCodigo().intValue() == u.getIdCurso()) {
                            u.setIdCurso(c.getId());
                            //Si tiene más de dos caracteres es que ya se le ha asignado
                            if (!(c.getCurso().length() > 1)) {
                                c.setCurso(c.getCurso() + u.getCurso());
                            }
                            Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.FINE, "Asignado ID CURSO {0} a Unidad {1}", new Object[]{c.getId(), u.getCodigo()});
                        }
                        //Y asignamos el curso 2 tambien si existe
                        if (u.getIdCurso2() != null && c.getCodigo().intValue() == u.getIdCurso2()) {
                            u.setIdCurso2(c.getId());
                            //Si tiene más de dos caracteres es que ya se le ha asignado
                            if (!(c.getCurso().length() > 1)) {
                                c.setCurso(c.getCurso() + u.getCurso());
                            }
                            Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.FINE, "Asignado ID CURSO 2 {0} a Unidad {1}", new Object[]{c.getId(), u.getCodigo()});
                        }
                    }
                }
                ret = guardarObjetosBD(unidades, true);
                //Y volvemos a gaurdar los cursos con el campo curso correcto
                ret = guardarObjetosBD(vCursos, true);
            }
            if (ret && isImportar(MATERIAS)) {
                firePropertyChange("message", null, "Guardando materias...");
                ret = guardarObjetosBD(materias, true);
            }
        }

        return ret;
    }

    public void reordenarCursosUnidad() {
        ArrayList<Unidad> unidades = new ArrayList<Unidad>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE ano=? AND curso2_id IS NOT NULL");
            st.setInt(1, getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Unidad u = new Unidad();
                u.cargarDesdeResultSet(res);
                unidades.add(u);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Unidad u : unidades) {
            try {
                Curso a = Curso.getCurso(u.getIdCurso());
                Curso b = Curso.getCurso(u.getIdCurso2());
                if (b.getNumeroDeAlumnosUnidad(u.getId()) > a.getNumeroDeAlumnosUnidad(u.getId())) {
                    u.setIdCurso(b.getId());
                    u.setIdCurso2(a.getId());
                    u.guardar();
                }
            } catch (Exception ex) {
                Logger.getLogger(ImportadorDatosGeneralesSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
