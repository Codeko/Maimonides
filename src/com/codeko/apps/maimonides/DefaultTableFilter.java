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

import java.util.ArrayList;
//import org.jdesktop.swingx.decorator.Filter;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
@Deprecated
public abstract class DefaultTableFilter {
//TODO BORRAR
//    extends Filter {
//
//    private ArrayList<Integer> toPrevious;
//
//    @Override
//    public int getSize() {
//        return toPrevious.size();
//    }
//
//    @Override
//    protected void init() {
//        toPrevious = new ArrayList<Integer>();
//    }
//
//    @Override
//    protected void reset() {
//        toPrevious.clear();
//        int inputSize = getInputSize();
//        fromPrevious = new int[inputSize]; // fromPrevious is inherited protected
//        for (int i = 0; i < inputSize; i++) {
//            fromPrevious[i] = -1;
//        }
//    }
//
//    @Override
//    protected void filter() {
//        int inputSize = getInputSize();
//        int current = 0;
//        for (int i = 0; i < inputSize; i++) {
//            if (test(i)) {
//                toPrevious.add(new Integer(i));
//                // generate inverse map entry while we are here
//                fromPrevious[i] = current++;
//            }
//        }
//    }
//
//    abstract protected boolean test(int row);
//
//    @Override
//    protected int mapTowardModel(int row) {
//        return toPrevious.get(row);
//    }
}
