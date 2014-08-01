package org.hadoop.test.jobs.utils;

/**
 Class responsible for calculating Geometrically Distributed Means for Forest Fire Model (Graph Evolution).
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
