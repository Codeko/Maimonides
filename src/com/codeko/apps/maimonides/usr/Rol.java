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

/**
 *
 * @author codeko
 */
public abstract class Rol {
    public static final int ROL_NULO=0;
    public static final int ROL_ADMIN=1;
    public static final int ROL_PROFESOR=2;
    public static final int ROL_JEFE_ESTUDIOS=4;
    public static final int ROL_DIRECTIVO=8;
    public static final int ROL_TUTOR=16;
    public static final int ROL_ALUMNO=32;
    public static final int ROL_PADRE=64;
    
    public static final int ROLES_EXTERNOS=ROL_ALUMNO | ROL_PADRE;
    
    public static final int ROL_TODOS=ROL_ADMIN | ROL_PROFESOR | ROL_JEFE_ESTUDIOS | ROL_DIRECTIVO | ROL_TUTOR | ROL_ALUMNO | ROL_PADRE;
        
        
    public static String getTextoRoles(int roles){
        StringBuilder sb=new StringBuilder();
        boolean primero=true;
        if((roles&ROL_ADMIN)==ROL_ADMIN){
            if(!primero){
                sb.append("/");
            }
            primero=false;
            sb.append("Admin");
        }
        if((roles&ROL_PROFESOR)==ROL_PROFESOR){
            if(!primero){
                sb.append("/");
            }
            primero=false;
            sb.append("Prof");
        }
        if((roles&ROL_JEFE_ESTUDIOS)==ROL_JEFE_ESTUDIOS){
            if(!primero){
                sb.append("/");
            }
            primero=false;
            sb.append("J.E.");
        }
        if((roles&ROL_DIRECTIVO)==ROL_DIRECTIVO){
            if(!primero){
                sb.append("/");
            }
            primero=false;
            sb.append("Dir");
        }
        if((roles&ROL_ALUMNO)==ROL_ALUMNO){
            if(!primero){
                sb.append("/");
            }
            primero=false;
            sb.append("Alu.");
        }
        if((roles&ROL_PADRE)==ROL_PADRE){
            if(!primero){
                sb.append("/");
            }
            primero=false;
            sb.append("Padres");
        }
        return sb.toString();
    }
}
