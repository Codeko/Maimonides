
/*
 * PanelListaPartesConvivencia.java
 *
 * Created on 25-ago-2009, 14:41:35
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.seneca.operaciones.convivencia.GestorConvivenciaSeneca;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.apps.maimonides.usr.Rol;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.estructuras.Par;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter.Entry;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.sort.RowFilters.GeneralFilter;
import org.jdesktop.swingx.table.TableColumnExt;

public class PanelListaPartesConvivencia extends javax.swing.JPanel implements IPanel {

    CodekoAutoTableModel<ParteConvivencia> modelo = new CodekoAutoTableModel<ParteConvivencia>(ParteConvivencia.class);
    Alumno alumno = null;
    PanelListaPartesConvivencia auto = this;
    //FiltroExpulsiones filtro = new FiltroExpulsiones();
    boolean modoFichaAlumno = false;
    DefaultListModel modeloLista = new DefaultListModel();
    boolean panelesInicializados = false;
    Expulsion expulsionActual = null;
    GeneralFilter filter = new GeneralFilter() {

        @Override
        protected boolean include(Entry<? extends Object, ? extends Object> value, int index) {
            boolean ret = true;
            if (expulsionActual != null) {
                ret=false;
                ParteConvivencia p = modelo.getElemento(Num.getInt(value.getIdentifier()));
                if (p.getExpulsionID() != null && expulsionActual.getId().equals(p.getExpulsionID())) {
                    ret = true;
                }
            }
            return ret;
        }
    };

    public boolean isModoFichaAlumno() {
        return modoFichaAlumno;
    }

    public void setModoFichaAlumno(boolean modoFichaAlumno) {
        this.modoFichaAlumno = modoFichaAlumno;
        TableColumnExt tc = tabla.getColumnExt("Alumno");
        tc.setVisible(!modoFichaAlumno);
        tc = tabla.getColumnExt("Situación");
        tc.setVisible(!modoFichaAlumno);
        panelFiltro.setVisible(!modoFichaAlumno);
    }

    public void setModoNotificaciones(boolean modoNotificaciones) {
        cbPadres1.setSelected(modoNotificaciones);
    }

    /** Creates new form PanelListaPartesConvivencia */
    public PanelListaPartesConvivencia() {
        initComponents();
        listaInfo.setModel(modeloLista);
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                setLineaSeleccionada(tabla.getSelectedRow() > -1);
                modeloLista.removeAllElements();
                //Ahora vemos si la linea tiene errores
                int row = tabla.getSelectedRow();
                if (row > -1) {
                    row = tabla.convertRowIndexToModel(row);
                    ParteConvivencia p = modelo.getElemento(row);
                    for (String s : p.validarParte()) {
                        modeloLista.addElement(s);
                    }
                }
            }
        });
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        MaimonidesUtil.addMenuTabla(tabla, "Listado de partes de convivencia");
        TableColumnExt tc = tabla.getColumnExt("Estado");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                int i = Num.getInt(val);
                String texto = ParteConvivencia.getTextoEstado(i);
                setText(texto);
            }
        });
        tc = tabla.getColumnExt("Notificados");
        tc.setVisible(false);
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                int i = Num.getInt(val);
                String texto = ParteConvivencia.getTextoNotificados(i);
                setText(texto);
            }
        });
        tc = tabla.getColumnExt("Enviable a Séneca");
        tc.setVisible(false);
        tc = tabla.getColumnExt("Situación");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                int i = Num.getInt(val);
                String texto = ParteConvivencia.getTextoSituacion(i);
                setText(texto);
            }
        });
        tc = tabla.getColumnExt("Gravedad");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                int i = Num.getInt(val);
                String texto = TipoConducta.getNombreGravedad(i);
                setText(texto);
            }
        });
        tc.setVisible(false);
        tc = tabla.getColumnExt("Fecha");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object value) {
                if (value instanceof GregorianCalendar) {
                    setText(Fechas.format((GregorianCalendar) value));
                }
            }
        });
        tabla.getColumnExt("Tramo Horario").setVisible(false);
        tabla.getColumnExt("Profesor").setVisible(false);
        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabla.getSelectedRow();
                if (row > -1) {
                    auto.firePropertyChange("parteSeleccionado", auto, modelo.getElemento(tabla.convertRowIndexToModel(row)));
                }
            }
        });
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowFilter(filter);
        ColorHighlighter coGrave = new ColorHighlighter(new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component arg0, org.jdesktop.swingx.decorator.ComponentAdapter arg1) {
                ParteConvivencia res = modelo.getElemento(tabla.convertRowIndexToModel(arg1.row));
                return res.getTipo() == TipoConducta.GRAVEDAD_GRAVE;
            }
        }, Color.decode("#FFCC99"), Color.BLACK);
        ColorHighlighter coLeve = new ColorHighlighter(new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component arg0, org.jdesktop.swingx.decorator.ComponentAdapter arg1) {
                ParteConvivencia res = modelo.getElemento(tabla.convertRowIndexToModel(arg1.row));
                return res.getTipo() == TipoConducta.GRAVEDAD_LEVE;
            }
        }, Color.decode("#FFFFCC"), Color.BLACK);
        tabla.addHighlighter(coGrave);
        tabla.addHighlighter(coLeve);
        cbTipoParte.addItem("Todos");
        cbTipoParte.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_LEVE));
        cbTipoParte.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_GRAVE));
        cbEstado.addItem("Todos");
        Par<String, Integer> estado = new Par<String, Integer>(ParteConvivencia.getTextoEstado(ParteConvivencia.ESTADO_IGNORADO), ParteConvivencia.ESTADO_IGNORADO) {

            @Override
            public String toString() {
                return getA();
            }
        };
        cbEstado.addItem(estado);
        estado = new Par<String, Integer>(ParteConvivencia.getTextoEstado(ParteConvivencia.ESTADO_PENDIENTE), ParteConvivencia.ESTADO_PENDIENTE) {

            @Override
            public String toString() {
                return getA();
            }
        };
        cbEstado.addItem(estado);
        estado = new Par<String, Integer>(ParteConvivencia.getTextoEstado(ParteConvivencia.ESTADO_SANCIONADO), ParteConvivencia.ESTADO_SANCIONADO) {

            @Override
            public String toString() {
                return getA();
            }
        };

        cbEstado.addItem(estado);
        MaimonidesUtil.setFormatosFecha(tfFechaDesde, false);
        MaimonidesUtil.setFormatosFecha(tfFechaHasta, false);
        if (Permisos.isUsuarioSoloProfesor()) {
            Unidad u = Permisos.getFiltroUnidad();
            if (u != null) {
                cbCursos1.setUnidad(u);
                cbCursos1.setEnabled(false);
                cbGrupos1.setEnabled(false);
            }
            //Si no es tutor sólo el dejamos acceder a sus partes
            if (!Permisos.isRol(Rol.ROL_TUTOR)) {
                Profesor p = Permisos.getFiltroProfesor();
                cbProfesores.setSelectedItem(p);
                cbProfesores.setEnabled(false);
            }
        }
        bEnviarSeneca.setVisible(Permisos.especial(getClass(), "enviarSeneca"));
        bEnviarTodos.setVisible(Permisos.especial(getClass(), "enviarSeneca"));
        panelInfo.setPreferredSize(new Dimension(0, 0));
    }

    public void setFiltroPartes(Expulsion exp) {
        expulsionActual = exp;
        tabla.setRowFilter(filter);
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        if (this.alumno == null && isModoFichaAlumno()) {
            modelo.vaciar();
        } else {
            MaimonidesUtil.ejecutarTask(auto, "actualizar");
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    void addParte(ParteConvivencia parteConvivencia) {
        modelo.addDato(parteConvivencia);
    }

    void quitarParte(ParteConvivencia parteConvivencia) {
        modelo.quitarDato(parteConvivencia);
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<ParteConvivencia>, Void> {

        GregorianCalendar fechaDesde = null;
        GregorianCalendar fechaHasta = null;
        Profesor profesor = null;
        Integer estado = null;
        Curso curso = null;
        Unidad unidad = null;
        Integer tipo = null;
        Integer situacion = 0;
        Integer noSituacion = 0;
        Integer notificado = 0;
        Integer noNotificado = 0;
        ArrayList<Conducta> conductas = null;

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setMessage("Cargando partes...");
            try {
                tfFechaDesde.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(PanelListaPartesConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                tfFechaHasta.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(PanelListaPartesConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            fechaDesde = Fechas.toGregorianCalendar(tfFechaDesde.getDate());
            fechaHasta = Fechas.toGregorianCalendar(tfFechaHasta.getDate());
            profesor = cbProfesores.getProfesor();
            unidad = cbGrupos1.getUnidad();
            curso = cbCursos1.getCurso();
            if (cbTipoParte.getSelectedIndex() > 0) {
                tipo = cbTipoParte.getSelectedIndex();
            }
            Object oEstado = cbEstado.getSelectedItem();
            if (oEstado instanceof Par) {
                estado = Num.getInt(((Par) oEstado).getB());
            }
            situacion = (cbSitTutor.isSelected() ? ParteConvivencia.SIT_REVISADO_TUTOR : 0) | (cbSitJE.isSelected() ? ParteConvivencia.SIT_REVISADO_JE : 0) | (cbSitSeneca.isSelected() ? ParteConvivencia.SIT_ENVIADO_SENECA : 0);
            if (situacion == 0) {
                situacion = null;
            }
            noSituacion = (cbSitTutor1.isSelected() ? ParteConvivencia.SIT_REVISADO_TUTOR : 0) | (cbSitJE1.isSelected() ? ParteConvivencia.SIT_REVISADO_JE : 0) | (cbSitSeneca1.isSelected() ? ParteConvivencia.SIT_ENVIADO_SENECA : 0);
            if (noSituacion == 0) {
                noSituacion = null;
            }
            notificado = (cbAlumno.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_ALUMNO : 0) | (cbTutor.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_TUTOR : 0) | (cbPadres.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_PADRES : 0);
            if (notificado == 0) {
                notificado = null;
            }
            noNotificado = (cbAlumno1.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_ALUMNO : 0) | (cbTutor1.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_TUTOR : 0) | (cbPadres1.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_PADRES : 0);
            if (noNotificado == 0) {
                noNotificado = null;
            }
            conductas = panelConductasParte1.getConductas();
        }

        @Override
        protected ArrayList<ParteConvivencia> doInBackground() {
            ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
            if ((getAlumno() != null && getAlumno().getId() != null) || !isModoFichaAlumno()) {
                partes = ParteConvivencia.getPartes(getAlumno(), fechaDesde, fechaHasta, profesor, curso, unidad, tipo, estado, situacion, noSituacion, notificado, noNotificado, conductas);
            }
            return partes;
        }

        @Override
        protected void succeeded(ArrayList<ParteConvivencia> result) {
            modelo.addDatos(result);
            setMessage("Partes de convivencia cargados correctamente.");
            tabla.packAll();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bEnviarSeneca = new javax.swing.JButton();
        bEnviarTodos = new javax.swing.JButton();
        bNotificarSeleccionados = new javax.swing.JButton();
        bNotificarTodas = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        panelFiltro = new javax.swing.JPanel();
        panelFiltroA = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfFechaDesde = new org.jdesktop.swingx.JXDatePicker();
        jLabel3 = new javax.swing.JLabel();
        cbTipoParte = new javax.swing.JComboBox();
        tfFechaHasta = new org.jdesktop.swingx.JXDatePicker();
        jLabel1 = new javax.swing.JLabel();
        lProfesor = new javax.swing.JLabel();
        cbProfesores = new com.codeko.apps.maimonides.profesores.CbProfesores();
        jLabel4 = new javax.swing.JLabel();
        cbEstado = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cbSitTutor = new javax.swing.JCheckBox();
        cbSitJE = new javax.swing.JCheckBox();
        cbSitSeneca = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        cbCursos1 = new com.codeko.apps.maimonides.cursos.CbCursos();
        cbGrupos1 = new com.codeko.apps.maimonides.cursos.CbGrupos();
        jLabel7 = new javax.swing.JLabel();
        cbSitTutor1 = new javax.swing.JCheckBox();
        cbSitJE1 = new javax.swing.JCheckBox();
        cbSitSeneca1 = new javax.swing.JCheckBox();
        lNotificado = new javax.swing.JLabel();
        cbTutor = new javax.swing.JCheckBox();
        cbPadres = new javax.swing.JCheckBox();
        cbAlumno = new javax.swing.JCheckBox();
        lNoNotificado = new javax.swing.JLabel();
        cbTutor1 = new javax.swing.JCheckBox();
        cbPadres1 = new javax.swing.JCheckBox();
        cbAlumno1 = new javax.swing.JCheckBox();
        panelConductasParte1 = new com.codeko.apps.maimonides.convivencia.PanelConductasParte();
        split = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        panelInfo = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listaInfo = new javax.swing.JList();

        setName("Form"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        setLayout(new java.awt.BorderLayout());

        bHerramientas.setFloatable(false);
        bHerramientas.setRollover(true);
        bHerramientas.setName("bHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelListaPartesConvivencia.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(bActualizar);

        bEnviarSeneca.setAction(actionMap.get("enviarSenecaSeleccionados")); // NOI18N
        bEnviarSeneca.setFocusable(false);
        bEnviarSeneca.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarSeneca.setName("bEnviarSeneca"); // NOI18N
        bEnviarSeneca.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(bEnviarSeneca);

        bEnviarTodos.setAction(actionMap.get("enviarSenecaTodos")); // NOI18N
        bEnviarTodos.setFocusable(false);
        bEnviarTodos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarTodos.setName("bEnviarTodos"); // NOI18N
        bEnviarTodos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(bEnviarTodos);

        bNotificarSeleccionados.setAction(actionMap.get("notificarSeleccionados")); // NOI18N
        bNotificarSeleccionados.setFocusable(false);
        bNotificarSeleccionados.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNotificarSeleccionados.setName("bNotificarSeleccionados"); // NOI18N
        bNotificarSeleccionados.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(bNotificarSeleccionados);

        bNotificarTodas.setAction(actionMap.get("notificarTodas")); // NOI18N
        bNotificarTodas.setFocusable(false);
        bNotificarTodas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNotificarTodas.setName("bNotificarTodas"); // NOI18N
        bNotificarTodas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(bNotificarTodas);

        add(bHerramientas, java.awt.BorderLayout.PAGE_START);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        panelFiltro.setName("panelFiltro"); // NOI18N
        panelFiltro.setLayout(new java.awt.BorderLayout(5, 5));

        panelFiltroA.setName("panelFiltroA"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelListaPartesConvivencia.class);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        tfFechaDesde.setName("tfFechaDesde"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        cbTipoParte.setName("cbTipoParte"); // NOI18N

        tfFechaHasta.setName("tfFechaHasta"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        lProfesor.setText(resourceMap.getString("lProfesor.text")); // NOI18N
        lProfesor.setName("lProfesor"); // NOI18N

        cbProfesores.setName("cbProfesores"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        cbEstado.setName("cbEstado"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        cbSitTutor.setText(resourceMap.getString("cbSitTutor.text")); // NOI18N
        cbSitTutor.setName("cbSitTutor"); // NOI18N

        cbSitJE.setText(resourceMap.getString("cbSitJE.text")); // NOI18N
        cbSitJE.setName("cbSitJE"); // NOI18N

        cbSitSeneca.setText(resourceMap.getString("cbSitSeneca.text")); // NOI18N
        cbSitSeneca.setName("cbSitSeneca"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        cbCursos1.setComboGrupos(cbGrupos1);
        cbCursos1.setMaximumSize(new java.awt.Dimension(200, 100));
        cbCursos1.setName("cbCursos1"); // NOI18N

        cbGrupos1.setName("cbGrupos1"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        cbSitTutor1.setText(resourceMap.getString("cbSitTutor1.text")); // NOI18N
        cbSitTutor1.setName("cbSitTutor1"); // NOI18N

        cbSitJE1.setText(resourceMap.getString("cbSitJE1.text")); // NOI18N
        cbSitJE1.setName("cbSitJE1"); // NOI18N

        cbSitSeneca1.setText(resourceMap.getString("cbSitSeneca1.text")); // NOI18N
        cbSitSeneca1.setName("cbSitSeneca1"); // NOI18N

        lNotificado.setText(resourceMap.getString("lNotificado.text")); // NOI18N
        lNotificado.setName("lNotificado"); // NOI18N

        cbTutor.setText(resourceMap.getString("cbTutor.text")); // NOI18N
        cbTutor.setName("cbTutor"); // NOI18N

        cbPadres.setText(resourceMap.getString("cbPadres.text")); // NOI18N
        cbPadres.setName("cbPadres"); // NOI18N

        cbAlumno.setText(resourceMap.getString("cbAlumno.text")); // NOI18N
        cbAlumno.setName("cbAlumno"); // NOI18N

        lNoNotificado.setText(resourceMap.getString("lNoNotificado.text")); // NOI18N
        lNoNotificado.setName("lNoNotificado"); // NOI18N

        cbTutor1.setText(resourceMap.getString("cbTutor1.text")); // NOI18N
        cbTutor1.setName("cbTutor1"); // NOI18N

        cbPadres1.setText(resourceMap.getString("cbPadres1.text")); // NOI18N
        cbPadres1.setName("cbPadres1"); // NOI18N

        cbAlumno1.setText(resourceMap.getString("cbAlumno1.text")); // NOI18N
        cbAlumno1.setName("cbAlumno1"); // NOI18N

        javax.swing.GroupLayout panelFiltroALayout = new javax.swing.GroupLayout(panelFiltroA);
        panelFiltroA.setLayout(panelFiltroALayout);
        panelFiltroALayout.setHorizontalGroup(
            panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroALayout.createSequentialGroup()
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFiltroALayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel5)
                                .addComponent(lProfesor)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cbCursos1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(panelFiltroALayout.createSequentialGroup()
                                    .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(cbTipoParte, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(tfFechaDesde, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel1))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tfFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbGrupos1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(cbProfesores, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelFiltroALayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelFiltroALayout.createSequentialGroup()
                                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(lNotificado))
                                .addGap(18, 18, 18)
                                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbSitTutor)
                                    .addComponent(cbSitTutor1)
                                    .addComponent(cbTutor)))
                            .addGroup(panelFiltroALayout.createSequentialGroup()
                                .addComponent(lNoNotificado)
                                .addGap(18, 18, 18)
                                .addComponent(cbTutor1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbPadres1)
                            .addComponent(cbSitJE)
                            .addComponent(cbSitJE1)
                            .addComponent(cbPadres))
                        .addGap(18, 18, 18)
                        .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbAlumno1)
                            .addComponent(cbAlumno)
                            .addComponent(cbSitSeneca)
                            .addComponent(cbSitSeneca1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelFiltroALayout.setVerticalGroup(
            panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroALayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbCursos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbGrupos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(tfFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbTipoParte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(cbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProfesor)
                    .addComponent(cbProfesores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbSitTutor)
                    .addComponent(cbSitSeneca)
                    .addComponent(cbSitJE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbSitTutor1)
                    .addComponent(cbSitSeneca1)
                    .addComponent(cbSitJE1)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lNotificado)
                    .addComponent(cbTutor)
                    .addComponent(cbPadres)
                    .addComponent(cbAlumno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lNoNotificado)
                    .addComponent(cbAlumno1)
                    .addComponent(cbPadres1)
                    .addComponent(cbTutor1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelFiltro.add(panelFiltroA, java.awt.BorderLayout.WEST);

        panelConductasParte1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelConductasParte1.border.title"))); // NOI18N
        panelConductasParte1.setName("panelConductasParte1"); // NOI18N
        panelFiltro.add(panelConductasParte1, java.awt.BorderLayout.CENTER);

        jPanel1.add(panelFiltro, java.awt.BorderLayout.NORTH);

        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(1.0);
        split.setName("split"); // NOI18N
        split.setOneTouchExpandable(true);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabla);

        split.setLeftComponent(jScrollPane1);

        panelInfo.setName("panelInfo"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        listaInfo.setName("listaInfo"); // NOI18N
        jScrollPane2.setViewportView(listaInfo);

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, Short.MAX_VALUE)
        );

        split.setRightComponent(panelInfo);

        jPanel1.add(split, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void tablaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaMouseClicked
        if (!isModoFichaAlumno() && evt.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(evt)) {
            //tenemos que ver la fila donde se ha hecho clic
            Point p = evt.getPoint();
            int row = tabla.rowAtPoint(p);
            if (row > -1) {
                row = tabla.convertRowIndexToModel(row);
                ParteConvivencia drc = modelo.getElemento(row);
                if (drc != null) {
                    MaimonidesApp.getMaimonidesView().mostrarFichaAlumno(drc.getAlumno());
                }
            }
        }
    }//GEN-LAST:event_tablaMouseClicked

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        if (!panelesInicializados) {
            panelesInicializados = true;
            split.setDividerLocation(1.0d);
        }
    }//GEN-LAST:event_formAncestorAdded
    private boolean lineaSeleccionada = false;

    public boolean isLineaSeleccionada() {
        return lineaSeleccionada;
    }

    public void setLineaSeleccionada(boolean b) {
        boolean old = isLineaSeleccionada();
        this.lineaSeleccionada = b;
        firePropertyChange("lineaSeleccionada", old, isLineaSeleccionada());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bEnviarSeneca;
    private javax.swing.JButton bEnviarTodos;
    private javax.swing.JToolBar bHerramientas;
    private javax.swing.JButton bNotificarSeleccionados;
    private javax.swing.JButton bNotificarTodas;
    private javax.swing.JCheckBox cbAlumno;
    private javax.swing.JCheckBox cbAlumno1;
    private com.codeko.apps.maimonides.cursos.CbCursos cbCursos1;
    private javax.swing.JComboBox cbEstado;
    private com.codeko.apps.maimonides.cursos.CbGrupos cbGrupos1;
    private javax.swing.JCheckBox cbPadres;
    private javax.swing.JCheckBox cbPadres1;
    private com.codeko.apps.maimonides.profesores.CbProfesores cbProfesores;
    private javax.swing.JCheckBox cbSitJE;
    private javax.swing.JCheckBox cbSitJE1;
    private javax.swing.JCheckBox cbSitSeneca;
    private javax.swing.JCheckBox cbSitSeneca1;
    private javax.swing.JCheckBox cbSitTutor;
    private javax.swing.JCheckBox cbSitTutor1;
    private javax.swing.JComboBox cbTipoParte;
    private javax.swing.JCheckBox cbTutor;
    private javax.swing.JCheckBox cbTutor1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lNoNotificado;
    private javax.swing.JLabel lNotificado;
    private javax.swing.JLabel lProfesor;
    private javax.swing.JList listaInfo;
    private com.codeko.apps.maimonides.convivencia.PanelConductasParte panelConductasParte1;
    private javax.swing.JPanel panelFiltro;
    private javax.swing.JPanel panelFiltroA;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JSplitPane split;
    private org.jdesktop.swingx.JXTable tabla;
    private org.jdesktop.swingx.JXDatePicker tfFechaDesde;
    private org.jdesktop.swingx.JXDatePicker tfFechaHasta;
    // End of variables declaration//GEN-END:variables

    /*class FiltroExpulsiones extends Filter {
    
    private ArrayList<Integer> toPrevious;
    private Expulsion expulsion = null;
    
    @Override
    public int getSize() {
    return toPrevious.size();
    }
    
    @Override
    protected void init() {
    toPrevious = new ArrayList<Integer>();
    }
    
    @Override
    protected void reset() {
    toPrevious.clear();
    int inputSize = getInputSize();
    fromPrevious = new int[inputSize]; // fromPrevious is inherited protected
    for (int i = 0; i < inputSize; i++) {
    fromPrevious[i] = -1;
    }
    }
    
    @Override
    protected void filter() {
    int inputSize = getInputSize();
    int current = 0;
    for (int i = 0; i < inputSize; i++) {
    if (expulsion == null || test(i)) {
    toPrevious.add(new Integer(i));
    // generate inverse map entry while we are here
    fromPrevious[i] = current++;
    }
    }
    }
    
    protected boolean test(int row) {
    boolean ret = false;
    ParteConvivencia p = modelo.getElemento(row);
    if (p.getExpulsionID() != null && expulsion != null && expulsion.getId().equals(p.getExpulsionID())) {
    ret = true;
    }
    return ret;
    }
    
    public void setExpulsion(Expulsion e) {
    this.expulsion = e;
    refresh();
    }
    
    @Override
    protected int mapTowardModel(int row) {
    return toPrevious.get(row);
    }
    }*/
    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "lineaSeleccionada")
    public Task enviarSenecaSeleccionados() {
        ArrayList<ParteConvivencia> seleccionados = new ArrayList<ParteConvivencia>();
        int[] rows = tabla.getSelectedRows();
        for (int r : rows) {
            r = tabla.convertRowIndexToModel(r);
            ParteConvivencia p = modelo.getElemento(r);
            seleccionados.add(p);
        }
        return getTaskEnvio(seleccionados);
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task enviarSenecaTodos() {
        return getTaskEnvio(new ArrayList<ParteConvivencia>(modelo.getDatos()));
    }

    private Task getTaskEnvio(Collection<ParteConvivencia> seleccionados) {
        //Primero quitamos todos los que no se pueden enviar o ya han sido
        //Enviados
        ArrayList<ParteConvivencia> eliminados = new ArrayList<ParteConvivencia>();
        for (ParteConvivencia p : seleccionados) {
            if (p.isEnviadoSeneca() || !p.isEnviableSeneca()) {
                eliminados.add(p);
            }
        }
        seleccionados.removeAll(eliminados);

        boolean enviarIgnorados = ParteConvivencia.isEnviarSenecaPartesIgnorados();
        //Si está configurado que no se envíen los partes ignorados tenemos que
        //ver si hay de estos en la selección
        if (!enviarIgnorados) {
            ArrayList<ParteConvivencia> ignorados = new ArrayList<ParteConvivencia>();
            for (ParteConvivencia p : seleccionados) {
                if (p.getEstado() == ParteConvivencia.ESTADO_IGNORADO) {
                    ignorados.add(p);
                }
            }
            if (ignorados.size() > 0) {
                int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Ha seleccionado " + ignorados.size() + " partes ignorados para enviar a Séneca.\nMaimónides está configurado para no enviar los partes marcados como ignorados.\n¿Desea enviar estos partes de todas formas?", "¿Enviar partes ignorados?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (op == JOptionPane.NO_OPTION) {
                    //Eliminamos los partes ignorados
                    seleccionados.removeAll(ignorados);
                } else if (op != JOptionPane.YES_OPTION) {
                    //Si la opción no es mantener los partes es que es cancelar
                    //o que se ha cerrado la ventana con lo que anulamos
                    return null;
                }
            }
        }
        //Ahora vemos si se está intentado enviar partes pendientes
        ArrayList<ParteConvivencia> pendientes = new ArrayList<ParteConvivencia>();
        for (ParteConvivencia p : seleccionados) {
            if (p.getEstado() == ParteConvivencia.ESTADO_PENDIENTE) {
                pendientes.add(p);
            }
        }
        if (pendientes.size() > 0) {
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Ha seleccionado " + pendientes.size() + " partes 'pendientes' para enviar a Séneca.\nSi envía estos partes luego tendrá que actualizarlos manualmente \nen Séneca una vez asignadas las medidas disciplinarias.\n¿Desea enviar estos partes de todas formas?", "¿Enviar partes pendientes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.NO_OPTION) {
                //Eliminamos los partes ignorados
                seleccionados.removeAll(pendientes);
            } else if (op != JOptionPane.YES_OPTION) {
                //Si la opción no es mantener los partes es que es cancelar
                //o que se ha cerrado la ventana con lo que anulamos
                return null;
            }
        }

        if (seleccionados.size() > 0) {
            return GestorConvivenciaSeneca.getTaskEnvioPartes(seleccionados);
        } else {
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Todos los partes seleccionados ya han sido enviados a Séneca o tienen errores que impiden su envío.", "No hay partes que enviar", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "lineaSeleccionada")
    public Task notificarSeleccionados() {
        ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
        for (int i : tabla.getSelectedRows()) {
            i = tabla.convertRowIndexToModel(i);
            ParteConvivencia p = modelo.getElemento(i);
            partes.add(p);
        }
        return new NotificarPartesDeConvivenciaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), partes);
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task notificarTodas() {
        ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
        partes.addAll(modelo.getDatos());
        return new NotificarPartesDeConvivenciaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), partes);
    }
}
