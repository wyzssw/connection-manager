package com.wap.sohu.recom.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 类TopNewsStrategy.java的实现描述：
 *
 * @author yeyanchao Jun 21, 2013 3:41:06 PM
 */
public class TopNewsStrategy {

    /**
     * short category retrive contants for index : 0, 1, 2, 3
     */
    private static final Map<Integer, double[]> retriveProbabilitys = new HashMap<Integer, double[]>();

    static {
        for (int i = 1; i < 10; i++) {
            retriveProbabilitys.put(i, distributeProbability(i));
        }
    }

    /**
     * @param shortCatSize
     * @return
     */
    public static int queryShortCatIndex(int shortCatSize) {
        if (shortCatSize <= 0) return -1;
        Random random = new Random();
        double prob = random.nextDouble();
        double[] distribution = retriveProbabilitys.get(shortCatSize);
        if (distribution == null || distribution.length < shortCatSize) {
            distribution = distributeProbability(shortCatSize);
            retriveProbabilitys.put(shortCatSize, distribution);
        }
        // search short cat index
        int index = 0;
        for (; index < distribution.length; index++) {
            if (prob <= distribution[index]) {
                break;
            }
        }
        return index;
    }

    /**
     * create probability distribution
     *
     * @param size
     * @return
     */
    private static double[] distributeProbability(int size) {
        double step = 1.0 / size;
        double delta = 1.0 / (size * size);
        double center = (size - 1) / 2.0;

        double[] probabilities = new double[size];
        for (int i = 0; i < size; i++) {
            probabilities[i] = step + delta * (center - i);
            if (i > 0) {
                probabilities[i] += probabilities[i - 1];
            }
        }

        if (probabilities[size - 1] < 1.0) probabilities[size - 1] = 1.0;

        return probabilities;
    }

    public static void main(String[] args) {
        for (Map.Entry<Integer, double[]> entry : retriveProbabilitys.entrySet()) {

            System.out.println(entry.getKey() + " : " + Arrays.toString(entry.getValue()));
        }
    }
}
