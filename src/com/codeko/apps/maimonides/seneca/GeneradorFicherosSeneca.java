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


package com.codeko.apps.maimonides.seneca;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.AlumnoEnvioErroneo;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.EnvioErroneo;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.GestorEnvioFaltas;
import com.codeko.util.Archivo;
import com.codeko.util.Cripto;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
//TODO Parametrizar la inclusión de alumnos mixtos.
public class GeneradorFicherosSeneca extends MaimonidesBean {

    Document documento = null;
    Element raiz = null;
    File carpetaSalida = null;
    File carpetaFallidos = null;
    private int diasParaSincronizar = 15;
    private boolean faltasExportadas = false;
    private boolean ficherosGenerados = false;
    boolean cancelado = false;
    boolean enviarASeneca = false;
    ClienteSeneca clienteSeneca = null;
    ArrayList<String> errores = new ArrayList<String>();
    boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ArrayList<String> getErrores() {
        return errores;
    }

    public boolean isEnviarASeneca() {
        return enviarASeneca;
    }

    protected int enviarFichero(File f, String codigoOperacion) {
        int numIntentos = 0;
        int maxIntentos = 2;
        //TODO Gestionar los posibles errores. Sería interesante darle un código a la transacción que se asigne a la descripción para poder deshacerla.
        int ret = GestorEnvioFaltas.RET_ERROR_ENVIANDO;
        boolean exito = false;
        boolean errorProcesando = false;
        while (!exito && !errorProcesando && numIntentos < maxIntentos && !isCancelado()) {
            ret = getClienteSeneca().enviarFicheroFaltasSeneca(f, codigoOperacion, this);
            if (ret == GestorEnvioFaltas.RET_OK) {
                exito = true;
            } else if (ret == GestorEnvioFaltas.RET_ERROR_PROCESANDO) {
                errorProcesando = true;
            } else if (numIntentos < maxIntentos) {
                numIntentos++;
                firePropertyChange("message", null, "Error enviando fichero a Séneca. Intentándolo de nuevo [" + numIntentos + "]...");
            }
        }
        return ret;
    }

    private void setEnviarASeneca(boolean enviarASeneca) {
        this.enviarASeneca = enviarASeneca;
    }

    public ClienteSeneca getClienteSeneca() {
        return clienteSeneca;
    }

    public void setClienteSeneca(ClienteSeneca clienteSeneca) {
        this.clienteSeneca = clienteSeneca;
        setEnviarASeneca(clienteSeneca != null);

    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
        firePropertyChange("cancelled", false, true);
    }

    public boolean isFicherosGenerados() {
        return ficherosGenerados;
    }

    public void setFicherosGenerados(boolean ficherosGenerados) {
        this.ficherosGenerados = ficherosGenerados;
    }

    private boolean isCambiarDeFichero(GregorianCalendar f, GregorianCalendar fechaMax) {
        //vemos la diferencia entre fechas
        boolean cambiar = false;
        long dias = Fechas.getDiferenciaTiempoEn(f, fechaMax, GregorianCalendar.DATE);
        cambiar = dias > 1;
//        //Si hay dias de diferencia pero no son dos se cambia de fichero seguro.
//        if (dias != 1 && dias != 2) {
//            cambiar = true;
//        } else {
//            //Si son dos días puede que sea el fin de semana
//            if (f.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.FRIDAY && fechaMax.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.MONDAY) {
//                cambiar = false;
//            } else {
//                cambiar = true;
//            }
//        }
        return cambiar;
    }

    private boolean isFaltasExportadas() {
        return faltasExportadas;
    }

    private void setFaltasExportadas(boolean faltasExportadas) {
        this.faltasExportadas = faltasExportadas;
    }

    public GeneradorFicherosSeneca() {
    }

