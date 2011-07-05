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


package com.codeko.apps.maimonides.horarios.impresion;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.horarios.BloqueHorario;
import com.codeko.apps.maimonides.horarios.MateriaVirtual;
import com.codeko.apps.maimonides.horarios.PanelVisionHorario;
import com.codeko.util.Str;
import java.util.ArrayList;

/**
 *
 * @author codeko
 */
public class HorarioImprimible {

    String titulo = "";
    Alumno alumno = null;
    Profesor profesor = null;
    Unidad unidad = null;
    ArrayList<BloqueHorario> bloques = null;
    int posicion = 0;

    public HorarioImprimible(Alumno alumno, Profesor profesor, Unidad unidad) {
        setAlumno(alumno);
        setProfesor(profesor);
        setUnidad(unidad);
        StringBuilder sb = new StringBuilder();
        if (unidad != null) {
            sb.append("Unidad: ").append(unidad).append(" ");
        }
        if (profesor != null) {
            sb.append("Profesor: ").append(profesor).append(" ");
        }
        if (alumno != null) {
            sb.append("Alumno: ").append(alumno).append(" ");
        }
        setTitulo(sb.toString());
    }

    public ArrayList<BloqueHorario> getBloques() {
        if (bloques == null) {
            bloques = Horario.getHorarios(getAlumno(), getProfesor(), getUnidad(), null, null);
            //Tenemos que asegurarnos de que hay horarios para todas las horas
            for (int i = 0; i < PanelVisionHorario.HORAS_DIA; i++) {
                //Ahora buscamos un bloque por cada dia
                int hora = i + 1;
                boolean ok = false;
                for (BloqueHorario b : bloques) {
                    if (b.getHora() == hora) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    //Añadimos un bloque horario falso
                    BloqueHorario b = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), 1, hora, null, null, null);
                    bloques.add(b);
                }
            }
            //Y para todos los dias de la semana
            for (int i = 0; i < PanelVisionHorario.DIAS_SEMANA; i++) {
                //Ahora buscamos un bloque por cada dia
                int dia = i + 1;
                boolean ok = false;
                for (BloqueHorario b : bloques) {
                    if (b.getDia() == dia) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    //Añadimos un bloque horario falso
                    BloqueHorario b = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), dia, 1, null, null, null);
                    bloques.add(b);
                }
            }

        }
        return bloques;
    }

    public void setBloques(ArrayList<BloqueHorario> bloques) {
        this.bloques = bloques;
    }

    public String getTitulo() {
        return titulo;
    }

    public final void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public final void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public final void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public final void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    private BloqueHorario getBloqueActual() {
        return getBloques().get(posicion);
    }

    public String getMateria() {
        if (getBloqueActual() != null) {
            return getCodigosMateriasBloque(getBloqueActual());
        }
        return null;
    }

    private String getCodigosMateriasBloque(BloqueHorario b) {
        ArrayList<String> materias = new ArrayList<String>();
        for (MateriaVirtual m : b.getMateriasVirtuales()) {
            if (!materias.contains(m.getCodigo())) {
                materias.add(m.getCodigo());
            }
        }
        return Str.implode(materias, ", ");
    }

    public String getTextoBloque() {
        if (getBloqueActual() != null) {
            //Tenemos que buscar todos los bloques que sean de la misma hora y dia
            ArrayList<BloqueHorario> bloquesActivos = new ArrayList<BloqueHorario>();
            bloquesActivos.add(getBloqueActual());
            for (BloqueHorario b : getBloques()) {
                if (b != getBloqueActual() && b.getDia() == getBloqueActual().getDia() && b.getHora() == getBloqueActual().getHora()) {
                    bloquesActivos.add(b);
                }
            }
            StringBuilder sb = new StringBuilder("");
            boolean primero = true;
            for (BloqueHorario b : bloquesActivos) {
                if (primero) {
                    primero = false;
                } else {
                    sb.append("<br/>");
                }
                if (getUnidad() == null) {
                    sb.append(getUnidadesBloque(b)).append("<br/>");
                }
                sb.append("<b>").append(getCodigosMateriasBloque(b)).append("</b>");
                if (getProfesor() == null) {
                    sb.append(": ");
                    sb.append(Str.noNulo(b.getProfesor()));
                }
                if (b.getDependencia() != null) {
                    sb.append(" [").append(b.getDependencia().getNombre()).append("]");
                }
            }
            sb.append("");
            return sb.toString();
        }
        return null;
    }

    public String getAula() {
        if (getBloqueActual() != null) {
            if (getBloqueActual().getDependencia() != null) {
                return Str.noNulo(getBloqueActual().getDependencia().getNombre());
            }
        }
        return null;
    }

    public Integer getHora() {
        if (getBloqueActual() != null) {
            return getBloqueActual().getHora();
        }
        return null;
    }

    public Integer getDia() {
        if (getBloqueActual() != null) {
            return getBloqueActual().getDia();
        }
        return null;
    }

    public String getUnidadActual() {
        if (getBloqueActual() != null) {
            return getUnidadesBloque(getBloqueActual());
        }
        return null;
    }

    private String getUnidadesBloque(BloqueHorario b) {
        ArrayList<String> unidades = new ArrayList<String>();
        for (Unidad u : b.getUnidades()) {
            String s = u.toString();
            if (!unidades.contains(s)) {
                unidades.add(s);
            }
        }
        return Str.implode(unidades, ", ");
    }

    public String getProfesorActual() {
        if (getBloqueActual() != null) {
            return Str.noNulo(getBloqueActual().getProfesor());
        }
        return null;
    }

    public String getLeyenda() {
        //Tenemos que sacar el código y nombre de todas las materias virtuales
        ArrayList<MateriaVirtual> materias = new ArrayList<MateriaVirtual>();
        for (BloqueHorario b : getBloques()) {
            for (MateriaVirtual m : b.getMateriasVirtuales()) {
                if (!materias.contains(m)) {
                    materias.add(m);
                }
            }
        }
        //Ahora creamos la cadena con las materias
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (MateriaVirtual m : materias) {
            if (primero) {
                primero = false;
            } else {
                sb.append(", ");
            }
            sb.append("<b>").append(m.getCodigo()).append("</b>: ").append(m.getNombre());
        }
        return sb.toString();
    }

    public boolean next() {
        this.posicion++;
        return posicion < getBloques().size();
    }
}
