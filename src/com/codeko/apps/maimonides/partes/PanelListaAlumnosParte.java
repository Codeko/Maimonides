/*
 * PanelListaAlumnosParte.java
 *
 * Created on 1 de septiembre de 2008, 12:47
 */
package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.asistencia.ConfiguracionAsistencia;
import com.codeko.apps.maimonides.digitalizacion.MensajeDigitalizacion;
import com.codeko.apps.maimonides.digitalizacion.VisorImagenes;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author  Codeko
 */
public class PanelListaAlumnosParte extends javax.swing.JPanel {

    VisorImagenes visorImagen = new VisorImagenes();
    boolean visorMostrado = false;
    PanelListaAlumnosParte esto = this;
    AsistenciaAlumno objetoModelo = new AsistenciaAlumno();
    boolean mensajeAdvertenciaMostrado = false;
    boolean opcionMensajeAdvertencia = false;
    boolean usarControlMensajeAdvertencia = false;
    boolean permisosEdicion = true;
    PropertyChangeListener listenerAsistencia = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("asistenciaSeleccionada".equals(evt.getPropertyName())) {
                panelDetalleAsistencia1.setDatos((LineaParteAlumno) evt.getNewValue());
            }
        }
    };
    CodekoTableModel<AsistenciaAlumno> modelo = new CodekoTableModel<AsistenciaAlumno>(objetoModelo) {

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (!cbProcesado.isSelected() && !cbEnviado.isSelected()) {
                super.setValueAt(aValue, row, column);
            } else {
                StringBuilder msg = new StringBuilder();
                if (cbProcesado.isSelected()) {
                    msg.append("El parte ya ha sido procesado.\nDebe desmarcarlo como procesado para editarlo.");
                }
                if (cbEnviado.isSelected()) {
                    msg.append("El parte ya ha sido enviado a Séneca.\nDebe desmarcarlo como enviado para editarlo.");
                }
                MaimonidesApp.getMaimonidesView().getBeanControl().firePropertyChange("message", null, msg.toString());
            }
        }
    };
    ParteFaltas parte = null;

    public ParteFaltas getParte() {
        return parte;
    }

    public void setParte(ParteFaltas parte) {
        this.parte = parte;
        objetoModelo.setParte(parte);
        int cols = tabla.getColumnCount() - 1;
        boolean digit = false;
        boolean justi = false;
        boolean proce = false;
        boolean envi = false;
        if (parte != null) {
            lInfo.setText("[" + Str.lPad(parte.getId(), 6, '0') + "] " + parte.getDescripcionObjeto());
            for (int i = 0; i < 6; i++) {
                tabla.getColumnExt(cols - i).setTitle(parte.getCabeceras().get((parte.getCabeceras().size() - 1) - i));
                tabla.getColumnExt(cols - i).setToolTipText(parte.getCabecerasCompletas().get((parte.getCabecerasCompletas().size() - 1) - i));
            }
            digit = parte.isDigitalizado();
            justi = parte.isJustificado();
            proce = parte.isProcesado();
            envi = parte.isEnviado();
            setPendienteDigitalizar(!digit);
        } else {
            lInfo.setText("");
            for (int i = 0; i < 6; i++) {
                tabla.getColumnExt(cols - i).setTitle((i + 1) + "ª");
                tabla.getColumnExt(cols - i).setToolTipText((i + 1) + "ª Hora");
            }
            setPendienteDigitalizar(false);
        }
        cbDigitalizado.setSelected(digit);
        cbJustificado.setSelected(justi);
        cbProcesado.setSelected(proce);
        cbEnviado.setSelected(envi);
        setParteCargado(parte != null);
    }

    private boolean isDatosEditables() {
        return isParteCargado() && !cbEnviado.isSelected() && !cbProcesado.isSelected();
    }

    /** Creates new form PanelListaAlumnosParte */
    public PanelListaAlumnosParte() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Parte de faltas");
        tabla.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                String letra = "";
                if (e.getKeyChar() == '\u007f') {
                    letra = " ";
                } else {
                    letra = (e.getKeyChar() + "").toUpperCase();
                }
                if (!letra.equals("")) {
                    procesarLetra(letra);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                String letra = "";
                if (e.getKeyChar() == '\n') {
                    int tRow = tabla.getSelectedRow();
                    int row = tabla.convertRowIndexToModel(tRow);
                    int tCol = tabla.getSelectedColumn();
                    int col = tabla.convertColumnIndexToModel(tCol);
                    if (Num.getInt(modelo.getValueAt(row, col)) <= 0) {
                        letra = "AX";
                    }
                }
                if (!letra.equals("")) {
                    procesarLetra(letra);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            void procesarLetra(String letra) {
                int tRow = tabla.getSelectedRow();
                int row = tabla.convertRowIndexToModel(tRow);
                if (row > -1) {
                    int tCol = tabla.getSelectedColumn();
                    int col = tabla.convertColumnIndexToModel(tCol);
                    if (col <= 2) {
                        mensajeAdvertenciaMostrado = false;
                        opcionMensajeAdvertencia = false;
                        usarControlMensajeAdvertencia = true;
                        for (int i = col + 1; i <= modelo.getColumnCount(); i++) {
                            if (letra.equals("AX")) {
                                Object val = modelo.getValueAt(row, i);
                                if (Num.getInt(val) < ParteFaltas.FALTA_ASISTENCIA) {
                                    //modelo.setValueAt("A", row, i);
                                    setValorAsistencia(row, i, "A");
                                }
                            } else {
                                //modelo.setValueAt(letra, row, i);
                                setValorAsistencia(row, i, letra);
                            }
                        }
                        mensajeAdvertenciaMostrado = false;
                        opcionMensajeAdvertencia = false;
                        usarControlMensajeAdvertencia = false;
                        tabla.changeSelection(tRow, tCol, false, false);
                        tabla.getSelectionModel().setSelectionInterval(tRow, tRow);
                    } else {
                        if (letra.equals("AX")) {
                            letra = "A";
                        }
                        //modelo.setValueAt(letra, row, col);
                        setValorAsistencia(row, col, letra);
                        tabla.changeSelection(tRow, tCol, false, false);
                        tabla.getSelectionModel().setSelectionInterval(tRow, tRow);
                    }
                }
            }
        });

        AbstractHighlighter h = new AbstractHighlighter() {

            @Override
            protected Component doHighlight(Component c, ComponentAdapter adapt) {
                Object valor = adapt.getValue();

                if (valor instanceof Alumno) {
                    Alumno a = (Alumno) adapt.getValue();
                    boolean expulsado = a.isExpulsado(getParte().getFecha());
                    if (expulsado) {
                        if (adapt.isSelected() && isDatosEditables()) {
                            c.setForeground(Color.ORANGE);
                            c.setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
                        } else {
                            c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                            c.setBackground(Color.ORANGE);
                        }
                    } else {
                        if (adapt.isSelected() && isDatosEditables()) {
                            Color back = UIManager.getDefaults().getColor("Table.selectionBackground");
                            c.setBackground(back);
                            Color fore = UIManager.getDefaults().getColor("Table.selectionForeground");
                            c.setForeground(fore);
                        } else {
                            Color back = UIManager.getDefaults().getColor("Table.background");
                            c.setBackground(back);
                            Color fore = UIManager.getDefaults().getColor("Table.foreground");
                            c.setForeground(fore);
                        }
                    }
                } else if (valor instanceof Integer && Num.getInt(valor) < 10) {
                    if (!adapt.isSelected() || !isDatosEditables()) {
                        c.setBackground(ParteFaltas.getColorTipoFalta(valor));
                        c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                    }
                } else if (valor == null) {
                    c.setBackground(Color.LIGHT_GRAY);
                } else if (valor instanceof String) {
                    c.setBackground(Color.LIGHT_GRAY);
                }
                if (adapt.hasFocus() && isDatosEditables()) {
                    ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.red, 2));
                    c.setForeground(Color.red);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }

                return c;
            }
        };
        tabla.setHighlighters(h);
        tabla.getColumnExt("Código").setVisible(false);
        tabla.getColumnExt("Unidad").setVisible(false);
        tabla.getColumnExt("Alumno").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof Alumno) {
                    Alumno a = (Alumno) val;
                    setText(a.getNombreFormateado());
                } else {
                    setText(Str.noNulo(val));
                }
            }
        });

        TableColumnExt colNum = tabla.getColumnExt(0);
        colNum.setPreferredWidth(30);
        colNum.setMaxWidth(30);
        colNum.setMinWidth(30);
        tabla.setEditable(false);
        int pos = tabla.getColumnCount() - 1;
        for (int i = 0; i < 6; i++) {
            TableColumnExt col = tabla.getColumnExt(pos - i);
            col.setPreferredWidth(65);
            col.setMinWidth(65);
            col.setCellRenderer(new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        JLabel t = (JLabel) c;
                        t.setHorizontalAlignment(SwingConstants.CENTER);
                        t.setFont(t.getFont().deriveFont(Font.BOLD));
                        if (value instanceof String) {
                            t.setFont(t.getFont().deriveFont(9f));
                        }
                    }
                    return c;
                }

                @Override
                public void setValue(Object value) {
                    if (value == null) {
                        setBackground(Color.LIGHT_GRAY);
                        setText("");
                    } else if (value instanceof String) {
                        setText(value.toString());
                    } else {
                        int val = Num.getInt(value);
                        switch (val) {
                            case ParteFaltas.FALTA_ASISTENCIA:
                                setText("A");
                                break;
                            case ParteFaltas.FALTA_EXPULSION:
                                setText("E");
                                break;
                            case ParteFaltas.FALTA_JUSTIFICADA:
                                setText("J");
                                break;
                            case ParteFaltas.FALTA_INJUSTIFICADA:
                                setText("I");
                                break;
                            case ParteFaltas.FALTA_RETRASO:
                                setText("R");
                                break;
                            default:
                                setText("");
                        }
                    }
                    setToolTipText(getText());
                }
            });
        }
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    int col = tabla.columnAtPoint(e.getPoint());
                    if (row > -1) {
                        row = tabla.convertRowIndexToModel(row);
                        col = tabla.convertColumnIndexToModel(col);
                        AsistenciaAlumno justi = modelo.getElemento(row);
                        LineaParteAlumno lin = justi.getLineaHora(col - 3);
                        firePropertyChange("asistenciaSeleccionada", null, lin);
                    }
                }
            }
        });
        this.addPropertyChangeListener(listenerAsistencia);
        bLimpiarParte.setVisible(Permisos.especial(getClass(), "limpiarParte"));
        this.permisosEdicion = Permisos.edicion(getClass());
        bQuitarIndeterminados.setVisible(Permisos.especial(getClass(), "quitarIndeterminados"));
        boolean mostrar = Permisos.especial(getClass(), "estadoParte");
        cbDigitalizado.setVisible(mostrar);
        cbEnviado.setVisible(mostrar);
        cbJustificado.setVisible(mostrar);
        cbProcesado.setVisible(mostrar);
    }

    protected void setValorAsistencia(int row, int col, String asistencia) {
        AsistenciaAlumno as = modelo.getDatos().elementAt(row);
        boolean asignar = this.permisosEdicion;
        //Si no tiene permisos de edicion general vemos si puede editar este dato
        //en concreto
        //Ahora verificamos los permisos del usuario para modificar estos datos
        if (!asignar) {
            LineaParteAlumno l = as.getLineaHora(col - 3);
            if (l != null) {
                Profesor p = Permisos.getFiltroProfesor();
                if (p != null) {
                    //Si hay profesor vemos si la asistencia es para ese profesor
                    asignar = l.getHorario().getProfesor().equals(p.getId());
                }
                //Si por profesor no se puede asignar vemos si podemos asignarla
                //por unidad
                if (!asignar) {
                    Unidad u = Permisos.getFiltroUnidad();
                    if (u != null) {
                        asignar = l.getHorario().getUnidad().equals(u.getId());
                    }
                }
            }
        }
        if (!asignar) {
            MaimonidesApp.getMaimonidesView().getBeanControl().firePropertyChange("message", null, "No tiene permiso para modificar la asistencia de ese alumno.");
        }
        if (asignar && !asistencia.equals("E") && as.getAlumno().isExpulsado(as.getParte().getFecha())) {
            if (ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados() == ConfiguracionAsistencia.AAE_ADVERTENCIA) {
                if (!mensajeAdvertenciaMostrado || (!usarControlMensajeAdvertencia)) {
                    int op = JOptionPane.showConfirmDialog(this, "Se va a asignar asistencia a distinta de expulsión a un alumno expulsado.\n¿Continuar?", "Asignar asistencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (op != JOptionPane.YES_OPTION) {
                        asignar = false;
                    }
                    opcionMensajeAdvertencia = asignar;
                } else {
                    asignar = opcionMensajeAdvertencia;
                }
            } else if (ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados() == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA) {
                if (!mensajeAdvertenciaMostrado || (!usarControlMensajeAdvertencia)) {
                    JOptionPane.showMessageDialog(this, "No se permite asignar asistencia distinta de expulsado a un alumno expulsado", "Error asignando asistencia", JOptionPane.INFORMATION_MESSAGE);
                    mensajeAdvertenciaMostrado = true;
                }
                asignar = false;
            } else if (ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados() == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION) {
                asignar = false;
            }
        }
        if (asignar) {
            modelo.setValueAt(asistencia, row, col);
        }
    }

    public void limpiar() {
        modelo.vaciar();
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargar() {
        return new CargarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarTask extends org.jdesktop.application.Task<Object, Void> {

        CargarTask(org.jdesktop.application.Application app) {
            super(app);
            firePropertyChange("setIniciado", null, true);
            limpiar();
            setHayImagenParte(false);
            setMessage("Cargando datos de alumnos...");
        }

        @Override
        protected Object doInBackground() {
            modelo.addDatos(getParte().getAsistencia());
            setHayImagenParte(getParte().hayImagen());
            if (visorMostrado) {
                visorImagen.setImagen(null);
                setMessage("Cargando imagen...");
                visorImagen.setImagen(getParte().getImagen());
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            firePropertyChange("setTerminado", null, true);
            tabla.packAll();
        }
    }

    public void cargarAsistencia(Collection<AsistenciaAlumno> asistencia) {
        limpiar();
        modelo.addDatos(asistencia);
        tabla.packAll();
    }
    //TODO Implementar la carga por fechas

    public void cargar(ParteFaltas parte) {
        setParte(parte);
        MaimonidesUtil.ejecutarTask(this, "cargar");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        split = new javax.swing.JSplitPane();
        lInfo = new javax.swing.JLabel();
        bQuitarIndeterminados = new javax.swing.JButton();
        bVerParte = new javax.swing.JButton();
        bLimpiarParte = new javax.swing.JButton();
        panelGeneral = new javax.swing.JPanel();
        panelParte = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        leyendaPartes1 = new com.codeko.apps.maimonides.partes.LeyendaPartes();
        cbDigitalizado = new javax.swing.JCheckBox();
        cbProcesado = new javax.swing.JCheckBox();
        cbEnviado = new javax.swing.JCheckBox();
        cbJustificado = new javax.swing.JCheckBox();
        panelDetalleAsistencia1 = new com.codeko.apps.maimonides.partes.informes.alumnos.PanelDetalleAsistencia();
        bVerPanelVisor = new javax.swing.JButton();

        split.setResizeWeight(0.5);
        split.setName("split"); // NOI18N

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelListaAlumnosParte.class);
        lInfo.setText(resourceMap.getString("lInfo.text")); // NOI18N
        lInfo.setName("lInfo"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelListaAlumnosParte.class, this);
        bQuitarIndeterminados.setAction(actionMap.get("quitarIndeterminados")); // NOI18N
        bQuitarIndeterminados.setName("bQuitarIndeterminados"); // NOI18N

        bVerParte.setAction(actionMap.get("verParte")); // NOI18N
        bVerParte.setName("bVerParte"); // NOI18N

        bLimpiarParte.setAction(actionMap.get("limpiarParte")); // NOI18N
        bLimpiarParte.setName("bLimpiarParte"); // NOI18N

        panelGeneral.setName("panelGeneral"); // NOI18N
        panelGeneral.setLayout(new java.awt.BorderLayout());

        panelParte.setName("panelParte"); // NOI18N

        scrollTabla.setName("scrollTabla"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.setShowGrid(true);
        tabla.setSortable(false);
        scrollTabla.setViewportView(tabla);

        leyendaPartes1.setName("leyendaPartes1"); // NOI18N

        cbDigitalizado.setAction(actionMap.get("digitalizado")); // NOI18N
        cbDigitalizado.setName("cbDigitalizado"); // NOI18N

        cbProcesado.setAction(actionMap.get("procesado")); // NOI18N
        cbProcesado.setName("cbProcesado"); // NOI18N

        cbEnviado.setAction(actionMap.get("enviado")); // NOI18N
        cbEnviado.setName("cbEnviado"); // NOI18N

        cbJustificado.setAction(actionMap.get("justificado")); // NOI18N
        cbJustificado.setText(resourceMap.getString("cbJustificado.text")); // NOI18N
        cbJustificado.setName("cbJustificado"); // NOI18N

        javax.swing.GroupLayout panelParteLayout = new javax.swing.GroupLayout(panelParte);
        panelParte.setLayout(panelParteLayout);
        panelParteLayout.setHorizontalGroup(
            panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelParteLayout.createSequentialGroup()
                .addComponent(leyendaPartes1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbDigitalizado)
                    .addComponent(cbProcesado))
                .addGap(2, 2, 2)
                .addGroup(panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbJustificado)
                    .addComponent(cbEnviado))
                .addContainerGap())
            .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        panelParteLayout.setVerticalGroup(
            panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelParteLayout.createSequentialGroup()
                .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(leyendaPartes1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelParteLayout.createSequentialGroup()
                        .addGroup(panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbDigitalizado)
                            .addComponent(cbJustificado))
                        .addGap(19, 19, 19))
                    .addGroup(panelParteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbProcesado)
                        .addComponent(cbEnviado))))
        );

        panelGeneral.add(panelParte, java.awt.BorderLayout.CENTER);

        panelDetalleAsistencia1.setName("panelDetalleAsistencia1"); // NOI18N
        panelGeneral.add(panelDetalleAsistencia1, java.awt.BorderLayout.SOUTH);

        bVerPanelVisor.setAction(actionMap.get("mostrarVisorLateral")); // NOI18N
        bVerPanelVisor.setName("bVerPanelVisor"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelGeneral, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bLimpiarParte)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bQuitarIndeterminados)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bVerParte)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bVerPanelVisor)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lInfo)
                    .addComponent(bVerPanelVisor)
                    .addComponent(bVerParte)
                    .addComponent(bQuitarIndeterminados)
                    .addComponent(bLimpiarParte))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "parteCargado")
    public Task digitalizado() {
        return new ParametroParteTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), "digitalizado", cbDigitalizado.isSelected());
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "parteCargado")
    public Task justificado() {
        return new ParametroParteTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), "justificado", cbJustificado.isSelected());
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "parteCargado")
    public Task procesado() {
        return new ParametroParteTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), "procesado", cbProcesado.isSelected());
    }

    private class ParametroParteTask extends org.jdesktop.application.Task<Object, Void> {

        boolean valor = false;
        String campo = "";
        boolean borrarMensajes = false;
        //TODO No me gusta esta funcion ni su diseño

        ParametroParteTask(org.jdesktop.application.Application app, String campo, boolean valor) {
            super(app);
            this.campo = campo;
            this.valor = valor;
            if (campo.equals("digitalizado")) {
                boolean cont = true;
                if (getParte().isDigitalizado() && !valor && MensajeDigitalizacion.hayMensajesPendientes(getParte().getId())) {
                    //Preguntamos si desea eliminar las advertencias de digitalización si las hubiera
                    int op = JOptionPane.showConfirmDialog(esto, "El parte de asistencia tiene advertencias de digitalización pendientes de revisar.\nNo es conveniente que deje esos mensajes pendientes si va a volver a digitalizar el parte.\n¿Eliminar advertencias de digitalización pendientes?", "Mensajes pendientes", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (op) {
                        case JOptionPane.YES_OPTION:
                            borrarMensajes = true;
                            break;
                        case JOptionPane.NO_OPTION:
                            //No hacemos nada
                            break;
                        default:
                            cont = false;
                    }
                }
                if (!cont) {
                    setPendienteDigitalizar(valor);
                    cbDigitalizado.setSelected(!valor);
                    this.campo = "Ninguno";
                }
            }
        }

        @Override
        protected Object doInBackground() {

            if (getParte() != null) {
                boolean guardar = false;
                if (campo.equals("digitalizado")) {
                    if (borrarMensajes) {
                        MensajeDigitalizacion.borrarMensajesPendientes(getParte().getId());
                    }
                    if (getParte().isDigitalizado() != valor) {
                        getParte().setDigitalizado(valor);
                        setPendienteDigitalizar(!valor);
                        guardar = true;
                    }
                } else if (campo.equals("procesado")) {
                    getParte().setProcesado(valor);
                    guardar = true;
                } else if (campo.equals("justificado")) {
                    getParte().setJustificado(valor);
                    guardar = true;
                } else if (campo.equals("enviado")) {
                    getParte().setEnviado(valor);
                    guardar = true;
                }
                if (guardar) {
                    getParte().guardar();
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
    private boolean parteCargado = false;

    public boolean isParteCargado() {
        return parteCargado;
    }

    public void setParteCargado(boolean b) {
        boolean old = isParteCargado();
        this.parteCargado = b;
        firePropertyChange("parteCargado", old, isParteCargado());
    }
    private boolean pendienteDigitalizar = false;

    public boolean isPendienteDigitalizar() {
        return pendienteDigitalizar;
    }

    public void setPendienteDigitalizar(boolean b) {
        boolean old = isPendienteDigitalizar();
        this.pendienteDigitalizar = b;
        firePropertyChange("pendienteDigitalizar", old, isPendienteDigitalizar());
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task quitarIndeterminados() {
        return new QuitarIndeterminadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class QuitarIndeterminadosTask extends org.jdesktop.application.Task<Object, Void> {

        QuitarIndeterminadosTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            getParte().quitarIndeterminados();
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            getParte().resetearAsistencia();
            cargar(getParte());
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "parteCargado")
    public Task enviado() {
        return new ParametroParteTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), "enviado", cbEnviado.isSelected());
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "hayImagenParte")
    public Task verParte() {
        return new VerParteTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class VerParteTask extends org.jdesktop.application.Task<BufferedImage, Void> {

        VerParteTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected BufferedImage doInBackground() {
            return getParte().getImagen();
        }

        @Override
        protected void succeeded(BufferedImage result) {
            VisorImagenes.mostrarImagen(result).setTitle(getParte().getDescripcionObjeto() + " : " + Fechas.format(getParte().getFecha()));

        }
    }
    private boolean hayImagenParte = false;

    public boolean isHayImagenParte() {
        return hayImagenParte;
    }

    public void setHayImagenParte(boolean b) {
        boolean old = isHayImagenParte();
        this.hayImagenParte = b;
        firePropertyChange("hayImagenParte", old, isHayImagenParte());
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "parteCargado")
    public Task limpiarParte() {
        return new LimpiarParteTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class LimpiarParteTask extends org.jdesktop.application.Task<Object, Void> {

        boolean limpiar = false;

        LimpiarParteTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(esto, "¿Esta seguro de que desea eliminar todas las marcas de asistencia del parte?", "Limpiar parte", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            limpiar = op == JOptionPane.OK_OPTION;
        }

        @Override
        protected Object doInBackground() {
            if (limpiar) {
                getParte().limpiar();
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            if (limpiar) {
                getParte().resetearAsistencia();
                cargar(getParte());
            }
        }
    }

    public static void abrirEditorParte(ParteFaltas parte) {
        PanelListaAlumnosParte panel = new PanelListaAlumnosParte();
        JFrame dlg = new JFrame("Editar parte:" + parte.getDescripcionObjeto() + " (" + parte.getId() + ")");
        dlg.add(panel);
        dlg.validate();
        dlg.pack();
        dlg.setAlwaysOnTop(true);
        dlg.setName("visorPartesFlotante");
        MaimonidesApp.getApplication().show(dlg);
        panel.cargar(parte);
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task mostrarVisorLateral() {
        return new MostrarVisorLateralTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));

    }

    private class MostrarVisorLateralTask extends org.jdesktop.application.Task<BufferedImage, Void> {

        MostrarVisorLateralTask(org.jdesktop.application.Application app) {
            super(app);
            visorMostrado = !visorMostrado;
            if (!visorMostrado) {
                panelGeneral.remove(split);
                panelGeneral.add(panelParte, BorderLayout.CENTER);
            } else {
                panelGeneral.remove(panelParte);
                split.setLeftComponent(panelParte);
                split.setRightComponent(visorImagen);
                panelGeneral.add(split);
                split.setDividerLocation(0.5d);
            }
            panelGeneral.updateUI();
        }

        @Override
        protected BufferedImage doInBackground() {
            if (!visorMostrado) {
                return null;
            } else {
                return getParte().getImagen();
            }
        }

        @Override
        protected void succeeded(BufferedImage result) {
            visorImagen.setImagen(result);
            visorImagen.ajustar();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bLimpiarParte;
    private javax.swing.JButton bQuitarIndeterminados;
    private javax.swing.JButton bVerPanelVisor;
    private javax.swing.JButton bVerParte;
    private javax.swing.JCheckBox cbDigitalizado;
    private javax.swing.JCheckBox cbEnviado;
    private javax.swing.JCheckBox cbJustificado;
    private javax.swing.JCheckBox cbProcesado;
    private javax.swing.JLabel lInfo;
    private com.codeko.apps.maimonides.partes.LeyendaPartes leyendaPartes1;
    private com.codeko.apps.maimonides.partes.informes.alumnos.PanelDetalleAsistencia panelDetalleAsistencia1;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelParte;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JSplitPane split;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
