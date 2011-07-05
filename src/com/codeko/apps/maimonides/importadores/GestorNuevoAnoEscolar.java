package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.*;
import com.codeko.util.Archivo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Codeko
 */
public class GestorNuevoAnoEscolar extends MaimonidesBean implements PropertyChangeListener {
    // Directorio donde se almacenan los datos
    private File directorioDatos = null;
    //Año escolar actual
    private AnoEscolar anoEscolar = null;
    //Archivo de exportación hacia generadores de horarios
    private File archivoHorarios = null;
    //Arhivo de datos de horw
    private File archivoHorw = null;
    //Archivo de tutores
    private File archivoTutores = null;
    //Archivos de matriculacion
    private ArrayList<File> archivosMatriculas = new ArrayList<File>();
    //Archivos de faltas de alumnos (para recoger datos de alumnos)
    private ArrayList<File> archivosFaltas = new ArrayList<File>();
    private ArrayList<String> erroresArchivos = new ArrayList<String>();
    private boolean hayErrores = false;
    DefaultMutableTreeNode arbolErrores = null;
    ImportadorDatosGeneralesSeneca importador = null;

    public ArrayList<String> getErroresArchivos() {
        return erroresArchivos;
    }

    public void setErroresArchivos(ArrayList<String> erroresArchivos) {
        this.erroresArchivos = erroresArchivos;
    }

    public DefaultMutableTreeNode getArbolErrores() {
        return arbolErrores;
    }

    public void setArbolErrores(DefaultMutableTreeNode arbolErrores) {
        this.arbolErrores = arbolErrores;
    }

    public boolean isHayErrores() {
        return hayErrores;
    }

    public void setHayErrores(boolean hayErrores) {
        this.hayErrores = hayErrores;
    }

    public AnoEscolar getAnoEscolar() {
        if(anoEscolar==null){
            if(importador!=null && importador.getAnoEscolar()!=null){
                anoEscolar=importador.getAnoEscolar();
            }
        }
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }

    public File getArchivoTutores() {
        return archivoTutores;
    }

    public void setArchivoTutores(File archivoTutores) {
        this.archivoTutores = archivoTutores;
    }

    public File getArchivoHorarios() {
        return archivoHorarios;
    }

    public void setArchivoHorarios(File archivoHorarios) {
        firePropertyChange("archivoHorarios", this.archivoHorarios, archivoHorarios);
        this.archivoHorarios = archivoHorarios;
    }

    public void addArchivoFaltas(File archivo) {
        if (!getArchivosFaltas().contains(archivo)) {
            firePropertyChange("addArchivoFaltas", null, archivo);
            getArchivosFaltas().add(archivo);
        }
    }

    public ArrayList<File> getArchivosFaltas() {
        return archivosFaltas;
    }

    public void addArchivoMatriculas(File archivo) {
        if (!getArchivosMatriculas().contains(archivo)) {
            firePropertyChange("addArchivoMatriculas", null, archivo);
            getArchivosMatriculas().add(archivo);
        }
    }

    public ArrayList<File> getArchivosMatriculas() {
        return archivosMatriculas;
    }

    public File getDirectorioDatos() {
        if (directorioDatos == null) {
            setDirectorioDatos(new File("datos", getAnoEscolar().getId() + ""));
            directorioDatos.mkdirs();
        }
        return directorioDatos;
    }

    public void setDirectorioDatos(File directorioDatos) {
        firePropertyChange("directorioDatos", this.directorioDatos, directorioDatos);
        this.directorioDatos = directorioDatos;
    }

    public File getArchivoHorw() {
        return archivoHorw;
    }

    public void setArchivoHorw(File archivoHorw) {
        firePropertyChange("archivoHorw", this.archivoHorw, archivoHorw);
        this.archivoHorw = archivoHorw;
    }

    public GestorNuevoAnoEscolar(AnoEscolar anoEscolar) {
        setAnoEscolar(anoEscolar);
    }

