package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.*;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImportadorArchivoAlumnos extends MaimonidesBean {

    private ArrayList<File> archivoAlumnos = null;
    private AnoEscolar anoEscolar = null;

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }

    public ImportadorArchivoAlumnos(AnoEscolar anoEscolar, ArrayList<File> archivoHorariosSeneca) {
        setAnoEscolar(anoEscolar);
        setArchivoAlumnos(archivoHorariosSeneca);
    }

    public ArrayList<File> getArchivoAlumnos() {
        return archivoAlumnos;
    }

    public final void setArchivoAlumnos(ArrayList<File> archivoHorariosSeneca) {
        firePropertyChange("archivoHorariosSeneca", this.archivoAlumnos, archivoHorariosSeneca);
        this.archivoAlumnos = archivoHorariosSeneca;
    }

    public boolean actualizarCodigoSenecaAlumnos() {
        boolean ret = false;
        try {
            //TODO Limpiar esto creo que se podrían quitar procesos
            firePropertyChange("message", null, "Procensando XML...");
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            for (File f : getArchivoAlumnos()) {
                Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.INFO, "Procesando archivo de faltas para cargar alumnos:{0}", f);
                Document doc = docBuilder.parse(f);
                //Normalizamos el texto
                doc.getDocumentElement().normalize();
                NodeList nodoAno = doc.getElementsByTagName("C_ANNO");
                Node datoAno = nodoAno.item(0);
                int ano = Num.getInt(datoAno.getChildNodes().item(0).getNodeValue());
                //Verificamos que el año sea correcto
                if (ano != getAnoEscolar().getAno()) {
                    ret = false;
                    firePropertyChange("message", null, "El año escolar del archivo de horarios es " + ano + " mientras que el asignado es " + getAnoEscolar().getAno() + ". Abortando importación.");
                    Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.SEVERE, "Los a\u00f1os escolares no coinciden: Actual={0} En fichero de horarios={1}. Abortando importaci\u00f3n.", new Object[]{getAnoEscolar().getAno(), ano});
                } else {
                    //Ahora vamos recogiendo los cursos
                    NodeList cursos = doc.getElementsByTagName("CURSO");
                    for (int c = 0; c < cursos.getLength(); c++) {
                        Node curso = cursos.item(c);

                        //Ahora tenemos que recuperar el código de curso
                        NodeList datosCurso = curso.getChildNodes();//doc.getElementsByTagName("UNIDAD");
                        for (int s = 0; s < datosCurso.getLength(); s++) {
                            Node datoCurso = datosCurso.item(s);
                            if (datoCurso.getNodeName().equals("UNIDADES")) {
                                NodeList unidades = ((Element) datoCurso).getElementsByTagName("UNIDAD");
                                for (int ud = 0; ud < unidades.getLength(); ud++) {
                                    NodeList elementosUnidad = unidades.item(ud).getChildNodes();
                                    int numUnidad = 0;
                                    Element elementoAlumnos = null;
                                    for (int j = 0; j < elementosUnidad.getLength(); j++) {
                                        Node nodoDatoUnidad = elementosUnidad.item(j);
                                        if (nodoDatoUnidad.getNodeType() == Node.ELEMENT_NODE) {
                                            Element elementoUnidad = (Element) nodoDatoUnidad;
                                            if ("X_UNIDAD".equals(elementoUnidad.getNodeName())) {
                                                numUnidad = Num.getInt(elementoUnidad.getTextContent());
                                            } else if ("ALUMNOS".equals(elementoUnidad.getNodeName())) {
                                                elementoAlumnos = elementoUnidad;
                                            }
                                        }
                                    }
                                    if (numUnidad > 0 && elementoAlumnos != null) {
                                        NodeList alumnosNode = elementoAlumnos.getElementsByTagName("ALUMNO");
                                        for (int x = 0; x < alumnosNode.getLength(); x++) {
                                            Node nAlumno = alumnosNode.item(x);
                                            String codFaltas = "";
                                            String numEscolar = "";
                                            NodeList datosAlumno = nAlumno.getChildNodes();
                                            for (int z = 0; z < datosAlumno.getLength(); z++) {
                                                Node datoAlumno = datosAlumno.item(z);
                                                if (datoAlumno.getNodeType() == Node.ELEMENT_NODE) {
                                                    Element elementoDatoAlumno = (Element) datoAlumno;
                                                    if ("X_MATRICULA".equals(elementoDatoAlumno.getNodeName())) {
                                                        codFaltas = Str.noNulo(elementoDatoAlumno.getTextContent());
                                                    } else if ("C_NUMESCOLAR".equals(elementoDatoAlumno.getNodeName())) {
                                                        numEscolar = elementoDatoAlumno.getTextContent();
                                                    }
                                                }
                                            }
                                            if (!codFaltas.equals("") && !numEscolar.trim().equals("")) {
                                                Alumno a = Alumno.getAlumnoDesdeNumEscolar(numEscolar);
                                                if (a != null && Num.getInt(a.getCodigo()) == 0) {
                                                    a.setCodFaltas(codFaltas);
                                                    a.guardar();
                                                }
                                            }
                                        }
                                    } else {
                                        //TODO Mostrar mensaje de error
                                        Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.SEVERE, "No existe informaci\u00f3n de unidad en el archivo: {0}", f);
                                    }
                                }
                            }
                        }
                    }
                    ret = true;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.SEVERE, "Error procesando archivo de horarios de Séneca", ex);
        }
        return ret;
    }

    public boolean importarAlumnos() {
        boolean ret = false;
        try {
            firePropertyChange("setMensaje", null, "Procensando XML...");
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            for (File f : getArchivoAlumnos()) {
                Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.INFO, "Procesando archivo de faltas para cargar alumnos:{0}", f);
                Document doc = docBuilder.parse(f);
                //Normalizamos el texto
                doc.getDocumentElement().normalize();
                NodeList nodoAno = doc.getElementsByTagName("C_ANNO");
                Node datoAno = nodoAno.item(0);
                int ano = Num.getInt(datoAno.getChildNodes().item(0).getNodeValue());
                //Verificamos que el año sea correcto
                if (ano != getAnoEscolar().getAno()) {
                    ret = false;
                    firePropertyChange("setMensaje", null, "El año escolar del archivo de horarios es " + ano + " mientras que el asignado es " + getAnoEscolar().getAno() + ". Abortando importación.");
                    Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.SEVERE, "Los a\u00f1os escolares no coinciden: Actual={0} En fichero de horarios={1}. Abortando importaci\u00f3n.", new Object[]{getAnoEscolar().getAno(), ano});
                } else {
                    ArrayList<IObjetoBD> alumnos = new ArrayList<IObjetoBD>();
                    //Ahora vamos recogiendo los cursos
                    NodeList cursos = doc.getElementsByTagName("CURSO");
                    for (int c = 0; c < cursos.getLength(); c++) {
                        Node curso = cursos.item(c);
                        Integer idCurso = null;
                        //Ahora tenemos que recuperar el código de curso
                        NodeList datosCurso = curso.getChildNodes();//doc.getElementsByTagName("UNIDAD");
                        for (int s = 0; s < datosCurso.getLength(); s++) {
                            Node datoCurso = datosCurso.item(s);
                            if (datoCurso.getNodeName().equals("X_OFERTAMATRIG")) {
                                idCurso = MaimonidesUtil.getIdTabla("cursos", getAnoEscolar(), Num.getInt(datoCurso.getTextContent()));
                            } else if (datoCurso.getNodeName().equals("UNIDADES")) {
                                NodeList unidades = ((Element) datoCurso).getElementsByTagName("UNIDAD");
                                for (int ud = 0; ud < unidades.getLength(); ud++) {
                                    NodeList elementosUnidad = unidades.item(ud).getChildNodes();
                                    int numUnidad = 0;
                                    Element elementoAlumnos = null;
                                    for (int j = 0; j < elementosUnidad.getLength(); j++) {
                                        Node nodoDatoUnidad = elementosUnidad.item(j);
                                        if (nodoDatoUnidad.getNodeType() == Node.ELEMENT_NODE) {
                                            Element elementoUnidad = (Element) nodoDatoUnidad;
                                            if ("X_UNIDAD".equals(elementoUnidad.getNodeName())) {
                                                numUnidad = Num.getInt(elementoUnidad.getTextContent());
                                            } else if ("ALUMNOS".equals(elementoUnidad.getNodeName())) {
                                                elementoAlumnos = elementoUnidad;
                                            }
                                        }
                                    }
                                    if (numUnidad > 0 && elementoAlumnos != null) {
                                        NodeList alumnosNode = elementoAlumnos.getElementsByTagName("ALUMNO");
                                        for (int x = 0; x < alumnosNode.getLength(); x++) {
                                            Node nAlumno = alumnosNode.item(x);
                                            Alumno al = new Alumno();
                                            al.setAnoEscolar(getAnoEscolar());
                                            al.setIdUnidad(MaimonidesUtil.getIdTabla("unidades", getAnoEscolar(), numUnidad));
                                            al.setIdCurso(idCurso);
                                            NodeList datosAlumno = nAlumno.getChildNodes();
                                            for (int z = 0; z < datosAlumno.getLength(); z++) {
                                                Node datoAlumno = datosAlumno.item(z);
                                                if (datoAlumno.getNodeType() == Node.ELEMENT_NODE) {
                                                    Element elementoDatoAlumno = (Element) datoAlumno;
                                                    if ("X_MATRICULA".equals(elementoDatoAlumno.getNodeName())) {
                                                        al.setCodigo(Num.getInt(elementoDatoAlumno.getTextContent()));
                                                    } else if ("T_APELLIDO1".equals(elementoDatoAlumno.getNodeName())) {
                                                        al.setApellido1(elementoDatoAlumno.getTextContent());
                                                    } else if ("T_APELLIDO2".equals(elementoDatoAlumno.getNodeName())) {
                                                        al.setApellido2(elementoDatoAlumno.getTextContent());
                                                    } else if ("T_NOMBRE_ALU".equals(elementoDatoAlumno.getNodeName())) {
                                                        al.setNombre(elementoDatoAlumno.getTextContent());
                                                    } else if ("C_NUMESCOLAR".equals(elementoDatoAlumno.getNodeName())) {
                                                        al.setNumeroEscolar(elementoDatoAlumno.getTextContent());
                                                    }
                                                }
                                            }
                                            //Tenemos que ver si ya existe
                                            if (al.existe()) {
                                                firePropertyChange("alumnoExiste", null, al);
                                            } else {
                                                alumnos.add(al);
                                                firePropertyChange("alumnoNuevo", null, al);
                                            }
                                        }
                                    } else {
                                        //TODO Mostrar mensaje de error
                                        Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.SEVERE, "No existe informaci\u00f3n de unidad en el archivo: {0}", f);
                                    }
                                }
                            }
                        }
                    }
                    ret = guardarObjetosBD(alumnos);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(ImportadorArchivoAlumnos.class.getName()).log(Level.SEVERE, "Error procesando archivo de horarios de Séneca", ex);
        }
        return ret;
    }
}
