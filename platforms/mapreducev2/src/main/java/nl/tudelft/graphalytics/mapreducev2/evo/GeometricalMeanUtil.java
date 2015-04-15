/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.mapreducev2.evo;

/**
 Class responsible for calculating Geometrically Distributed Means for Forest Fire Model (Graph Evolution).

 @author Marcin Biczak
 */
public class GeometricalMeanUtil {
    private final int a = 16807;
    private final int m=2147483647;
    private final int q=127773; // m DIV a
    private final int r=2836; // m MOD a
    private int seed;

    public GeometricalMeanUtil() {
        this.putSeed(1);
        this.move(0);
    }

    private void putSeed(int seed) {
        //Assert(_Seed>=0); // IGNORE
        if (seed == 0)
            this.seed = (int)Math.abs(this.getPerfTimerTicks());
        else
            this.seed=seed;
    }

    private long getPerfTimerTicks() {
        long largeInt = System.nanoTime();

        if (largeInt > 0){
            String hex = Long.toHexString(largeInt);
            if(hex.length() < 8)
                for(int i=hex.length(); i<=8; i++)
                    hex = "0"+hex;

            return Long.parseLong(hex, 16);
        } else {
            return 0;
        }
    }

    private void move(int steps) {
        for(int StepN=0; StepN<steps; StepN++)
            getNextSeed();
    }

    private int getNextSeed() {
        if((seed=a*(seed%q)-r*(seed/q))>0)
            return seed;
        else
            return seed+=m;
    }

    private double getUniDev(){ return this.getNextSeed()/(double)m; }

    public int getGeoDev(double prb){
        return 1+(int)Math.floor( Math.log(1.0-this.getUniDev()) / Math.log(1.0-prb) );
    }
}
