/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package eu.stratosphere.pact.example.biggraph;

import java.io.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.contract.MapContract;
import eu.stratosphere.pact.common.contract.ReduceContract;
import eu.stratosphere.pact.common.io.DelimitedInputFormat;
import eu.stratosphere.pact.common.io.FileOutputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.stubs.ReduceStub;
import eu.stratosphere.pact.common.stubs.StubAnnotation.ConstantFields;
import eu.stratosphere.pact.common.stubs.StubAnnotation.OutCardBounds;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;

/**
 * @author yg
 *
 */
public class GeometricalMeanUtil {
	
    private final int a = 16807;
    private final int m=2147483647;
    private final int q=127773; // m DIV a
    private final int r=2836; // m MOD a
    private int seed;

    // dt.h
    // TRnd(const int& _Seed=1, const int& Steps=0){ PutSeed(_Seed); Move(Steps); }
    public GeometricalMeanUtil() {
        this.putSeed(1);
        this.move(0);
    }

    // dt.cpp
    // void TRnd::PutSeed(const int& _Seed){ ... }
    private void putSeed(int seed) {
        //Assert(_Seed>=0); // IGNORE
        if (seed == 0)
            this.seed = (int)Math.abs(this.getPerfTimerTicks());
        else
            this.seed=seed;
    }

    // http://stackoverflow.com/questions/10381140/how-to-query-the-high-resolution-performance-counter
    // this method is so EVIL :)
    /*
        os.cpp
        uint64 TSysTm::GetPerfTimerTicks(){ ... }
     */
    private long getPerfTimerTicks() {
        long largeInt = System.nanoTime(); //isn't return System.nanoTime(); sufficient

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

    // dt.cpp
    // void TRnd::Move(const int& Steps){ for (int StepN=0; StepN<Steps; StepN++){GetNextSeed();} }
    private void move(int steps) {
        for(int StepN=0; StepN<steps; StepN++)
            getNextSeed();
    }

    // dt.h
    //int GetNextSeed(){ if ((Seed=a*(Seed%q)-r*(Seed/q))>0){return Seed;} else {return Seed+=m;} }
    private int getNextSeed() {
        if((seed=a*(seed%q)-r*(seed/q))>0)
            return seed;
        else
            return seed+=m;
    }

    // dt.h
    // double GetUniDev(){ return GetNextSeed()/double(m); }
    private double getUniDev(){ return this.getNextSeed()/(double)m; }

    // dt.h
    // int GetGeoDev(const double& Prb){ return 1+(int)floor(log(1.0-GetUniDev())/log(1.0-Prb)); }
    public int getGeoDev(double prb){
        return 1+(int)Math.floor( Math.log(1.0-this.getUniDev()) / Math.log(1.0-prb) );
    }

}
