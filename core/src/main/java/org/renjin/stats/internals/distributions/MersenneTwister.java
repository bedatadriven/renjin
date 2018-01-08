/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.stats.internals.distributions;

/**
 * A Mersenne-Twister implementation modeled after and validated with GNU R's RNG.
 */
public class MersenneTwister {
    /* Period parameters */
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908b0df;   /* constant vector a */
    private static final int UPPER_MASK = 0x80000000; /* most significant w-r bits */
    private static final int LOWER_MASK = 0x7fffffff; /* least significant r bits */
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;
    private static final int GNU_R_SEED_SCRAMBLE_CONSTANT = 50;

    private int[] stateVector = new int[N + 1];
    private int stateVectorIndex = N+1;

    // A key difference here is the unsigned right shift >>> since we are using 32-bit two's complement integers
    private static int TEMPERING_SHIFT_U(int y) {
        return (y >>> 11);
    }
    private static int TEMPERING_SHIFT_S(int y) {
        return (y << 7);
    }
    private static int TEMPERING_SHIFT_T(int y) {
        return (y << 15);
    }
    private static int TEMPERING_SHIFT_L(int y) {
        return (y >>> 18);
    }

    /**
     * Default seedless constructor
     */
    public MersenneTwister() {
        this.setSeed(System.currentTimeMillis());
    }

    /**
     * Construct a new MersenneTwister with the given seed
     *
     * @param seed The seed
     */
    public MersenneTwister(long seed) {
        setSeed(seed);
    }

    /**
     * Sets the PRNG seed
     * @param seed The seed
     */
    public void setSeed(long seed) {
        int j;

        // Unsign the incoming seed
        int s = (int) (seed & 0x0000FFFFL);

        // Initial seed scrambling ( GNU R Convention )
        for (j = 0; j < GNU_R_SEED_SCRAMBLE_CONSTANT; j++)
            s = (69069 * s + 1);

        // Generate the seed vector
        for (j = 0; j < N + 1; j++) {
            s = (69069 * s + 1);
            stateVector[j] = s;
        }

        // Default the first element to unsigned N ( A GNU R convention )
        stateVector[0] = N;

        // Check for all zeros to be safe
        boolean notallzero = true;
        for (j = 1; j <= N; j++)
            if (stateVector[j] != 0) {
                notallzero = false;
                break;
            }
        if(notallzero) setSeed(System.currentTimeMillis());
    }

    /**
     * Gets the next double value from the PRNG
     *
     * @return The next double
     */
    public double nextDouble() {
        int y;
        int[] mag01 = new int[]{0x0, MATRIX_A};

        stateVectorIndex = stateVector[0];

        // Should N new words be generated?
        if (stateVectorIndex >= N) {
            int kk;

            for (kk = 1; kk < N - M + 1; kk++) {
                y = (stateVector[kk] & UPPER_MASK) | (stateVector[kk + 1] & LOWER_MASK);
                stateVector[kk] = stateVector[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];  // Note: Unsigned right shift

            }
            for (; kk < N; kk++) {
                y = (stateVector[kk] & UPPER_MASK) | (stateVector[kk + 1] & LOWER_MASK);
                stateVector[kk] = stateVector[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];   // Note: Unsigned right shift
            }
            y = (stateVector[N] & UPPER_MASK) | (stateVector[1] & LOWER_MASK);

            stateVector[N] = stateVector[M] ^ (y >>> 1) ^ mag01[y & 0x1]; // Note: Unsigned right shift
            stateVectorIndex = 0;
        }

        y = stateVector[stateVectorIndex +1];
        y ^= TEMPERING_SHIFT_U(y);
        y ^= TEMPERING_SHIFT_S(y) & TEMPERING_MASK_B;
        y ^= TEMPERING_SHIFT_T(y) & TEMPERING_MASK_C;
        y ^= TEMPERING_SHIFT_L(y);
        stateVector[0] = ++stateVectorIndex;

        // Unsign y
        long yUnsigned = (((long) y) & 0xFFFFFFFFL);

        // [0,1) interval
        return yUnsigned * 2.3283064365386963e-10;
    }
}
