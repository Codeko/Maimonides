package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.convivencia.Expulsion;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSourceProvider;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ParteGenericoDataSourceProvider extends JRAbstractBeanDataSourceProvider implements JRDataSource {

    MaimonidesBean bean = null;
    AnoEscolar anoEscolar = null;
    GregorianCalendar fecha = null;
    int posicion = 0;
    int posicionAlumno = -1;
    ArrayList<Unidad> unidades = null;

    public MaimonidesBean getBean() {
        if (bean == null) {
            bean = new MaimonidesBean();
        }
        return bean;
    }

    public final void setBean(MaimonidesBean bean) {
        this.bean = bean;
    }

    public int getPosicionAlumno() {
        return posicionAlumno;
    }

    public void setPosicionAlumno(int posicionAlumno) {
        this.posicionAlumno = posicionAlumno;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public final void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public ParteGenericoDataSourceProvider(AnoEscolar anoEscolar, GregorianCalendar fecha, MaimonidesBean bean, Collection<Unidad> unidades) {
        super(ParteGenericoDataSourceProvider.class);
        setAnoEscolar(anoEscolar);
        setFecha(fecha);
        setBean(bean);
        setUnidades(new ArrayList<Unidad>(unidades));
    }

    public ArrayList<Unidad> getUnidades() {
        return unidades;
    }

    public final void setUnidades(ArrayList<Unidad> unidades) {
        this.unidades = unidades;
    }

    @Override
    public JRDataSource create(JasperReport arg0) throws JRException {

        return this;
    }

    @Override
    public void dispose(JRDataSource arg0) throws JRException {
        setUnidades(null);
    }

    @Override
    public boolean supportsGetFieldsOperation() {
        return false;
    }

    public Unidad getUnidadActual() {
        return getUnidades().get(getPosicion());
    }

    @Override
    public boolean next() throws JRException {
        //Avanzamos la posicion del alumno
        setPosicionAlumno(getPosicionAlumno() + 1);
        //vemos si hay más datos de alumno para esa posicion
        boolean ok = true;
        if (getUnidadActual().getAlumnos().size() <= getPosicionAlumno()) {
            //Entonces nos movemos al siguiente parte
            setPosicion(getPosicion() + 1);
            setPosicionAlumno(0);
            ok = getPosicion() < getUnidades().size();
            if (ok) {
                getBean().firePropertyChange("progress", (getPosicion() * 100) / getUnidades().size(), ((getPosicion() + 1) * 100) / getUnidades().size());
                getBean().firePropertyChange("message", null, "Rellenando parte " + getUnidadActual().getDescripcionObjeto() + "...");
            }
        }
        //FIXME Esto no debería darse en ningún caso pero se da
        while (ok && getUnidadActual().getAlumnos().isEmpty()) {
            Logger.getLogger(ParteDataSourceProvider.class.getName()).log(Level.SEVERE, "El parte no tiene alumnos: {0}", getUnidadActual());
            ok = next();
        }
        return ok;
    }

    public Alumno getAlumnoActual() {
        return getUnidadActual().getAlumnos().get(getPosicionAlumno());
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object ret = null;
        if (field.getName().equals("idParte")) {
            ret = getUnidadActual().getCursoGrupo();
        } else if (field.getName().equals("unidades")) {
            ret = getUnidadActual().getCursoGrupo();
        } else if (field.getName().equals("fecha")) {
            ret = getFecha().getTime();
        } else if (field.getName().equals("nombreAlumno")) {
            Alumno a = getAlumnoActual();
            StringBuilder sb = new StringBuilder();
            sb.append(a.getApellido1());
            sb.append(" ");
            sb.append(a.getApellido2());
            sb.append(", ");
            sb.append(a.getNombre());
            ret = sb.toString();
        } else if (field.getName().equals("posicionAlumno")) {
            ret = getPosicionAlumno() + 1;
        } else if (field.getName().equals("infoExtraCabecera")) {

            if (getUnidadActual().getIdProfesor() > 0) {
                try {
                    ret = "Tutor: " + Profesor.getNombreProfesor(getUnidadActual().getIdProfesor());
                } catch (Exception e) {
                    ret = "Tutor: ";
                }
            } else {
                ret = "Tutor: ";
            }
        } else if (field.getName().equals("alumnoExpulsado")) {
            ret = Expulsion.isAlumnoExpulsado(getAlumnoActual(), getFecha());
        } else if (field.getName().equals("curso")) {
            ret = getAlumnoActual().getUnidad().getCursoGrupo();
        } else if (field.getName().equals("pie")) {
            ret = "Parte genérico de la unidad " + getUnidadActual().getCursoGrupo();
        } else {
            HashMap<String, Object> datosBase = Configuracion.getDatosBaseImpresion();
            if (datosBase.containsKey(field.getName())) {
                ret = datosBase.get(field.getName());
            }
        }
        return ret;
    }
}