    public Document getDocumento() {
        if (documento == null) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                documento = documentBuilder.newDocument();
                raiz = getDocumento().createElement("SERVICIO");
                documento.appendChild(raiz);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(GeneradorFicherosSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return documento;
    }

    public void setDocumento(Document documento) {
        this.documento = documento;
    }

    public File getCarpetaSalida() {
        if (carpetaSalida == null) {
            if (MaimonidesApp.isJnlp()) {
                carpetaSalida=Configuracion.getSubCarpertaUsuarioMaimonides(Configuracion.CARPETA_SENECA);
                carpetaSalida=new File(carpetaSalida,"faltas");
            } else {
                carpetaSalida = new File(Configuracion.CARPETA_SENECA+"/faltas/");
            }
            carpetaSalida.mkdirs();

        }
        return carpetaSalida;
    }

    public File getCarpetaFallidos() {
        if (carpetaFallidos == null) {
            carpetaFallidos = new File(getCarpetaSalida(), "fallidos" + (isDebug() ? "_debug" : ""));
            carpetaFallidos.mkdirs();
        }
        return carpetaFallidos;
    }

    public File[] getFicherosExistentes() {
        return getCarpetaSalida().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
    }

    public File[] getFicherosFallidos() {
        return getCarpetaFallidos().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
    }

    public File[] getFicherosPropiedadesFallidos() {
        return getCarpetaFallidos().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".info");
            }
        });
    }

    public ArrayList<String> exportarFaltas() {
        return exportarFaltas(null, null);
    }

    public ArrayList<String> exportarFaltas(GregorianCalendar fechaDesde, GregorianCalendar fechaHasta) {
        setFicherosGenerados(false);
        getErrores().clear();
        if (MaimonidesApp.getApplication().getAnoEscolar() == null) {
            getErrores().add("No hay año escolar asignado.");
        } else {
            //Ponemos el tema de h.materia_id IS NOT NULL para que no introduzca la actvidaddes. Podría ponerse directamente un tipo de actividad.
            //No queremos que meta actividades porque séneca no admite faltas en las actividades sólo en las materias.
            //El envío de faltas hay que hacerlo siempre por cursos completos para una fecha por lo que primero tenemos que sacar fechas y cursos
            String sql = "SELECT distinct p.fecha,p.curso FROM partes_alumnos AS pa "
                    + " JOIN partes AS p ON p.id=pa.parte_id "
                    + " JOIN alumnos AS a ON a.id=pa.alumno_id "
                    + " LEFT JOIN alumnos_problemas_envio AS ap ON a.id=ap.alumno_id "
                    + " JOIN horarios AS h ON h.id=pa.horario_id "
                    + " JOIN tramos AS t ON t.id=h.tramo_id "
                    + " LEFT JOIN calendario_escolar AS ce ON ce.ano=p.ano AND p.fecha=ce.dia AND ce.docentes "
                    + //TODO Esta linea elimina a caso hecho a los alumnos "MIXTOS" ya que séneca da un error con ellos.
                    " JOIN unidades AS u ON a.unidad_id=u.id AND a.curso_id=u.curso_id "
                    + //Sólo hay que recuperar las que sean de actividad doncencia de alumnos con código de faltas y que no están borrados, y que no son festivos
                    " WHERE ce.id IS NULL AND (ap.id IS NULL || ap.excluir=0 ) AND  h.actividad_id=" + Actividad.getIdActividadDocencia(MaimonidesApp.getApplication().getAnoEscolar()) + " AND a.borrado=0 AND a.codFaltas!='' AND pa.asistencia>" + ParteFaltas.FALTA_ASISTENCIA + " AND p.enviado=0 AND p.ano=? AND (p.justificado OR p.fecha<? )"
                    + ((fechaDesde != null) ? " AND p.fecha>=? " : "")
                    + ((fechaHasta != null) ? " AND p.fecha<=? " : "")
                    + " ORDER BY p.fecha ASC,p.curso";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                int pos = 1;
                st.setInt(pos++, MaimonidesApp.getApplication().getAnoEscolar().getId());
                GregorianCalendar fechaFiltro = new GregorianCalendar();
                //Asignamos el principio de la semana siempre
                fechaFiltro.add(GregorianCalendar.DATE, fechaFiltro.get(GregorianCalendar.DAY_OF_WEEK) * -1);
                st.setDate(pos++, new java.sql.Date(fechaFiltro.getTime().getTime()));
                if (fechaDesde != null) {
                    st.setDate(pos++, new java.sql.Date(fechaDesde.getTime().getTime()));
                }
                if (fechaHasta != null) {
                    st.setDate(pos++, new java.sql.Date(fechaHasta.getTime().getTime()));
                }

                GregorianCalendar fechaMin = null;
                GregorianCalendar fechaMax = null;
                ArrayList<String> cursos = new ArrayList<String>();
                res = st.executeQuery();
                while (res.next() && !isCancelado()) {
                    GregorianCalendar f = Fechas.toGregorianCalendar(res.getDate("fecha"));
                    String c = res.getString("curso");
                    if (fechaMin == null) {
                        fechaMin = f;
                        fechaMax = f;
                    } else {
                        //vemos la diferencia entre fechas
                        boolean cambiarDeFichero = isCambiarDeFichero(f, fechaMax);
                        if (cambiarDeFichero) {
                            //Si es mayor de un dia hay que crear un fichero nuevo
                            getErrores().addAll(procesarFaltas(fechaMin, fechaMax, cursos));
                            fechaMin = f;
                            fechaMax = f;
                            cursos.clear();
                        } else {
                            //Si no asignamos ese dia simplemente
                            fechaMax = f;
                        }
                    }
                    if (!cursos.contains(c)) {
                        firePropertyChange("message", null, "Procesando " + Fechas.format(f) + " " + c + "...");
                        cursos.add(c);
                    }
                }
                if (cursos.size() > 0) {
                    //Y procesamos la última tanda
                    getErrores().addAll(procesarFaltas(fechaMin, fechaMax, cursos));
                }
            } catch (SQLException ex) {
                Logger.getLogger(GeneradorFicherosSeneca.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return getErrores();
    }

    public ArrayList<String> procesarFaltas(GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, ArrayList<String> cursos) {
        return exportarFaltas(fechaDesde, fechaHasta, cursos);
    }

    public ArrayList<String> exportarFaltas(GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, ArrayList<String> cursos) {
        ArrayList<String> err = new ArrayList<String>();
        setFaltasExportadas(false);
        String texto = "Procesando... ";
        if (Fechas.getDiferenciaTiempoEn(fechaDesde, fechaHasta, GregorianCalendar.DATE) == 0) {
            texto += Fechas.format(fechaHasta, "dd/MM");
        } else {
            texto += Fechas.format(fechaDesde, "dd/MM") + " a " + Fechas.format(fechaHasta, "dd/MM");
        }
        PreparedStatement st = null;
        try {
            firePropertyChange("message", null, "Recuperando datos...");
            StringBuilder sbCursos = new StringBuilder();
            boolean primero = true;
            for (String c : cursos) {
                if (primero) {
                    primero = false;
                } else {
                    sbCursos.append(",");
                }
                sbCursos.append("\"").append(c).append("\"");
            }
            String sql = "SELECT p.id as idParte,p.fecha,t.cod AS codTramo,pa.asistencia,a.id AS idAlumno FROM partes_alumnos AS pa "
                    + " JOIN partes AS p ON p.id=pa.parte_id "
                    + " JOIN alumnos AS a ON a.id=pa.alumno_id "
                    + " LEFT JOIN alumnos_problemas_envio AS ap ON a.id=ap.alumno_id "
                    + " JOIN horarios AS h ON h.id=pa.horario_id "
                    + " LEFT JOIN calendario_escolar AS ce ON ce.ano=p.ano AND p.fecha=ce.dia AND ce.docentes "
                    + //TODO Esta linea elimina a caso hecho a los alumnos "MIXTOS" ya que séneca da un error con ellos.
                    " JOIN unidades AS u ON a.unidad_id=u.id AND a.curso_id=u.curso_id "
                    + " JOIN tramos AS t ON t.id=h.tramo_id "
                    + //Sólo hay que recuperar las que sean de actividad doncencia de alumnos con código de faltas y que no están borrados, y que no son festivos
                    " WHERE ce.id IS NULL AND (ap.id IS NULL || ap.excluir=0 ) AND h.actividad_id=" + Actividad.getIdActividadDocencia(MaimonidesApp.getApplication().getAnoEscolar()) + " AND a.borrado=0 AND a.codFaltas!='' AND p.fecha BETWEEN ? AND ? AND p.curso IN (" + sbCursos.toString() + ") AND pa.asistencia > " + ParteFaltas.FALTA_ASISTENCIA
                    + " AND p.ano=? ORDER BY a.curso_id,a.unidad_id,idAlumno,p.fecha,asistencia,t.hora";
            System.out.println(sql);
            PreparedStatement actuParte = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes SET enviado=1, justificado=1 WHERE fecha BETWEEN ? AND ? AND curso IN (" + sbCursos.toString() + ") AND ano=?  ");
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setDate(1, new java.sql.Date(fechaDesde.getTime().getTime()));
            st.setDate(2, new java.sql.Date(fechaHasta.getTime().getTime()));
            st.setInt(3, MaimonidesApp.getApplication().getAnoEscolar().getId());
            actuParte.setDate(1, new java.sql.Date(fechaDesde.getTime().getTime()));
            actuParte.setDate(2, new java.sql.Date(fechaHasta.getTime().getTime()));
            actuParte.setInt(3, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            Curso ultimoCurso = null;
            Unidad ultimaUnidad = null;
            Alumno ultimoAlumno = null;
            Element nCursos = getDocumento().createElement("CURSOS");
            Element nCurso = null;
            Element nUnidades = null;
            Element nUnidad = null;
            Element nAlumnos = null;
            Element nAlumno = null;
            Element nFaltas = null;
            while (res.next() && !isCancelado()) {
                setFaltasExportadas(true);
                GregorianCalendar fecha = Fechas.toGregorianCalendar(res.getDate("fecha"));
                //firePropertyChange("message", null, "Procesando asistencia ...");
                //int idParte = res.getInt("idParte");
                int tramo = res.getInt("codTramo");
                int asistencia = res.getInt("asistencia");
                int idAlumno = res.getInt("idAlumno");
                Alumno a = Alumno.getAlumno(idAlumno);
                Curso c = Curso.getCurso(a.getIdCurso());
                Unidad u = a.getUnidad();
                firePropertyChange("message", null, texto + " " + u + " " + a + "...");
                if (ultimoCurso == null || !ultimoCurso.equals(c)) {
                    nCurso = getDocumento().createElement("CURSO");
                    nCursos.appendChild(nCurso);
                    //Creamos la linea <X_OFERTAMATRIG>2063</X_OFERTAMATRIG>
                    nCurso.appendChild(crearTag("X_OFERTAMATRIG", c.getCodigo()));
                    //Creamos la linea <D_OFERTAMATRIG>1º de Bachillerato (Humanidades y Ciencias Sociales)</D_OFERTAMATRIG>
                    nCurso.appendChild(crearTag("D_OFERTAMATRIG", c.getDescripcion()));
                    //Ahora creamos el comienzo de la unidad
                    nUnidades = getDocumento().createElement("UNIDADES");
                    nCurso.appendChild(nUnidades);
                }
                if (ultimaUnidad == null || !ultimaUnidad.equals(u)) {
                    //Creamos una nueva unidad
                    nUnidad = getDocumento().createElement("UNIDAD");
                    nUnidades.appendChild(nUnidad);
                    //Creamos la linea <X_UNIDAD>601648</X_UNIDAD>
                    nUnidad.appendChild(crearTag("X_UNIDAD", u.getCodigo()));
                    //Creamos la linea <T_NOMBRE>1BTO-A</T_NOMBRE>
                    nUnidad.appendChild(crearTag("T_NOMBRE", u.getCursoGrupo()));
                    //Ahora creamos el comienzo de la unidad
                    nAlumnos = getDocumento().createElement("ALUMNOS");
                    nUnidad.appendChild(nAlumnos);
                }
                if (ultimoAlumno == null || !ultimoAlumno.equals(a)) {
                    nAlumno = getDocumento().createElement("ALUMNO");
                    nAlumno.appendChild(crearTag("X_MATRICULA", a.getCodFaltas()));
                    //Y Creamos el inicio para las faltas de asistencia de este alumno
                    nFaltas = getDocumento().createElement("FALTAS_ASISTENCIA");
                    nAlumno.appendChild(nFaltas);
                    nAlumnos.appendChild(nAlumno);
                }
                addFalta(fecha, tramo, asistencia, nFaltas);
                ultimoCurso = c;
                ultimaUnidad = u;
                ultimoAlumno = a;
            }
            if (isFaltasExportadas() && !isCancelado()) {
                firePropertyChange("message", null, "Generando fichero...");
                generarCabeceraFaltas(fechaDesde, fechaHasta, nCursos);
                File f = generarFicheroFaltas(fechaDesde, fechaHasta, cursos);
                if (isEnviarASeneca()) {
                    firePropertyChange("message", null, "Enviando fichero a Séneca...");
                    String nombre = f.getName().substring(0, f.getName().length() - 4);
                    String codigoOperacion = Cripto.md5(nombre + "" + Math.random());
                    int ret = enviarFichero(f, codigoOperacion);
                    if (ret == GestorEnvioFaltas.RET_OK) {
                        firePropertyChange("message", null, "Fichero enviado correctamente. Marcando faltas como enviadas...");
                        actuParte.executeUpdate();
                        File enviados = new File(f.getParent(), "enviados");
                        enviados.mkdirs();
                        f.renameTo(new File(enviados, f.getName()));
                        firePropertyChange("message", null, "Envío de faltas terminado.");
                    } else {
                        if (ret == GestorEnvioFaltas.RET_ERROR_PROCESANDO) {
                            firePropertyChange("message", null, "Séneca ha dado errores procesando el fichero enviado.");
                            File nuevo = new File(getCarpetaFallidos(), nombre + "-ID" + codigoOperacion + ".xml");
                            f.renameTo(nuevo);
                            //Creamos el fichero de propiedades del nuevo
                            File info = new File(nuevo.getParentFile(), nuevo.getName() + ".info");
                            Properties p = new Properties();
                            p.setProperty("desde", fechaDesde.getTimeInMillis() + "");
                            p.setProperty("hasta", fechaHasta.getTimeInMillis() + "");
                            p.setProperty("cursos", Str.implode(cursos, ","));
                            p.setProperty("archivo", nuevo.getAbsolutePath());
                            p.setProperty("codigo", codigoOperacion);
                            p.setProperty("error", getClienteSeneca().getUltimoError());
                            FileOutputStream fos = new FileOutputStream(info);
                            p.store(fos, "Error enviando fichero de faltas");
                            Obj.cerrar(fos);
                            err.add("<html>Séneca ha dado errores procesando el fichero enviado:<br/> " + nombre + ".<br/><br/>");
                            if (!getClienteSeneca().getUltimoError().equals("")) {
                                err.add(getClienteSeneca().getUltimoError());
                            }
                        } else {
                            firePropertyChange("message", null, "No se ha podido enviar el fichero a Séneca.");
                            f.delete();
                            err.add("<html>No se ha podido enviar el fichero:<br/> " + nombre + ".<br/><br/>");
                            if (!getClienteSeneca().getUltimoError().equals("")) {
                                err.add(getClienteSeneca().getUltimoError());
                            }
                        }
                        //Si no se está loggeado cancelamos el proceso
                        if (!getClienteSeneca().isLoggeado()) {
                            setCancelado(true);
                        }
                    }
                } else {
                    firePropertyChange("message", null, "Marcando faltas como enviadas...");
                    actuParte.executeUpdate();
                }
            }
            Obj.cerrar(actuParte, st, res);
        } catch (Exception ex) {
            Logger.getLogger(GeneradorFicherosSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        return err;
    }

    public void enviarFicheroGeneralEnPorciones(EnvioErroneo envio) {
        try {

            //Tenemos que dividir el archivo por cursos y unidades
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            documentBuilderFactory.setValidating(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            File tmpX = new File(getCarpetaFallidos(), envio.getFicheroEnvio().getName() + ".tmp");
            Archivo.setContenido(Archivo.getContenido(envio.getFicheroEnvio(), "latin1"), "UTF-8", tmpX, false);
            Document doc = documentBuilder.parse(tmpX);
            tmpX.delete();
            //Ahore recorremos el documento generando un archivo nuevo por cada version
            Node nCursos = doc.getElementsByTagName("CURSOS").item(0);
            Node datosGenerales = doc.getElementsByTagName("DATOS_GENERALES").item(0);
            NodeList cursos = nCursos.getChildNodes();
            for (int i = 0; i < cursos.getLength(); i++) {
                Object obj = cursos.item(i);
                if (!(obj instanceof Element)) {
                    continue;
                }
                Element cursoActual = (Element) obj;
                //Creamos un nuevo documento
                Document nuevoDoc = documentBuilder.newDocument();
                //Le añadimos un nodo de servicio
                Node servicio = nuevoDoc.createElement("SERVICIO");
                nuevoDoc.appendChild(servicio);
                //A este le añadimos los datos generales (clonados)
                Node nuevoDatosGenerales = datosGenerales.cloneNode(true);
                nuevoDoc.adoptNode(nuevoDatosGenerales);
                servicio.appendChild(nuevoDatosGenerales);
                //Creamos un nodo de cursos y se lo añadimos
                Node nuevoCursos = nuevoDoc.createElement("CURSOS");
                servicio.appendChild(nuevoCursos);
                //Creamos un nodo de curso y se lo añadimos
                Node nuevoCurso = nuevoDoc.createElement("CURSO");
                nuevoCursos.appendChild(nuevoCurso);
                //Le añadimos los datos al curso
                Node el1 = cursoActual.getElementsByTagName("X_OFERTAMATRIG").item(0).cloneNode(true);
                nuevoDoc.adoptNode(el1);
                String nombreCurso = el1.getTextContent();
                Node el2 = cursoActual.getElementsByTagName("D_OFERTAMATRIG").item(0).cloneNode(true);
                nuevoDoc.adoptNode(el2);
                nuevoCurso.appendChild(el1);
                nuevoCurso.appendChild(el2);
                //Creamos un nodo de unidades y se lo añadimos
                Node nuevoUnidades = nuevoDoc.createElement("UNIDADES");
                nuevoCurso.appendChild(nuevoUnidades);
                //Ahora recorremos las unidades creando un documento nuevo por cada una
                Element nodoUnidadesActual = (Element) cursoActual.getElementsByTagName("UNIDADES").item(0);
                NodeList unidades = nodoUnidadesActual.getChildNodes();
                for (int x = 0; x < unidades.getLength(); x++) {
                    Node nObj = unidades.item(x);
                    if (!(nObj instanceof Element)) {
                        continue;
                    }
                    //Clonamos la unidad
                    Node unidad = nObj.cloneNode(true);
                    //Clonamos el documento completo
                    Document docActual = (Document) nuevoDoc.cloneNode(true);
                    //Adoptamos el nodo creado
                    docActual.adoptNode(unidad);
                    //Y se lo añadimos a las unidades
                    docActual.getElementsByTagName("UNIDADES").item(0).appendChild(unidad);
                    //Guardamos el documento y lo enviamos
                    String contenido = docToString(docActual);
                    File tmp = new File(getCarpetaFallidos(), "tmp_curso_" + nombreCurso + "_" + x + ".xml");
                    Archivo.setContenido(contenido, "latin1", tmp, false);
                    String codigoOperacion = Cripto.md5(tmp.getName() + "" + Math.random());
                    int ret = enviarFichero(tmp, codigoOperacion);
                    if (ret == GestorEnvioFaltas.RET_ERROR_PROCESANDO) {
                        tmp.delete();
                        //Sobre ese archivo tenemos que hacer envíos alumno por alumno
                        //Clonamos el documento
                        docActual = (Document) nuevoDoc.cloneNode(true);
                        //Y procesamos el envío
                        enviarFicheroUnidadEnPorciones(envio, docActual, (Element) unidad);
                    } else if (ret == GestorEnvioFaltas.RET_ERROR_ENVIANDO) {
                        envio.getFallidos().add(tmp);
                    } else {
                        tmp.delete();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GeneradorFicherosSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void enviarFicheroUnidadEnPorciones(EnvioErroneo envio, Document doc, Element unidad) throws TransformerException {
        //Creamos un nuevo nodo de unidad
        Element nuevaUnidad = doc.createElement("UNIDAD");
        doc.getElementsByTagName("UNIDADES").item(0).appendChild(nuevaUnidad);
        //Le añadimos los parámetros
        Node xUnidad = unidad.getElementsByTagName("X_UNIDAD").item(0).cloneNode(true);
        doc.adoptNode(xUnidad);
        String codUnidad = xUnidad.getTextContent();
        Node xNombre = unidad.getElementsByTagName("T_NOMBRE").item(0).cloneNode(true);
        doc.adoptNode(xNombre);
        nuevaUnidad.appendChild(xUnidad);
        nuevaUnidad.appendChild(xNombre);
        //Ahora añadimos los alumnos
        Element alumnos = doc.createElement("ALUMNOS");
        nuevaUnidad.appendChild(alumnos);
        //Y vamos creando un fichero por cada alumno
        NodeList nl = unidad.getElementsByTagName("ALUMNOS").item(0).getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Object obj = nl.item(i);
            if (!(obj instanceof Element)) {
                continue;
            }
            Element al = (Element) obj;
            al = (Element) al.cloneNode(true);
            //Clonamos el documento
            Document docAlumno = (Document) doc.cloneNode(true);
            docAlumno.adoptNode(al);
            //Y le añadimos el
            docAlumno.getElementsByTagName("ALUMNOS").item(0).appendChild(al);
            //Guardamos el archivo y lo enviamos
            String contenido = docToString(docAlumno);
            File tmp = new File(getCarpetaFallidos(), "tmp_unidad_" + codUnidad + "_" + i + ".xml");
            Archivo.setContenido(contenido, "latin1", tmp, false);
            String codigoOperacion = Cripto.md5(tmp.getName() + "" + Math.random());
            int ret = enviarFichero(tmp, codigoOperacion);
            if (ret == GestorEnvioFaltas.RET_ERROR_PROCESANDO) {
                envio.getErroneos().add(tmp);
                //Cogemos el dato X_MATRICULA
                String datoMatricula = al.getElementsByTagName("X_MATRICULA").item(0).getTextContent();
                Alumno a = Alumno.getAlumnoDesdeCodFaltas(datoMatricula);
                if (a != null) {
                    AlumnoEnvioErroneo aee = new AlumnoEnvioErroneo(a);
                    envio.getAlumnosFallidos().add(aee);
                    //Ahora tenemos que localizar las fechas distintas donde falla
                    NodeList nlf = al.getElementsByTagName("F_FALASI");
                    ArrayList<String> fechas = new ArrayList<String>();
                    for (int x = 0; x < nlf.getLength(); x++) {
                        String sFecha = nlf.item(x).getTextContent();
                        if (!fechas.contains(sFecha)) {
                            fechas.add(sFecha);
                        }
                    }
                    aee.setInfo(Str.implode(fechas, "; "));
                }
            } else if (ret == GestorEnvioFaltas.RET_ERROR_ENVIANDO) {
                envio.getFallidos().add(tmp);
            } else {
                tmp.delete();
            }

        }
    }

    private void addFalta(GregorianCalendar fecha, int tramo, int asistencia, Element nFaltas) throws DOMException {
        //Ahora añadimos la falta en cuestion
        Element falta = getDocumento().createElement("FALTA_ASISTENCIA");
        //<F_FALASI>06/10/2008</F_FALASI>
        falta.appendChild(crearTag("F_FALASI", Fechas.format(fecha)));
        //<X_TRAMO>506701</X_TRAMO>
        falta.appendChild(crearTag("X_TRAMO", tramo));
        //<C_TIPFAL>J</C_TIPFAL>
        falta.appendChild(crearTag("C_TIPFAL", getTipoAsistencia(asistencia)));
        //<L_DIACOM>N</L_DIACOM> El dia completo siempre es false
        falta.appendChild(crearTag("L_DIACOM", "N"));
        nFaltas.appendChild(falta);
    }

    private Element crearTag(String nombre, Object contenido) {
        Element tag = getDocumento().createElement(nombre);
        tag.appendChild(getDocumento().createTextNode(contenido.toString()));
        return tag;
    }

    private void generarCabeceraFaltas(GregorianCalendar fechaMin, GregorianCalendar fechaMax, Element nCursos) throws DOMException {
        //Ahora generamos la cabecera

        Element datosGenerales = getDocumento().createElement("DATOS_GENERALES");
        //<MODULO>FALTAS DE ASISTENCIA</MODULO>
        datosGenerales.appendChild(crearTag("MODULO", "FALTAS DE ASISTENCIA"));
        //<TIPO_INTERCAMBIO>I</TIPO_INTERCAMBIO>
        datosGenerales.appendChild(crearTag("TIPO_INTERCAMBIO", "I"));
        //<AUTOR>SENECA</AUTOR>
        datosGenerales.appendChild(crearTag("AUTOR", "MAIMONIDES"));
        //<FECHA>17/10/2008 14:11:47</FECHA>
        datosGenerales.appendChild(crearTag("FECHA", Fechas.format(new GregorianCalendar(), "dd/MM/yyyy HH:mm:ss")));
        //<C_ANNO>2008</C_ANNO>
        datosGenerales.appendChild(crearTag("C_ANNO", MaimonidesApp.getApplication().getAnoEscolar().getAno()));
        //<FECHA_DESDE>06/10/2008</FECHA_DESDE>
        datosGenerales.appendChild(crearTag("FECHA_DESDE", Fechas.format(fechaMin)));
        //<FECHA_HASTA>06/10/2008</FECHA_HASTA>
        datosGenerales.appendChild(crearTag("FECHA_HASTA", Fechas.format(fechaMax)));
        //<CODIGO_CENTRO>18002243</CODIGO_CENTRO>
        datosGenerales.appendChild(crearTag("CODIGO_CENTRO", MaimonidesApp.getApplication().getConfiguracion().get("codigo_centro", "")));
        //<NOMBRE_CENTRO>I.E.S. Federico García Lorca</NOMBRE_CENTRO>
        datosGenerales.appendChild(crearTag("NOMBRE_CENTRO", MaimonidesApp.getApplication().getConfiguracion().get("nombre_centro", "")));
        //<LOCALIDAD_CENTRO>Churriana de la Vega (Granada)</LOCALIDAD_CENTRO>
        datosGenerales.appendChild(crearTag("LOCALIDAD_CENTRO", MaimonidesApp.getApplication().getConfiguracion().get("poblacion_centro", "") + " (" + MaimonidesApp.getApplication().getConfiguracion().get("provincia_centro", "") + ")"));
        raiz.appendChild(datosGenerales);
        raiz.appendChild(nCursos);
        //getDocumento().appendChild(raiz);
    }
    Transformer trans = null;

    public Transformer getTransformer() {
        if (trans == null) {
            TransformerFactory transfac = TransformerFactory.newInstance();

//            if (tFact instanceof TransformerFactoryImpl) {
//                tFact.setAttribute(TransformerFactoryImpl.FEATURE_INCREMENTAL,
//                        Boolean.TRUE);
//            }
            try {
                trans = transfac.newTransformer();
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(GeneradorFicherosSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return trans;
    }

    private String docToString(Document doc) throws TransformerException {
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        getTransformer().transform(source, result);
        String contenido = sw.toString();
        Obj.cerrar(sw);
        source = null;
        result = null;
        return contenido;
    }

    private File generarFicheroFaltas(GregorianCalendar fechaMin, GregorianCalendar fechaMax, ArrayList<String> cursos) throws TransformerConfigurationException, IllegalArgumentException, TransformerFactoryConfigurationError, TransformerException {
        setFicherosGenerados(true);
        String contenido = docToString(getDocumento());
        String nombre = "";
        if (fechaMin.equals(fechaMax)) {
            nombre = "Faltas " + Fechas.format(fechaMin, "dd-MM-yyyy");
        } else {
            nombre = "Faltas desde " + Fechas.format(fechaMin, "dd-MM-yyyy") + " hasta " + Fechas.format(fechaMax, "dd-MM-yyyy");
        }
        StringBuilder sbCursos = new StringBuilder();
        boolean primero = true;
        for (String c : cursos) {
            if (primero) {
                primero = false;
            } else {
                sbCursos.append("-");
            }
            sbCursos.append(c);
        }
        nombre = nombre + " " + sbCursos.toString();
        sbCursos = null;
        File f = new File(getCarpetaSalida(), nombre + ".xml");
        Archivo.setContenido(contenido, "latin1", f, false);
        contenido = null;
        setDocumento(null);
        trans = null;
        System.gc();
        setFicherosGenerados(true);
        return f;
    }

    private String getTipoAsistencia(int asistencia) {
        String sAsistencia = "";
        switch (asistencia) {
            case ParteFaltas.FALTA_INJUSTIFICADA:
                sAsistencia = "I";
                break;
            case ParteFaltas.FALTA_EXPULSION://las expulsiones son faltas justificadas
            case ParteFaltas.FALTA_JUSTIFICADA:
                sAsistencia = "J";
                break;
            case ParteFaltas.FALTA_RETRASO:
                sAsistencia = "R";
                break;
        }
        return sAsistencia;
    }
}
