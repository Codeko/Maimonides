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


package com.codeko.apps.maimonides.usr;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesView;
import com.codeko.apps.maimonides.PanelAnos;
import com.codeko.apps.maimonides.PanelInicio;
import com.codeko.apps.maimonides.alumnos.PanelAlumnos;
import com.codeko.apps.maimonides.alumnos.PanelAlumnos2;
import com.codeko.apps.maimonides.alumnos.PanelControlAlumnos;
import com.codeko.apps.maimonides.alumnos.PanelCorrespondencia;
import com.codeko.apps.maimonides.alumnos.PanelFichaAlumno;
import com.codeko.apps.maimonides.alumnos.PanelMatriculaciones;
import com.codeko.apps.maimonides.asistencia.escolaridad.PanelPerdidaEscolaridadGlobal;
import com.codeko.apps.maimonides.asistencia.escolaridad.PanelPerdidaEscolaridadPorMaterias;
import com.codeko.apps.maimonides.calendario.PanelCalendarioEscolar;
import com.codeko.apps.maimonides.cartero.PanelEnvioNotificacionesManuales;
import com.codeko.apps.maimonides.conf.PanelConfiguracionAccesoBD;
import com.codeko.apps.maimonides.convivencia.PanelConfiguracionConvivencia;
import com.codeko.apps.maimonides.convivencia.PanelExpulsiones;
import com.codeko.apps.maimonides.convivencia.PanelGeneracionExpulsiones;
import com.codeko.apps.maimonides.convivencia.PanelListaPartesConvivencia;
import com.codeko.apps.maimonides.convivencia.PanelNotificacionesConvivencia;
import com.codeko.apps.maimonides.convivencia.PanelPartes;
import com.codeko.apps.maimonides.convivencia.expulsiones.PanelListadoExpulsiones;
import com.codeko.apps.maimonides.convivencia.informes.PanelResumenConvivencia;
import com.codeko.apps.maimonides.cursos.PanelCursos;
import com.codeko.apps.maimonides.cursos.PanelCursosGrupos;
import com.codeko.apps.maimonides.cursos.PanelGrupos;
import com.codeko.apps.maimonides.dependencias.PanelDependencias;
import com.codeko.apps.maimonides.digitalizacion.PanelDigitalizacion;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.horarios.PanelEditorHorarios;
import com.codeko.apps.maimonides.horarios.PanelEditorHorariosDoble;
import com.codeko.apps.maimonides.importadores.PanelImportacionInicial;
import com.codeko.apps.maimonides.materias.PanelMaterias;
import com.codeko.apps.maimonides.partes.PanelEditorPartes;
import com.codeko.apps.maimonides.partes.PanelListaAlumnosParte;
import com.codeko.apps.maimonides.partes.PanelListaPartes;
import com.codeko.apps.maimonides.partes.cartas.PanelCartas;
import com.codeko.apps.maimonides.partes.cartas.PanelCartasAsistencia;
import com.codeko.apps.maimonides.partes.divisiones.PanelDivisionAlumnosMultimateria;
import com.codeko.apps.maimonides.partes.informes.PanelInformesPartesNoDigitalizados;
import com.codeko.apps.maimonides.partes.informes.PanelPartesMedioDigitalizados;
import com.codeko.apps.maimonides.partes.informes.PanelPartesNoFirmados;
import com.codeko.apps.maimonides.partes.informes.PanelPartesPendientes;
import com.codeko.apps.maimonides.partes.informes.asistencia.PanelEvolucionAsistencia;
import com.codeko.apps.maimonides.partes.justificaciones.PanelJustificaciones;
import com.codeko.apps.maimonides.partes.justificaciones.PanelJustificacionesRapidas;
import com.codeko.apps.maimonides.profesores.PanelProfesores;
import com.codeko.apps.maimonides.seneca.PanelExportacionSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.PanelDebugEnvioFicheros;
import java.util.HashMap;

/**
 *
 * @author codeko
 */
public class Permisos {

    private static final int ACCESO = 1;
    private static final int EDICION = 2;
    private static final int CREACION = 3;
    private static final int BORRADO = 4;
    private static final int ESPECIAL = 5;
    private static HashMap<String, Integer> acceso = null;
    private static HashMap<String, Integer> edicion = null;
    private static HashMap<String, Integer> creacion = null;
    private static HashMap<String, Integer> borrado = null;
    private static HashMap<String, Integer> especial = null;

