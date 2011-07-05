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