    public boolean nuevoAnoEscolar(File directorioDatos) throws Exception {
        boolean ret = true;
        setDirectorioDatos(directorioDatos);
        ret = procesarDirectorioDatos();
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Problemas");
        boolean hayProblemas = false;
        if (ret) {
            //Creamos un importador de arhivo de horarios
            importador = new ImportadorDatosGeneralesSeneca(getAnoEscolar(), getArchivoHorarios());
            importador.addPropertyChangeListener(this);
            ret = importador.importarDatosGeneralesSeneca();
            if (ret) {
                setAnoEscolar(importador.getAnoEscolar());
                ImportadorArchivoAlumnos alumnos = new ImportadorArchivoAlumnos(getAnoEscolar(), getArchivosFaltas());
                alumnos.addPropertyChangeListener(this);
                ret = alumnos.importarAlumnos();
            }
            if (ret) {
                ImportadorArchivosMatriculas matriculas = new ImportadorArchivosMatriculas(getAnoEscolar(), getArchivosMatriculas());
                matriculas.addPropertyChangeListener(this);
                ret = matriculas.importarMatriculas();

                if (ret && matriculas.getAlumnosNoEncontrados().size() > 0) {
                    hayProblemas = true;
                    DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Hay datos de matrículas de alumnos no matriculados. Posiblemente sean matrículas canceladas o transladadas.");
                    for (String a : matriculas.getAlumnosNoEncontrados()) {
                        DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                        nodo.add(n);
                    }
                    top.add(nodo);
                }
            }
            if (ret) {
                ImportadorArchivoTutores tutores = new ImportadorArchivoTutores(getAnoEscolar(), getArchivoTutores());
                tutores.addPropertyChangeListener(this);
                ret = tutores.importarTutores();
                if (tutores.getErrores().size() > 0) {
                    hayProblemas = true;
                    DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Ha habido algunos errores importando el archivo de tutores");
                    for (String a : tutores.getErrores()) {
                        DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                        nodo.add(n);
                    }
                    top.add(nodo);
                }
            }
            if (ret) {
                //Ahora que tenemos los alumnos reordenamos los cursos de las unidades según numero de alumnos
                importador.reordenarCursosUnidad();
            }
            if (ret) {
                firePropertyChange("setMensaje", null, "Verificando errores...");
                ImportadorHorw horw = new ImportadorHorw(getAnoEscolar(), getArchivoHorw());
                horw.addPropertyChangeListener(this);
                ret = horw.importarHorarios();
                if (ret) {
                    if (horw.getAsignaturasNoExistentes().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Las siguientes asignaturas no existen en Séneca y sí en Horw");
                        for (String a : horw.getAsignaturasNoExistentes()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }
                    if (horw.getDependenciasNoExistentes().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Las siguientes dependencias (Aulas) no existen en Séneca y sí en Horw");
                        for (String a : horw.getDependenciasNoExistentes()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }
                    if (horw.getUnidadesNoExistentes().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Las siguientes unidades (Curso+Grupo) no existen en Séneca y sí en Horw");
                        for (String a : horw.getUnidadesNoExistentes()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }
                    if (horw.getProfesoresNoExistentes().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Los siguientes profesores no existen en Séneca y sí en Horw");
                        for (String a : horw.getProfesoresNoExistentes()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }

                    if (horw.getAsignaturasRepetidas().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Las siguientes asignaturas existen duplicadas (Igual nombre y curso) por lo que no se puede determinar cual es la correcta.");
                        for (String a : horw.getAsignaturasRepetidas()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }
                    if (horw.getDependenciasRepetidas().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Las siguientes dependencias existen duplicadas (Igual nombre) por lo que no se puede determinar cual es la correcta.");
                        for (String a : horw.getDependenciasRepetidas()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }
                    if (horw.getUnidadesRepetidas().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Las siguientes unidades existen duplicadas (Igual nombre y curso) por lo que no se puede determinar cual es la correcta.");
                        for (String a : horw.getUnidadesRepetidas()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }
                    if (horw.getProfesoresRepetidos().size() > 0) {
                        hayProblemas = true;
                        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Los siguientes profesores existen duplicados (Igual nombre y apellidos) por lo que no se puede determinar cual es la correcta.");
                        for (String a : horw.getProfesoresRepetidos()) {
                            DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                            nodo.add(n);
                        }
                        top.add(nodo);
                    }

                }
            }
            if (hayProblemas) {
                setArbolErrores(top);
                setHayErrores(true);
            }
        } else {
            if (getErroresArchivos().size() > 0) {
                DefaultMutableTreeNode nodo = new DefaultMutableTreeNode("Los siguientes archivos no se encuentran en el directorio de datos (" + getDirectorioDatos() + ")");
                for (String a : getErroresArchivos()) {
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode(a);
                    nodo.add(n);
                }
                top.add(nodo);
                setArbolErrores(top);
                setHayErrores(true);
            }
        }
        return ret;
    }

    /**
     * Analiza el directorio de datos buscando los distintos tipos de archivo que necesita y procesandolos
     * @return true si ha encontrado todos los archivos necesarios para la creación de un nuevo año, false si no
     */
    public boolean procesarDirectorioDatos() {
        boolean ret = true;
        firePropertyChange("setMensaje", null, "Procesando directorio de datos...");
        File[] archivos = getDirectorioDatos().listFiles();
        int count = 0;
        int max = archivos.length;
        firePropertyChange("setMinimo", null, 0);
        firePropertyChange("setMaximo", 0, max);
        for (File f : archivos) {
            firePropertyChange("setProgreso", count, ++count);
            firePropertyChange("setMensaje", null, "Procesando archivo " + count + " de " + max + ".");
            firePropertyChange("setInfoExtra", null, f);
            if (f.isDirectory()) {
                Logger.getLogger(GestorNuevoAnoEscolar.class.getName()).log(Level.INFO, "Archivo {0} es directorio. Ignorado.", f);
            } else {
                if (f.getName().endsWith(".zip")) {
                    //gr7693gr Si es un archivo zip es un archivo de faltas de alumnado. Lo descomprimirmos y asignamos los archivos
                    //El archivo se recupera de Utilidades->Exportación desde séneca->Faltas de alumnado
                    firePropertyChange("setMensaje", null, "Descomprimiendo " + f + "...");
                    Logger.getLogger(GestorNuevoAnoEscolar.class.getName()).log(Level.INFO, "Descomprimiendo archivo de faltas de alumnado {0}.", f);
                    getArchivosFaltas().addAll(Archivo.descomprimirZip(f, new File(getDirectorioDatos(), "faltas"), true));
                } else if (f.getName().equals("Exportacion_hacia_generadores_de_horarios.xml") || f.getName().equals("ExportacionHorarios.xml")) {
                    //Si es un xml es el archivo de exportación a generadores de horarios
                    //El archivo se recupera de Utilidades->Exportación desde séneca->Exportación hacia generadores de horarios
                    setArchivoHorarios(f);
                } else if (f.getName().endsWith(".csv")) {
                    if (f.getName().equals("RegUnidades.csv")) {
                        setArchivoTutores(f);
                    } else {
                        //Si es un csv es un archivo de matriculas
                        //El archivo se recupera de Alumnos->Matriculacion->relacion de matriculas->Asignaturas de las que se ha matriculado cada alumno->Exportar datos formato CSV
                        addArchivoMatriculas(f);
                    }
                } else if (f.getName().endsWith(".txt")) {
                    //Si es un archivo dbf es la exportación de datos de horw
                    setArchivoHorw(f);
                }
            }
        }

        //Ahora vemos si tenemos todo lo necesario
        if (getArchivosFaltas().isEmpty()) {
            getErroresArchivos().add("Archivo de faltas (*.zip)");
            ret = false;
        }
        if (getArchivosMatriculas().isEmpty()) {
            getErroresArchivos().add("Archivo de matrículas (*.csv)");
            ret = false;
        }
        if (getArchivoHorarios() == null || !getArchivoHorarios().exists()) {
            ret = false;
            getErroresArchivos().add("Exporación hacia generadores de horarios (ExportacionHorarios.xml)");
        }
        if (getArchivoHorw() == null || !getArchivoHorw().exists()) {
            ret = false;
            getErroresArchivos().add("Archivo de horarios de Horw (*.txt)");
        }
        if (getArchivoTutores() == null || !getArchivoTutores().exists()) {
            ret = false;
            getErroresArchivos().add("Archivo de tutores (RegUnidades.csv)");
        }
        if (ret) {
            firePropertyChange("setMensaje", null, "Directorio de datos procesado correctamente.");
        } else {
            firePropertyChange("setMensaje", null, "Error procesando directorio de datos. Faltan uno o más archivos.");
        }
        return ret;
    }

    public boolean borrarAnoEscolar() {
        PanelArbolErrores arbol = new PanelArbolErrores(getArbolErrores(), "Existen advertencias en la importación de datos. Por favor reviselas:");
        int op = JOptionPane.showOptionDialog(MaimonidesApp.getMaimonidesView().getFrame(), arbol, "Advertencias en la importación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Crear el nuevo año", "Cancelar creación"}, "Cancelar creación");
        return (op != JOptionPane.OK_OPTION);
    }

    public void mostrarErrores() {
        PanelArbolErrores arbol = new PanelArbolErrores(getArbolErrores(), "Existen errores creando el nuevo año escolar");
        JOptionPane.showMessageDialog(MaimonidesApp.getMaimonidesView().getFrame(), arbol, "Errores en la importación", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}