    private static HashMap<String, Integer> getPermisosEdicion() {
        if (edicion == null) {
            edicion = new HashMap<String, Integer>();

            edicion.put(getNombre(PanelAnos.class), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_DIRECTIVO);
            edicion.put(getNombre(PanelDependencias.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelMaterias.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelMatriculaciones.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelCursos.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelGrupos.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelCalendarioEscolar.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelAlumnos.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelFichaAlumno.class), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_TUTOR);
            edicion.put(getNombre(PanelListaAlumnosParte.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelEditorHorarios.class), Rol.ROL_JEFE_ESTUDIOS);
            edicion.put(getNombre(PanelPartes.class), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR);
        }
        return edicion;
    }

    private static HashMap<String, Integer> getPermisosCreacion() {
        if (creacion == null) {
            creacion = new HashMap<String, Integer>();

            creacion.put(getNombre(PanelAnos.class), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_DIRECTIVO);
            creacion.put(getNombre(PanelDependencias.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelMaterias.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelCursos.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelGrupos.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelCalendarioEscolar.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelAlumnos.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelFichaAlumno.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelListaPartes.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelEditorHorarios.class), Rol.ROL_JEFE_ESTUDIOS);
            creacion.put(getNombre(PanelPartes.class), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR);
            creacion.put(getNombre(PanelExpulsiones.class), Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
        }
        return creacion;
    }

    private static HashMap<String, Integer> getPermisosBorrado() {
        if (borrado == null) {
            borrado = new HashMap<String, Integer>();

            borrado.put(getNombre(PanelAnos.class), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_DIRECTIVO);
            borrado.put(getNombre(PanelDependencias.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelMaterias.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelCursos.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelGrupos.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelCalendarioEscolar.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelAlumnos.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelFichaAlumno.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelListaPartes.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelEditorHorarios.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelPartes.class), Rol.ROL_JEFE_ESTUDIOS);
            borrado.put(getNombre(PanelExpulsiones.class), Rol.ROL_JEFE_ESTUDIOS);
        }
        return borrado;
    }

    private static HashMap<String, Integer> getPermisosEspecial() {
        if (especial == null) {
            especial = new HashMap<String, Integer>();
            especial.put(getNombre(PanelMatriculaciones.class), Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelCalendarioEscolar.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelAlumnos.class, "apoyos"), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelAlumnos.class, "importar"), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelAlumnos.class, "enviarWeb"), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelFichaAlumno.class, "apoyos"), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_TUTOR);
            especial.put(getNombre(PanelListaAlumnosParte.class, "limpiarParte"), Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelListaAlumnosParte.class, "quitarIndeterminados"), Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelListaAlumnosParte.class, "estadoParte"), Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelPartes.class, "asignarMedidas"), Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelPartes.class, "asignarExpulsion"), Rol.ROL_JEFE_ESTUDIOS);
            especial.put(getNombre(PanelListaPartesConvivencia.class, "enviarSeneca"), Rol.ROL_JEFE_ESTUDIOS | Rol.ROL_TUTOR);
        }
        return especial;
    }

    private static HashMap<String, Integer> getPermisosAcceso() {
        if (acceso == null) {
            acceso = new HashMap<String, Integer>();
            //Primero los elementos del menu y sus paneles asociados
            //Menu archivo
            String mmView = MaimonidesView.class.getCanonicalName() + ".";
            acceso.put(mmView + "quit", Rol.ROL_NULO);
            if(MaimonidesApp.isJnlp()){
                acceso.put(mmView + "editarConexion", Rol.ROL_ADMIN);
                acceso.put(getNombre(PanelConfiguracionAccesoBD.class),  Rol.ROL_ADMIN);
            }else{
                acceso.put(mmView + "editarConexion", Rol.ROL_NULO);
                acceso.put(getNombre(PanelConfiguracionAccesoBD.class),  Rol.ROL_NULO);
            }

            //acceso.put("com.codeko.apps.maimonides.MaimonidesView.mostrarPanelConfiguracion", Rol.ROL_ADMIN);

            //Menu herramientas
            acceso.put(mmView + "inicio", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelInicio.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            //acceso.put("com.codeko.apps.maimonides.MaimonidesView.backup", Rol.ROL_ADMIN);
            acceso.put(mmView + "mostrarPanelImportacionInicialDatos", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelImportacionInicial.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelProblemasEnvioFaltas", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelDebugEnvioFicheros.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelNotificacionesManuales", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelEnvioNotificacionesManuales.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelControlDatosAlumnos", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelControlAlumnos.class), Rol.ROL_JEFE_ESTUDIOS);

            //Menu datos
            acceso.put(mmView + "configAnoEscolar", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelAnos.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarDependencias", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelDependencias.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "editarProfesores", Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelProfesores.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "fichasAlumnos", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelAlumnos2.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "editarMaterias", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelMaterias.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "editarMatriculaciones", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelMatriculaciones.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "editarCursosGrupos", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelCursosGrupos.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelGrupos.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelCursos.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelCalendarioEscolar", Rol.ROL_NULO);
            acceso.put(getNombre(PanelCalendarioEscolar.class), Rol.ROL_NULO);

            acceso.put(mmView + "mostrarPanelNotificaciones", Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelCorrespondencia.class), Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            //Menu asistencia
            acceso.put(mmView + "editarPartes", Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelEditorPartes.class), Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelPartesGenericos", Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            //El panel es el de grupos y los permisos se definen en esa zona

            acceso.put(mmView + "digitalizarPartes", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelDigitalizacion.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(getNombre(PanelPartesMedioDigitalizados.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(getNombre(PanelPartesPendientes.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(getNombre(PanelPartesNoFirmados.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelJustificacionesRapidas", Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelJustificacionesRapidas.class), Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelJustificaciones", Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelJustificaciones.class), Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "exportarFaltasSeneca", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelExportacionSeneca.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarInformePartesNoFirmados", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelPartesNoFirmados.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarInformePartesNoDigitalizados", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelInformesPartesNoDigitalizados.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarInformeListadoAsistencia", Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelCartasAsistencia.class), Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarInformeEvolucionAsistencia", Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelEvolucionAsistencia.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarInformePerdidaEscolaridadMaterias", Rol.ROL_TUTOR | Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelPerdidaEscolaridadPorMaterias.class), Rol.ROL_TUTOR | Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarInformePerdidaEscolaridadGlobal", Rol.ROL_TUTOR | Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelPerdidaEscolaridadGlobal.class), Rol.ROL_TUTOR | Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelCartas", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelCartas.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);


            //Menu horarios
            acceso.put(mmView + "mostrarEditorHorarios", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelEditorHorariosDoble.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelEditorHorarios.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "divisionesAlumnos", Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelDivisionAlumnosMultimateria.class), Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "exportarFicheroHorariosSeneca", Rol.ROL_JEFE_ESTUDIOS);

            //Menu convivencia
            acceso.put(mmView + "mostrarPartesConvivencia", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelPartes.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarExpulsiones", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelExpulsiones.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelGeneracionExpulsiones", Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelGeneracionExpulsiones.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);


            acceso.put(mmView + "mostrarPanelResumenDeConvivencia", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelResumenConvivencia.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelInformeExpulsiones", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelListadoExpulsiones.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(mmView + "mostrarPanelListadoPartesConvivencia", Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelListaPartesConvivencia.class), Rol.ROL_DIRECTIVO | Rol.ROL_PROFESOR | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);


            acceso.put(mmView + "mostrarCartasExpulsion", Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);
            acceso.put(getNombre(PanelNotificacionesConvivencia.class), Rol.ROL_DIRECTIVO | Rol.ROL_TUTOR | Rol.ROL_JEFE_ESTUDIOS);

            acceso.put(getNombre(PanelConfiguracionConvivencia.class), Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);

            //Menu ayuda
            acceso.put(mmView + "showAboutBox", Rol.ROL_NULO);
            acceso.put(mmView + "mostrarAyuda", Rol.ROL_NULO);

            //Menu usuario
            acceso.put(mmView + "cambiarUsuario", Rol.ROL_NULO);
            acceso.put(mmView + "editarUsuarioActual", Rol.ROL_NULO);

        }
        return acceso;
    }

    public static boolean acceso(Object elemento, String tag) {
        return permiso(ACCESO, elemento, tag);
    }

    public static boolean edicion(Object elemento, String tag) {
        return permiso(EDICION, elemento, tag);
    }

    public static boolean creacion(Object elemento, String tag) {
        return permiso(CREACION, elemento, tag);
    }

    public static boolean borrado(Object elemento, String tag) {
        return permiso(BORRADO, elemento, tag);
    }

    public static boolean especial(Object elemento, String tag) {
        return permiso(ESPECIAL, elemento, tag);
    }

    public static boolean acceso(Object elemento) {
        return permiso(ACCESO, elemento, "");
    }

    public static boolean edicion(Object elemento) {
        return permiso(EDICION, elemento, "");
    }

    public static boolean creacion(Object elemento) {
        return permiso(CREACION, elemento, "");
    }

    public static boolean borrado(Object elemento) {
        return permiso(BORRADO, elemento, "");
    }

    public static boolean especial(Object elemento) {
        return permiso(ESPECIAL, elemento, "");
    }

    private static boolean permiso(int tipo, Object elemento, String tag) {
        int roles = Rol.ROL_NULO;
        if (MaimonidesApp.getApplication().getUsuario() != null) {
            roles = MaimonidesApp.getApplication().getUsuario().getRolesEfectivos();
        }
        return permiso(tipo, getNombre(elemento, tag), roles);
    }

    private static String getNombre(Object elemento) {
        return getNombre(elemento, "");
    }

    private static String getNombre(Object elemento, String tag) {
        String nombre = "";
        if (elemento instanceof Class) {
            nombre = ((Class) elemento).getCanonicalName();
        } else {
            nombre = elemento.getClass().getCanonicalName();
        }
        if (!tag.trim().equals("")) {
            nombre = nombre + "." + tag;
        }
        return nombre;
    }

    private static boolean permiso(int tipo, String elemento, int roles) {
        boolean ret = false;
        //Si es administrador siempre tiene acceso
        if ((roles & Rol.ROL_ADMIN) == Rol.ROL_ADMIN) {
            ret = true;
        } else {
            HashMap<String, Integer> hmPermisos = null;
            switch (tipo) {
                case ACCESO:
                    hmPermisos = getPermisosAcceso();
                    break;
                case EDICION:
                    hmPermisos = getPermisosEdicion();
                    break;
                case BORRADO:
                    hmPermisos = getPermisosBorrado();
                    break;
                case CREACION:
                    hmPermisos = getPermisosCreacion();
                    break;
                case ESPECIAL:
                    hmPermisos = getPermisosEspecial();
                    break;
            }
            if (hmPermisos != null && hmPermisos.containsKey(elemento)) {
                int permisos = hmPermisos.get(elemento);
                ret = (permisos & roles) > 0 || permisos == roles || permisos == Rol.ROL_NULO;
            }
        }
        return ret;
    }

    public static boolean isUsuarioSoloProfesor() {
        boolean ret = false;
        int roles = Rol.ROL_NULO;
        if (MaimonidesApp.getApplication().getUsuario() != null) {
            roles = MaimonidesApp.getApplication().getUsuario().getRolesEfectivos();
        }
        //Si es sólo profesor o profesor y tutor se le considera sólo profesor
        ret = (roles == Rol.ROL_PROFESOR) || (roles == (Rol.ROL_PROFESOR | Rol.ROL_TUTOR));
        return ret;
    }

    public static Profesor getFiltroProfesor() {
        Profesor p = null;
        Usuario usr = MaimonidesApp.getApplication().getUsuario();
        if (usr != null) {
            p = usr.getProfesor();
        }
        return p;
    }

    public static Unidad getFiltroUnidad() {
        Unidad u = null;
        if (isUsuarioSoloProfesor()) {
            Profesor p = getFiltroProfesor();
            if (p != null) {
                u = Unidad.getUnidadPorTutor(p.getId());
            }
        }
        return u;
    }

    public static boolean isRol(int rol) {
        boolean ret = false;
        Usuario u = MaimonidesApp.getApplication().getUsuario();
        if (u != null) {
            ret = (u.getRolesEfectivos() & rol) == rol;
        } else {
            ret = rol == Rol.ROL_NULO;
        }
        return ret;
    }
}
