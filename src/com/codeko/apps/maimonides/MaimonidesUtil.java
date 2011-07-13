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


package com.codeko.apps.maimonides;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.impresion.JRModeloTablaDS;
import com.codeko.swing.tablas.MouseListenerOpcionesTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class MaimonidesUtil {

    /**
     * Recupera el campo código de una tabla cualquiera que tenga la estructura id,cod,ano
     * @param tabla Tabla a consultar
     * @param id ID del elemento que queremos recuperar su código
     * @return Código del elemento identificado por ID o -1 si no se encuentra o hay algún error
     */
    public static int getCodigoTabla(String tabla, int id) {
        int ret = -1;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT cod FROM " + tabla + " WHERE id=?");
            st.setInt(1, id);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(MaimonidesUtil.class.getName()).log(Level.SEVERE, "Error recuperando código de ID " + id + " en tabla " + tabla, ex);
        }
        return ret;
    }

    /**
     * Recupera el campo ID de una tabla cualquiera que tenga la estructura id,cod,ano
     * @param tabla Tabla a consultar
     * @param ano AnoEscolar del registro
     * @param codigo Código del registro
     * @return  ID del resgitro o -1 si no se encuentra o hay algún fallo
     */
    public static int getIdTabla(String tabla, AnoEscolar ano, int codigo) {
        int ret = -1;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM " + tabla + " WHERE ano=? AND cod=?");
            st.setInt(1, ano.getId());
            st.setInt(2, codigo);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(MaimonidesUtil.class.getName()).log(Level.SEVERE, "Error recuperando ID de COD " + codigo + " con año: " + ano + " en tabla " + tabla, ex);
        }
        return ret;
    }

    public static String getCodigoAbreviado(
            String nombre) {
        //Primero quitamos del nombre todo caracter simple o doble
        String tmpNombre = "";
        while (!(tmpNombre = nombre.replaceAll(" .{1,2} ", " ")).equals(nombre)) {
            nombre = tmpNombre;
        }
//Y todo bloque entre parentesis

        nombre = nombre.replaceAll("\\(.*\\)", "");
        //Sacamos el código con las primeras mayusculas
        boolean ultimoEsEspacio = true;
        String codigo = "";
        for (int i = 0; i
                < nombre.length() && codigo.length() < 2; i++) {
            char car = nombre.charAt(i);
            if (ultimoEsEspacio) {
                codigo += car;
            }

            ultimoEsEspacio = car == ' ';
        }
//Ahora si hay un sólo caracter le añadimos la segunda letra

        if (codigo.length() < 2) {
            for (int i = codigo.length(); i
                    < nombre.length() && codigo.length() < 2; i++) {
                char car = nombre.charAt(i);
                codigo +=
                        car;
            }

        }
        codigo = codigo.toUpperCase();
        return codigo;
    }

    public static boolean ejecutarTask(Object clase, String task) {
        try {
            Action a = getActionTask(clase, task);
            a.actionPerformed(new ActionEvent(MaimonidesApp.getApplication().getMainFrame(), 0, task));
            return true;
        } catch (Exception e) {
            Logger.getLogger(MaimonidesUtil.class.getName()).log(Level.WARNING, "Error ejecutando task '" + task + "' de la clase '" + clase + "' (" + clase.getClass().getName() + ")", e);
            return false;
        }
    }

    public static Action getActionTask(Object clase, String task) {
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(clase.getClass(), clase);
        return actionMap.get(task);
    }

    public static Action getActionTask(final Task t, String nombre) {
        AbstractAction a = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                MaimonidesApp.getApplication().getContext().getTaskService().execute(t);
            }
        };
        a.putValue(AbstractAction.SHORT_DESCRIPTION, nombre);
        a.putValue(AbstractAction.LONG_DESCRIPTION, nombre);
        a.putValue(AbstractAction.NAME, nombre);
        return a;
    }

    public static ResourceMap getResourceMap(
            Class aClass) {
        return org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(aClass);
    }

    public static JPopupMenu addMenuTabla(final JXTable tabla,
            String cabecera) {
        //TODO El uso de un sistema de impresión u otro así como las opciones de fuente y demás deberían ser configurables
        //Estaría además bien poder usar una plantilla base (creo que lo admite)
        MouseListenerOpcionesTabla a = new MouseListenerOpcionesTabla(tabla, false, "Maimónides: " + cabecera, cabecera, "Página {0} - " + Fechas.format(new GregorianCalendar())) {

            @Override
            public void accionImpresion(ActionEvent e) {
                try {
                    FastReportBuilder drb = new FastReportBuilder();
                    for (int i = 0; i < tabla.getModel().getColumnCount(); i++) {
                        int col = tabla.convertColumnIndexToView(i);
                        if (col > -1) {
                            TableColumnExt tcEx = tabla.getColumnExt(col);
                            String nombre = tcEx.getTitle();
                            int size = 5;
                            boolean fixed = false;
                            int max = 0;
                            String[] subNombre = nombre.split(" ");
                            for (String s : subNombre) {
                                if (s.length() > max) {
                                    max = s.length();
                                }
                            }
                            Class clase = tabla.getModel().getColumnClass(i);
                            if (clase == GregorianCalendar.class) {
                                max = 10;
                                size = 60;
                                fixed = true;
                            } else if (clase == String.class || clase == Integer.class) {
                                //Vemos el tamáño máximo de la columna
                                for (int x = 0; x < tabla.getModel().getRowCount(); x++) {
                                    String val = Str.noNulo(tabla.getModel().getValueAt(x, i));
                                    if (val.length() > max) {
                                        max = val.length();
                                    }
                                }
                                if (max < 11) {
                                    if (clase == Integer.class) {
                                        size = max * 6;
                                    } else {
                                        size = max * 9;
                                    }
                                    fixed = true;
                                }
                            } else {
                                TableCellRenderer tcr = tabla.getDefaultRenderer(clase);
                                if (tcr instanceof DefaultTableRenderer) {
                                    for (int x = 0; x < tabla.getModel().getRowCount(); x++) {
                                        String val = Str.noNulo(((DefaultTableRenderer) tabla.getCellRenderer(x, i)).getString(tabla.getModel().getValueAt(x, i)));
                                        if (val.length() > max) {
                                            max = val.length();
                                        }
                                    }
                                    if (max < 11) {
                                        size = max * 9;
                                        fixed = true;
                                    }
                                }
                            }
                            Style s = new Style();
                            s.setBorder(Border.THIN);
                            Font f = new Font(8, "Arial", false);
                            s.setFont(f);        
                            if (max > 40) {
                                f = new Font(7, "Arial", false);
                                s.setFont(f);
                            }
                            s.setVerticalAlign(VerticalAlign.TOP);
                            drb.addColumn(nombre, tabla.getModel().getColumnName(i), String.class, size, fixed, null, s, null);
                        }
                    }
                    drb.setTitle(getCabecera());
                    drb.setUseFullPageWidth(true);

                    drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT, 100, 20);
                    drb.addAutoText(AutoText.AUTOTEXT_CREATED_ON, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_LEFT, AutoText.PATTERN_DATE_DATE_TIME);

                    DynamicReport dr = drb.build();
                    JRDataSource ds = new JRModeloTablaDS(tabla.getModel());
                    JasperPrint jasperPrint = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
                    //TODO Aquí está tomando la configuración de la impresión de partes. Debería hacerlo de su propia configuración
                    if (MaimonidesApp.getApplication().getConfiguracion().isImprimirEnPDF()) {
                        File salida = File.createTempFile("tabla", ".pdf");
                        JasperExportManager.exportReportToPdfFile(jasperPrint, salida.getAbsolutePath());
                        Desktop.getDesktop().open(salida);
                    } else {
                        JasperPrintManager.printReport(jasperPrint, true);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MaimonidesUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        tabla.addMouseListener(a);
        JPopupMenu pm = a.getMenu();
        JMenuItem mCsv = new JMenuItem("Exportar", 'e');
        mCsv.setIcon(MaimonidesApp.getApplication().getContext().getResourceMap(MaimonidesUtil.class).getIcon("icono.excel"));
        pm.add(mCsv);
        mCsv.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            JFileChooser jfc = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo()) {

                                @Override
                                public void approveSelection() {
                                    File f = getSelectedFile();
                                    if (f.exists() && getDialogType() == SAVE_DIALOG) {
                                        int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                                                "El archivo seleccionado ya existe. "
                                                + "¿Desea sobreescribirlo?",
                                                "El archivo ya existe",
                                                JOptionPane.YES_NO_CANCEL_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
                                        switch (result) {
                                            case JOptionPane.YES_OPTION:
                                                super.approveSelection();
                                                return;
                                            case JOptionPane.NO_OPTION:
                                                return;
                                            case JOptionPane.CANCEL_OPTION:
                                                cancelSelection();
                                                return;
                                        }
                                    }
                                    super.approveSelection();
                                }
                            };

                            jfc.setFileFilter(new FileNameExtensionFilter("Ficheros CSV", "csv", "CSV"));

                            File f = null;
                            boolean cancelar = false;
                            while ((f == null || f.exists()) && !cancelar) {
                                int op = jfc.showSaveDialog(MaimonidesApp.getApplication().getMainFrame());
                                if (op == JFileChooser.APPROVE_OPTION) {
                                    f = jfc.getSelectedFile();
                                    if (!f.getName().toLowerCase().endsWith(".csv")) {
                                        f = new File(f.getParentFile(), f.getName() + ".csv");
                                    }
                                    if (f.exists()) {
                                        f.delete();
                                    }
                                } else {
                                    cancelar = true;
                                }
                            }
                            if (f != null && !f.exists() && !cancelar) {
                                MaimonidesApp.getApplication().setUltimoArchivo(f);
                                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                                for (int r = -1; r < tabla.getRowCount(); r++) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int c = 0; c < tabla.getColumnCount(); c++) {
                                        if (r >= 0) {
                                            if (c != 0) {
                                                sb.append(";");
                                            }
                                            sb.append("\"");
                                            Object valor = tabla.getValueAt(r, c);
                                            if (valor instanceof Boolean) {
                                                valor = (((Boolean) valor) ? "Si" : "No");
                                            } else if (valor instanceof Calendar || valor instanceof Date) {
                                                valor = Fechas.format(valor);
                                            }
                                            sb.append(Str.noNulo(valor).replace("\"", "\\\""));
                                            sb.append("\"");
                                        } else {
                                            if (c != 0) {
                                                sb.append(";");
                                            }
                                            sb.append("\"");
                                            sb.append(Str.noNulo(tabla.getColumn(c).getHeaderValue()).replace("\"", "\\\""));
                                            sb.append("\"");
                                        }

                                    }
                                    sb.append("\r\n");
                                    bw.write(sb.toString());
                                }
                                bw.close();
                                int opA = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "Tabla exportada correctamente en:\n" + f.getAbsolutePath(), "Exportación realizada", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Abrir archivo", "Aceptar"}, "Aceptar");
                                if (opA == JOptionPane.OK_OPTION) {
                                    Desktop.getDesktop().open(f);
                                }
                            }

                        } catch (IOException ex) {
                            Logger.getLogger(MaimonidesUtil.class.getName()).log(Level.WARNING, "Error exportando tabla", ex);
                            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se ha producido un error escribiendo en el fichero:\n" + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
        return pm;
    }

    @SuppressWarnings("unchecked")
    public static void mostrarVentanaListaDatos(String titulo, Collection datos) {
        if (datos.size() > 0) {
            if (datos.size() > 1) {
                JList l = new JList(datos.toArray());
                JScrollPane scroll = new JScrollPane(l);
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), scroll, titulo, JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), datos.toArray()[0], titulo, JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    public static int getCount(String tabla) {
        return getCount(tabla, "");
    }

    public static int getCount(String tabla, String extraWhere) {
        int ret = 0;
        String sql = "SELECT count(*) FROM " + tabla + " WHERE ano=? " + extraWhere;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MaimonidesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static void setFormatosFecha(JXDatePicker dp, boolean mostrarDiaSemana) {
        if (mostrarDiaSemana) {
            SimpleDateFormat[] formatos = new SimpleDateFormat[]{new SimpleDateFormat("EEEE dd/MM/yy"), new SimpleDateFormat("dd/MM/yy"), new SimpleDateFormat("EEEE dd-MM-yy"), new SimpleDateFormat("dd-MM-yy"), new SimpleDateFormat("ddMMyy")};
            dp.setFormats(formatos);
        } else {
            SimpleDateFormat[] formatos = new SimpleDateFormat[]{new SimpleDateFormat("dd/MM/yy"), new SimpleDateFormat("EEEE dd/MM/yy"), new SimpleDateFormat("EEEE dd-MM-yy"), new SimpleDateFormat("dd-MM-yy"), new SimpleDateFormat("ddMMyy")};
            dp.setFormats(formatos);
        }
    }

    public static int getDiaFecha(GregorianCalendar fecha) {
        int diaSemana = fecha.get(GregorianCalendar.DAY_OF_WEEK);
        int dia = -1;
        switch (diaSemana) {
            case Calendar.MONDAY:
                dia = 1;
                break;
            case Calendar.TUESDAY:
                dia = 2;
                break;
            case Calendar.WEDNESDAY:
                dia = 3;
                break;
            case Calendar.THURSDAY:
                dia = 4;
                break;
            case Calendar.FRIDAY:
                dia = 5;
                break;
        }
        return dia;
    }

    public static String getNombreDiaSemana(int dia, boolean extendido) {
        String txt = "";
        if (extendido) {
            switch (dia) {
                case 1:
                    txt = "Lunes";
                    break;
                case 2:
                    txt = "Martes";
                    break;
                case 3:
                    txt = "Miércoles";
                    break;
                case 4:
                    txt = "Jueves";
                    break;
                case 5:
                    txt = "Viernes";
                    break;
                case 6:
                    txt = "Sábado";
                    break;
                case 7:
                    txt = "Domingo";
                    break;
            }
        } else {
            switch (dia) {
                case 1:
                    txt = "Lun.";
                    break;
                case 2:
                    txt = "Mar.";
                    break;
                case 3:
                    txt = "Mié.";
                    break;
                case 4:
                    txt = "Jue.";
                    break;
                case 5:
                    txt = "Vie.";
                    break;
                case 6:
                    txt = "Sáb.";
                    break;
                case 7:
                    txt = "Dom.";
                    break;
            }
        }
        return txt;
    }

    public static void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    public static void expandAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public static boolean dialogoGuardarArchivo(String titulo,File archivo,String extension) {
        boolean ret = false;
        extension=extension.toLowerCase();
        JFileChooser jfc = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo()) {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                            "El archivo seleccionado ya existe. "
                            + "¿Desea sobreescribirlo?",
                            "El archivo ya existe",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        jfc.setDialogTitle(titulo);
        jfc.setFileFilter(new FileNameExtensionFilter("Ficheros "+(extension.toUpperCase()), extension, extension.toUpperCase()));

        File f = null;
        boolean cancelar = false;
        while ((f == null || f.exists()) && !cancelar) {
            int op = jfc.showSaveDialog(MaimonidesApp.getApplication().getMainFrame());
            if (op == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith("."+extension)) {
                    f = new File(f.getParentFile(), f.getName() + "."+extension);
                }
                if (f.exists()) {
                    f.delete();
                }
            } else {
                cancelar = true;
            }
        }
        if (f != null && !f.exists() && !cancelar) {
            MaimonidesApp.getApplication().setUltimoArchivo(f);
            ret=archivo.renameTo(f);
        }
        return ret;
    }
}
