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

package com.codeko.apps.maimonides.cartero;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.conf.PanelConfiguracionImpresion;
import com.codeko.apps.maimonides.conf.mail.ConfiguracionMail;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.IEmailable;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.impresion.informes.Informes;
import com.codeko.apps.maimonides.impresion.informes.custom.CustomInformes;
import com.codeko.apps.maimonides.partes.cartas.ConcatenadorPDF;
import com.codeko.apps.maimonides.partes.informes.asistencia.AsistenciaAlumno;
import com.codeko.apps.maimonides.partes.informes.asistencia.FiltroAsistenciaAlumno;
import com.codeko.util.OS;
import com.codeko.util.Str;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.mail.internet.InternetAddress;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.JobName;
import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateFactory;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.validator.EmailValidator;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

public class CarteroAlumno<T extends IAlumno> extends MaimonidesBean {

    OfficeDocumentConverter converter = null;
    ConfiguracionMail conf = new ConfiguracionMail("EMAIL");
    ArrayList<File> impresionesPendientes = new ArrayList<File>();
    Collection<T> elementos = null;
    Collection<T> elementosEnviados = new ArrayList<T>();
    boolean cargarAsistenciaTotal = false;
    String nombreNotificacion = "";
    int tipo = 0;//Tipo denotificacion
    GregorianCalendar fechaDesde = null;
    GregorianCalendar fechaHasta = null;

