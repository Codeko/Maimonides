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
        return sb.toString();
    }
}