    public GregorianCalendar getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(GregorianCalendar fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public GregorianCalendar getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(GregorianCalendar fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getNombreNotificacion() {
        return nombreNotificacion;
    }

    public final void setNombreNotificacion(String nombreNotificacion) {
        this.nombreNotificacion = nombreNotificacion;
    }

    public Collection<T> getElementos() {
        return elementos;
    }

    public void setElementos(Collection<T> elementos) {
        this.elementos = elementos;
    }

    public Collection<T> getElementosEnviados() {
        return elementosEnviados;
    }

    public void setElementosEnviados(Collection<T> elementosEnviados) {
        this.elementosEnviados = elementosEnviados;
    }

    public ArrayList<File> getImpresionesPendientes() {
        return impresionesPendientes;
    }

    public String getNombrePlantilla() {
        String plantilla = null;
        switch (getTipo()) {
            case Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_GLOBAL:
                plantilla = "carta_perdida_evaluacion_continua.odt";
                break;
            case Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS:
                plantilla = "carta_perdida_evaluacion_continua_materias.odt";
                break;
            case Carta.TIPO_CARTA_EXPULSION:
                plantilla = "carta_expulsion.odt";
                break;
            case Carta.TIPO_CARTA_AVISO_FALTAS:
                plantilla = "carta_aviso_faltas.odt";
                break;
            case Carta.TIPO_CARTA_PARTE_CONVIVENCIA:
                plantilla = "carta_parte_convivencia.odt";
                break;
        }
        return plantilla;
    }

    public DocumentTemplate getTemplate() {
        DocumentTemplateFactory dtf = new DocumentTemplateFactory();
        DocumentTemplate template = null;
        try {
            String nombrePlantilla = getNombrePlantilla();
            //Primero vemos si existe en la carpeta de usuario
            File informesFolder = Configuracion.getSubCarpertaUsuarioMaimonides(Configuracion.CARPETA_INFORMES);
            File customVersion = new File(informesFolder, nombrePlantilla);
            if (customVersion.exists()) {
                try {
                    template = dtf.getTemplate(customVersion);
                } catch (IOException ex) {
                    Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (template == null) {
                //Buscamos en la carpeta de instalacion
                File plantilla = new File(Configuracion.CARPETA_INFORMES, nombrePlantilla);
                if (plantilla != null && plantilla.exists()) {
                    try {
                        template = dtf.getTemplate(plantilla);
                    } catch (IOException ex) {
                        Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            if (template == null) {
                //Buscamos en el los informes propios dentro del jar (por si se ha distribuido un jar con 
                //informes personalizados
                try {
                    InputStream plantilla = CustomInformes.class.getResourceAsStream(nombrePlantilla);//getClass().getResourceAsStream("/" + Configuracion.CARPETA_INFORMES + "/" + nombrePlantilla);
                    if (plantilla != null) {
                        template = dtf.getTemplate(plantilla);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (template == null) {
                //Finalmente cogemos los propios de la aplicacion
                try {
                    InputStream plantilla = Informes.class.getResourceAsStream(nombrePlantilla);//getClass().getResourceAsStream("/" + Configuracion.CARPETA_INFORMES + "/" + nombrePlantilla);
                    if (plantilla != null) {
                        template = dtf.getTemplate(plantilla);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (template == null) {
                Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, "No se ha podido encontrar la plantilla para {0}", getNombrePlantilla());
            }
        } catch (Exception e) {
            Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, "Error recuperando template para " + getNombrePlantilla(), e);
        }
        return template;
    }

    public String getPlantillaSMS() {
        return getPlantillaSMS(getTipo());
    }

    public static String getPlantillaSMS(int tipo) {
        String plantilla = "";
        switch (tipo) {
            case Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_GLOBAL:
                plantilla = MaimonidesApp.getApplication().getConfiguracion().get("psms_escolaridad_global", "Su #hijoHija# #nombreAlumno# ha perdido el derecho a la evaluación continua en todas las asignaturas.\n"
                        + "Para más información póngase en contacto con el tutor de su #hijoHija#.");
                break;
            case Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS:
                plantilla = MaimonidesApp.getApplication().getConfiguracion().get("psms_escolaridad_materias", "Su #hijoHija# #nombreAlumno# ha perdido el derecho a la evaluación continua en: #listaMaterias#\n"
                        + "Para más información póngase en contacto con el tutor de su #hijoHija#.");
                break;
            case Carta.TIPO_CARTA_EXPULSION:
                plantilla = MaimonidesApp.getApplication().getConfiguracion().get("psms_expulsion", "Su #hijoHija# #nombreAlumno# ha sido expulsado del centro desde #fechaInicio# a #fechaFin# (#numDias#días).\n"
                        + "Para más información póngase en contacto con el tutor de su #hijoHija#.");
                break;
            case Carta.TIPO_CARTA_AVISO_FALTAS:
                plantilla = MaimonidesApp.getApplication().getConfiguracion().get("psms_aviso_faltas", "Su #hijoHija# #nombreAlumno# ha tenido las siguiente faltas entre #fechaInicial# y #fechaFinal#: \n"
                        + "Justificadas: #faltasJustificadas#  Injustificadas:#faltasInjustificadas# Restrasos:#faltasRetrasos#.\n"
                        + "Para más información póngase en contacto con el tutor de su #hijoHija#.");
                break;
            case Carta.TIPO_CARTA_PARTE_CONVIVENCIA:
                plantilla = MaimonidesApp.getApplication().getConfiguracion().get("psms_convivencia_parte", "Su #hijoHija# #nombreAlumno# ha cometido el #fecha# "
                        + "la siguiente conducta #textoTipo#: #descripcion#.\n"
                        + "Para más información póngase en contacto con el tutor de su #hijoHija#.");
                break;
        }
        return plantilla;
    }

    public boolean isCargarAsistenciaTotal() {
        return cargarAsistenciaTotal;
    }

    public void setCargarAsistenciaTotal(boolean cargarAsistenciaTotal) {
        this.cargarAsistenciaTotal = cargarAsistenciaTotal;
    }

    public CarteroAlumno(String nombreNotificacion, int tipo) {
        setNombreNotificacion(nombreNotificacion);
        setTipo(tipo);
    }

    public int getTipo() {
        return tipo;
    }

    public final void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public boolean enviar(Collection<T> elementos) {
        setElementos(elementos);
        return enviar();
    }

    private boolean enviar() {
        DocumentTemplate docTemplate = getTemplate();
        if (getElementos() == null || docTemplate == null) {
            return false;
        }
        docTemplate = null;
        getElementosEnviados().clear();
        boolean ret = true;
        //TODO Mover a un punto de cargas generales de configuraciones
        String fOOO = Preferences.userNodeForPackage(PanelConfiguracionImpresion.class).get("office.home", System.getProperty("office.home"));
        if (fOOO == null) {
            System.getProperties().remove("office.home");
        } else {
            System.setProperty("office.home", fOOO);
        }

        try {
            DefaultOfficeManagerConfiguration OOConf = new DefaultOfficeManagerConfiguration();
            try {
                if (fOOO != null) {
                    OOConf.setOfficeHome(fOOO);
                }
            } catch (Exception e) {
                firePropertyChange("error", null, "No se ha podido iniciar la comunicación con OpenOffice.\nNo existe el directorio de OpenOffice/LibreOffice:\n" + fOOO + "\nRevise la configuración de impresión.");
                return false;
            }
            //TODO Sería bueno que esto fuese configurble
            //TODO Implementar soporte para libreoffice: bug: http://code.google.com/p/jodconverter/source/detail?r=201
            //OOConf.setConnectionProtocol(OfficeConnectionProtocol.PIPE);
            //OOConf.setPortNumber(8100);
            //ManagedProcessOfficeManagerConfiguration OOConf = new ManagedProcessOfficeManagerConfiguration(OfficeConnectionMode.socket(8100));
            OOConf.setTaskExecutionTimeout(1000 * 60 * 10);
            OfficeManager officeManager = OOConf.buildOfficeManager();//new ManagedProcessOfficeManager(OOConf);
            officeManager.start();
            converter = new OfficeDocumentConverter(officeManager);
            getImpresionesPendientes().clear();
            for (T a : getElementos()) {
                firePropertyChange("message", null, a.getAlumno().getNombreFormateado());
                firePropertyChange("progreso", null, a);
                ret = crearDocumento(a) && ret;
            }
            if (getImpresionesPendientes().size() > 0) {
                //TODO Si es un solo archivo no debería concatenarse
                //TODO Añadir el poder decidir por equipo que tipo de impresión usar
                File documentos = Configuracion.getSubCarpertaUsuarioMaimonides(Configuracion.CARPETA_DOCUMENTOS);
                File destino = new File(documentos, "Impresion-" + System.currentTimeMillis() + ".pdf");
                ConcatenadorPDF.concatenar(destino, getImpresionesPendientes());
                getImpresionesPendientes().clear();
                try {
                    if (MaimonidesApp.getApplication().getConfiguracion().isImprimirEnPDF()) {
                        firePropertyChange("message", null, "Abriendo PDF...");
                        Desktop.getDesktop().open(destino);
                    } else {
                        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                        if (services.length == 0) {
                            firePropertyChange("message", null, "No hay impresoras configuradas.");
                            firePropertyChange("error", null, "No hay impresoras configuradas.");
                            ret = false;
                        } else {
                            PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
                            printAttributes.add(Chromaticity.MONOCHROME); // print in mono
                            printAttributes.add(new JobName("Maimónides: " + getNombreNotificacion(), Locale.getDefault()));
                            PrintService service = ServiceUI.printDialog(null, 100, 100, services, null, null, printAttributes);
                            if (service != null) {
                                DocFlavor flavor = DocFlavor.URL.PDF;
                                if (!service.isDocFlavorSupported(flavor)) {
                                    Logger.getLogger(CarteroAlumno.class.getName()).info("Usando flavor autosense");
                                    flavor = DocFlavor.URL.AUTOSENSE;
                                }
                                DocPrintJob printerJob = service.createPrintJob();
                                HashDocAttributeSet docAtt = new HashDocAttributeSet();
                                /**
                                 * import javax.print.attribute.standard.ColorSupported;
                                import javax.print.attribute.standard.Copies;
                                import javax.print.attribute.standard.MediaName;
                                import javax.print.attribute.standard.OrientationRequested;
                                import javax.print.attribute.standard.PrintQuality;
                                import javax.print.attribute.standard.PrinterLocation;
                                
                                 */
                                //FileInputStream fis=new FileInputStream(destino);
                                SimpleDoc simpleDoc = new SimpleDoc(destino.toURI().toURL(), flavor, docAtt);
                                printerJob.print(simpleDoc, printAttributes);
                                //Obj.cerrar(fis);
                            } else {
                                firePropertyChange("message", null, "Impresión cancelada por el usuario.");
                                firePropertyChange("error", null, "Impresión cancelada por el usuario.");
                                ret = false;
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
                    firePropertyChange("error", null, "No se ha generado correctamente el PDF.");
                    ret = false;
                } catch (Exception e) {
                    Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, e);
                    firePropertyChange("error", null, "Ha habido algún error imprimiendo.\nSe imprimirá por la impresora estándar.\nError:\n" + e.getLocalizedMessage());
                    firePropertyChange("message", null, "Usando impresora por defecto.");
                    //Para el resto de excepciones asumimos que el comando print no ha funcionado
                    //Si no funciona probamos a imprimir desde consola
                    try {
                        ret = true;
                        if (!OS.isWindows()) {
                            //-P "printerName"
                            ProcessBuilder pb = new ProcessBuilder("lpr", destino.getAbsolutePath());
                            pb.start();
                        } else {
                            Desktop.getDesktop().print(destino);
                        }
                    } catch (Exception ex1) {
                        Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex1);
                        firePropertyChange("error", null, "Ha habido algún error imprimiendo:\n" + ex1.getLocalizedMessage());
                        ret = false;
                    }
                }

            }
            try {
                officeManager.stop();
            } catch (Exception ex) {
                Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            firePropertyChange("error", null, "No se ha podido iniciar la comunicación con OpenOffice:\n" + e.getLocalizedMessage() + "\nRevise la configuración de impresión.");
            ret = false;
        }
        return ret;
    }

    protected void addDatosExtra(Map<String, Object> data, T a, Carta carta) {
        //Para ser sobreescrito si es necesario
    }

    protected void addDatosCalculados(Map<String, Object> data, T a) {
        //Ahora los datos calculados
        Alumno al = a.getAlumno();
        data.put("localizador", System.currentTimeMillis() + "-" + al.getId());
        data.put("nombreTutor", al.getTutor().getNombreConDon());
        data.put("direccion", al.getDireccion());
        data.put("codigoPostal", al.getCp());
        data.put("poblacion", al.getPoblacion());
        data.put("nombreAlumno", al.getNombreApellidos());
        data.put("unidad", al.getUnidad().toString());
        data.put("hijoHija", al.getSexo().equals("M") ? "hija" : "hijo");
        if (isCargarAsistenciaTotal()) {
            AsistenciaAlumno at = AsistenciaAlumno.getAsistencia(al, getFechaDesde(), getFechaHasta(), this);
            data.put("totalFaltasInjustificadas", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA));
            data.put("totalFaltasJustificadas", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_JUSTIFICADA));
            data.put("totalFaltasRetrasos", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO));
            data.put("totalFaltasExpulsiones", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_EXPULSION));
        }
    }

    private boolean crearDocumento(T a) {
        boolean ret = true;
        try {
            Alumno al = a.getAlumno();
            Carta carta = new Carta();
            carta.setAlumno(al);
            Map<String, Object> data = getDatosBase();
            addDatosCalculados(data, a);
            addDatosExtra(data, a, carta);
            DocumentTemplate template = getTemplate();
            File archivo = new File(Configuracion.getSubCarpertaUsuarioMaimonides(Configuracion.CARPETA_DOCUMENTOS), data.get("localizador") + ".odt");
            template.createDocument(data, new FileOutputStream(archivo));
            boolean enviado = false;
            File pdf = new File(archivo.getParentFile(), archivo.getName().substring(0, archivo.getName().indexOf(".")) + ".pdf");
            converter.convert(archivo, pdf);
            carta.setLocalizador(data.get("localizador").toString());
            carta.setNombre(getNombreNotificacion());
            carta.setTipo(getTipo());

            if (al.isNotificar(Alumno.NOTIFICAR_SMS)) {
                //Entonces enviamos el sms
                boolean ok = enviarSMS(data, a, carta, false);
                enviado = enviado || ok;
                if (ok) {
                    carta.addModo(Alumno.NOTIFICAR_SMS);
                    carta.addParametro(a.getAlumno().getSms());
                }
            }

            if (al.isNotificar(Alumno.NOTIFICAR_TELEFONO)) {
                //Entonces enviamos el sms
                boolean ok = enviarSMS(data, a, carta, true);
                enviado = enviado || ok;
                if (ok) {
                    carta.addModo(Alumno.NOTIFICAR_TELEFONO);
                    carta.addParametro(a.getAlumno().getSms());
                }
            }

            if (al.isNotificar(Alumno.NOTIFICAR_EMAIL)) {
                carta.setArchivo(pdf);
                if (EmailValidator.getInstance().isValid(al.getEmail()) && EmailValidator.getInstance().isValid(MaimonidesApp.getApplication().getConfiguracion().get("email_centro", ""))) {
                    boolean ok = enviarEmail(pdf, a, carta);
                    enviado = enviado || ok;
                    if (ok) {
                        carta.addModo(Alumno.NOTIFICAR_EMAIL);
                        carta.addParametro(a.getAlumno().getEmail());
                    }
                }
            }
            //Si tiene puesto que se le envíe impreso o no se le ha enviado por correo.
            if (al.isNotificar(Alumno.NOTIFICAR_IMPRESO) || !enviado) {
                carta.setArchivo(pdf);
                //Desktop.getDesktop().print(archivo);
                getImpresionesPendientes().add(pdf);
                carta.addModo(Alumno.NOTIFICAR_IMPRESO);
                enviado = true;
            }
            carta.guardar();
            ret = enviado;
        } catch (Exception ex) {
            Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
            firePropertyChange("error", null, "Ha habido algún error generando el documento:\n" + ex.getLocalizedMessage());
            ret = false;
        }
        if (ret) {
            getElementosEnviados().add(a);
        }
        return ret;
    }

    private boolean enviarSMS(Map<String, Object> data, T a, Carta carta, boolean voz) {
        boolean ret = false;
        //Primero recuperamos la plantilla
        String plantilla = getPlantillaSMS();
        //Ahora recorremos los datos y sustituimos las variables
        Iterator<String> vars = data.keySet().iterator();
        while (vars.hasNext()) {
            String var = vars.next();
            plantilla = plantilla.replace("#" + var + "#", Str.noNulo(data.get(var)));
        }
        //Ahora realizamos el envio
        if (SMS.enviarSMS(a.getAlumno().getSms(), plantilla, voz)) {
            //Una vez que hemos sustituido todas las variables le añadimos el SMS a la descripción de la carta
            carta.setDescripcion(carta.getDescripcion() + (voz ? "VOZ" : "SMS") + ":\n" + plantilla + "\n\n");
            ret = true;
        } else {
            firePropertyChange("error", null, "Ha habido algún error enviado la notificación por SMS a " + a.getAlumno().getSms());
        }
        return ret;
    }

    private String getTextoEmail(T a) {
        return a.getAlumno().getTutor().getNombreConDon() + ".\nLe adjuntamos a continuación la notificación de " + getNombreNotificacion() + ".";
    }

    private boolean enviarEmail(File documento, T a, Carta carta) {
        boolean enviado = true;
        String texto = getTextoEmail(a) + conf.getPie();
        try {
            EmailAttachment attachment = new EmailAttachment();
            attachment.setPath(documento.getAbsolutePath());
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setDescription("Notificación de " + getNombreNotificacion() + ".");
            attachment.setName("Notificación.pdf");
            MultiPartEmail email = new MultiPartEmail();
            try {
                conf.aplicarConfiguracion(email);
                if (MaimonidesApp.isDebug()) {
                    email.addTo("maimonides@codeko.com", a.getAlumno().getTutor().getNombreConDon());
                } else {
                    email.addTo(a.getAlumno().getEmail(), a.getAlumno().getTutor().getNombreConDon());
                }
                ArrayList<InternetAddress> replyTo = new ArrayList<InternetAddress>(1);
                replyTo.add(new InternetAddress(MaimonidesApp.getApplication().getConfiguracion().get("email_centro", ""), MaimonidesApp.getApplication().getConfiguracion().get("nombre_centro", ""), "utf-8"));
                email.setReplyTo(replyTo);
                email.setSubject("Notificación de " + getNombreNotificacion() + " de su " + (a.getAlumno().getSexo().equals("M") ? "hija" : "hijo") + " " + a.getAlumno().getNombreApellidos());
                email.setMsg(texto);
                email.attach(attachment);
                email.send();
            } catch (EmailException ex) {
                Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
                enviado = false;
                firePropertyChange("error", null, "No se ha podido enviar el email a '" + a.getAlumno().getEmail() + "' :\n" + ex.getLocalizedMessage());
            }
        } catch (Exception e) {
            enviado = false;
            firePropertyChange("error", null, "No se ha podido enviar el email a '" + a.getAlumno().getEmail() + "' :\n" + e.getLocalizedMessage());
        }
        if (enviado) {
            carta.setDescripcion(carta.getDescripcion() + "Email:\n" + texto + "\n\n");
        }
        return enviado;
    }

    public static boolean enviarEmail(String titulo, String texto, Collection<File> adjuntos, Collection<IEmailable> destinatarios) throws EmailException {
        boolean enviado = true;
        ConfiguracionMail conf = new ConfiguracionMail("EMAIL");
        MultiPartEmail email = new MultiPartEmail();
        if (adjuntos != null) {
            for (File f : adjuntos) {
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(f.getAbsolutePath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setName(f.getName());
                email.attach(attachment);
            }
        }
        conf.aplicarConfiguracion(email);
        texto += conf.getPie();
        ArrayList<Carta> cartas = new ArrayList<Carta>();
        if (MaimonidesApp.isDebug()) {
            email.addTo("maimonides@codeko.com");
        } else {
            for (IEmailable ie : destinatarios) {
                email.addBcc(ie.getEmail(), ie.getNombreEmail());
                if (ie instanceof Alumno) {
                    Carta carta = new Carta();
                    carta.setAlumno((Alumno) ie);
                    carta.addModo(Alumno.NOTIFICAR_EMAIL);
                    carta.addParametro(ie.getEmail());
                    carta.setLocalizador(System.currentTimeMillis() + "-" + ((Alumno) ie).getId());
                    carta.setNombre("notificación manual");
                    carta.setTipo(Carta.TIPO_CARTA_NOTIFICACION_MANUAL);
                    cartas.add(carta);
                    carta.setDescripcion("Email " + titulo + ":\n" + texto);
                    cartas.add(carta);
                }
            }
        }
        ArrayList<InternetAddress> replyTo = new ArrayList<InternetAddress>(1);
        try {
            replyTo.add(new InternetAddress(MaimonidesApp.getApplication().getConfiguracion().get("email_centro", ""), MaimonidesApp.getApplication().getConfiguracion().get("nombre_centro", ""), "utf-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CarteroAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }
        email.setReplyTo(replyTo);
        email.setSubject(titulo);
        email.setMsg(texto);
        email.send();
        if (enviado) {
            for (Carta c : cartas) {
                c.guardar();
            }
        }
        return enviado;
    }

    private Map<String, Object> getDatosBase() {
        return Configuracion.getDatosBaseImpresion();
    }
}
