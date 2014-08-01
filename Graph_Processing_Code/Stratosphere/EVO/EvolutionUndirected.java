/***********************************************************************************************************************
 *
 * EVO: Graph Evolution, Forest Fire Model
 *
 **********************************************************************************************************************/
package eu.stratosphere.pact.example.biggraph;

import eu.stratosphere.pact.example.biggraph.GeometricalMeanUtil;

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


public class EvolutionUndirected implements PlanAssembler, PlanAssemblerDescription{
	
	private static final Logger LOG = Logger.getLogger(EvolutionUndirected.class);
	public static final String newVerticesNum = "0";
	public static final String pNumber = "pNumber";
	
	/**
	 * Read undirected graph
	 */
	public static class GraphInFormatUndirect extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();
		

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			lineStr = lineStr.replace("\t", " ").trim();
			StringTokenizer st = new StringTokenizer(lineStr, " ");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());

			target.setField(0, this.fromNode);
			target.setField(1, this.toNodeList);

		
			return true;
		}
	}
	
	
	/**
	 * Output format
	 */
	public static class GraphOutFormat extends FileOutputFormat {

		@Override
		public void writeRecord(PactRecord record) throws IOException {
			StringBuilder line = new StringBuilder();

			
			line.append(record.getField(0, PactString.class).toString());
			line.append("|");
	
			line.append(record.getField(1, PactString.class).toString());
			
			line.append("\n");
			
			stream.write(line.toString().getBytes());
			
		}

	}

	
	/**
	 *
	 *Mapper
	 *
	 */
	@OutCardBounds(lowerBound=0, upperBound=OutCardBounds.UNBOUNDED)
	public static class GenerateAllVertices extends MapStub
	{
		
		// initialize reusable mutable objects
		private final PactRecord outputRecord = new PactRecord();
		private final PactString vertex = new PactString();
		private final PactString toList = new PactString();
		private final PactString sameKey = new PactString("K");

		
		
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector)
		{
			String source = record.getField(0, PactString.class).toString();
			this.vertex.setValue(source);
			String des = record.getField(1, PactString.class).toString();
			this.toList.setValue(des);
			
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.toList);
			collector.collect(this.outputRecord);	
			
			this.outputRecord.setField(0, this.sameKey);
			this.outputRecord.setField(1, this.vertex);
			collector.collect(this.outputRecord);	
			
			
		}

	}
	
	/**
	 *
	 *Reducer
	 *
	 */
	public static class GenerateAmbassadorFirst extends ReduceStub
	{		
		
		private String newVNum;	
		
		@Override
		public void open(Configuration parameters) {			
			this.newVNum = parameters.getString(newVerticesNum, "9999");
		}
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
			PactString ambassador = new PactString();
			PactString fromVertex = new PactString();
			PactString toList = new PactString();
			PactString newVertex = new PactString();
			List<String> allVertices = new ArrayList<String>();
			
			while (records.hasNext())
			{
				element = records.next();
				String source = element.getField(0, PactString.class).toString();
				String sVertex = element.getField(1, PactString.class).toString();
				
				//this.vertex.setValue(element.getField(1, PactString.class));
				if(source.startsWith("K"))
				{
					allVertices.add(sVertex);
				}
				else
				{
					fromVertex.setValue(source);
					toList.setValue(sVertex);
				}
				
			}
			
			if(fromVertex.getValue().toString().length() > 0)
			{
				element.setField(0, fromVertex);
				element.setField(1, toList);
				out.collect(element);
			}
			else
			{
				int addCounter = 0;
				String selectedAmbassador = new String();
				String vertexName = new String();
				while (addCounter < Integer.parseInt(this.newVNum))
				{
					Random r = new Random();
					int random = r.nextInt(allVertices.size());
					selectedAmbassador = allVertices.get(random);
					ambassador.setValue(selectedAmbassador);
					addCounter++;
					vertexName = "A"+ Integer.toString(addCounter);
					newVertex.setValue(vertexName);
					element.setField(0, ambassador);
					element.setField(1, newVertex);
					out.collect(element);
					element.setField(0, newVertex);
					element.setField(1, ambassador);
					out.collect(element);
				}
				allVertices.clear();
			}
		}
		
	}
	
	
	/**
	 *
	 *Reducer
	 *
	 */
	@ConstantFields(fields={0})
	@OutCardBounds(lowerBound=1, upperBound=1)
	public static class SetAmbassador extends ReduceStub
	{		
		
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
			PactString fromVertex = new PactString();
			PactString toListorNewVertex = new PactString();
			String toList = new String();
			String ambassadorList = new String();
			String sVertex = new String();
			while (records.hasNext())
			{
				element = records.next();
				sVertex = element.getField(0, PactString.class).toString();
				String tValue = element.getField(1, PactString.class).toString();
				
				if(tValue.startsWith("A"))
				{
					if (ambassadorList.length() == 0 )
					{
						ambassadorList = tValue;
					}
					else
					{
						ambassadorList += ",";
						ambassadorList += tValue;
					}
				}
				else
				{
					toList = tValue;
				}
			}
			if(ambassadorList.length() > 0)
			{
				toList = toList + "$" + ambassadorList; 
			}
			fromVertex.setValue(sVertex);
			toListorNewVertex.setValue(toList);
			
			
			element.setField(0, fromVertex);
			element.setField(1, toListorNewVertex);
			out.collect(element);
			
			//LOG.info("reduce");
		}
		
	}
	
	
	
	
	/**
	 * Read graphs during iteration
	 */
	public static class GraphInFormatOther extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			StringTokenizer st = new StringTokenizer(lineStr, "|");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());
			

			target.setField(0, this.fromNode);
			target.setField(1, this.toNodeList);
			
			return true;
		}
	}
	
	
	/**
	 *
	 *Mapper
	 *
	 */
	public static class GeneratePossibleAmbassador extends MapStub
	{
		
		// initialize reusable mutable objects
		private final PactRecord outputRecord = new PactRecord();
		private final PactString vertex = new PactString();
		private final PactString toList = new PactString();

		
		
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector)
		{
			String source = record.getField(0, PactString.class).toString();
			this.vertex.setValue(source);
			String des = record.getField(1, PactString.class).toString();
			if(des.contains("$"))
			{
				PactString amb = new PactString();
				PactString ambtoList = new PactString();
				StringTokenizer st = new StringTokenizer(des, "$");
				String temp = st.nextToken();
				this.toList.setValue(temp);
				ambtoList.setValue("$"+ temp);
				String ambassadorList = st.nextToken();
				StringTokenizer st2 = new StringTokenizer(ambassadorList, ",");
				while(st2.hasMoreTokens())
				{
					amb.setValue(st2.nextToken());
					this.outputRecord.setField(0, amb);
					this.outputRecord.setField(1, ambtoList);
					collector.collect(this.outputRecord);	
				}
			}
			else
			{
				this.toList.setValue(des);
			}
			
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.toList);
			collector.collect(this.outputRecord);	
			
		}

	}
	
	/**
	 *
	 *Reducer
	 *
	 */
	
	public static class GenerateAmbassadorNotFirst extends ReduceStub
	{		
		private int newAmbNum = 0;
		
		@Override
		public void open(Configuration parameters) {
			this.newAmbNum = parameters.getInteger(pNumber, 1);
		}
		
		
		//int newAmbNum = this.burnpNumber;
		
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
			PactString fromVertex = new PactString();
			PactString toList = new PactString();
			String oriAmbtoList = new String();
			List<String> allAmbtoList = new ArrayList<String>();
			String sVertex = new String();
			while (records.hasNext())
			{
				element = records.next();
				sVertex = element.getField(0, PactString.class).toString();
				String tValue = element.getField(1, PactString.class).toString();
				fromVertex.setValue(sVertex);
				if(sVertex.startsWith("A"))
				{
					if(tValue.startsWith("$"))
					{
						allAmbtoList.add(tValue.substring(1));
					}
					else
					{
						oriAmbtoList = tValue;
					}
				}
				else
				{
					fromVertex.setValue(sVertex);
					toList.setValue(tValue);
					element.setField(0, fromVertex);
					element.setField(1, toList);
					out.collect(element);
				}
			}
			
			if(sVertex.startsWith("A"))
			{
				Set<String> setOri = new HashSet<String>();
				StringTokenizer st = new StringTokenizer(oriAmbtoList, ",");
				while(st.hasMoreTokens())
				{
					setOri.add(st.nextToken());
				}
				
				Set<String> selectedAmbs = new HashSet<String>();
				Iterator<String> it = allAmbtoList.iterator();
	            while (it.hasNext())
	            {
	            	String strAmbtoList = it.next();
	            	List<String> canbeAmb = new ArrayList<String>();
	            	StringTokenizer st2 = new StringTokenizer(strAmbtoList, ",");
	            	while(st2.hasMoreTokens())
	            	{
	            		String possAmb = st2.nextToken();
	            		if(!setOri.contains(possAmb))
	            		{
	            			canbeAmb.add(possAmb);
	            			
	            		}
	            	}
	            	if(canbeAmb.size() <= Math.floor(this.newAmbNum))
	            	{
	            		Iterator<String> it2 = canbeAmb.iterator();
	            		while (it2.hasNext())
	            		{
	            			selectedAmbs.add(it2.next());
	            		}
	            	}
	            	else
	            	{
	            		int selCounter = 0;
	            		while(selCounter < Math.floor(this.newAmbNum))
	            		{
	            			Random r = new Random();
	            			int random = r.nextInt(canbeAmb.size());
	            			selectedAmbs.add(canbeAmb.get(random));
	            			canbeAmb.remove(random);
	            			selCounter++;
	            		}
	            	}
	            	canbeAmb.clear();	
	            }
	            
	            
	            Iterator<String> it3 = selectedAmbs.iterator();
	            PactString newFrom = new PactString();
	            while(it3.hasNext())
	            {
	            	String temp = it3.next();
	            	if(!setOri.contains(temp))
	            	{
	            		newFrom.setValue(temp);
	            		element.setField(0, newFrom);
						element.setField(1, fromVertex);
						out.collect(element);
						oriAmbtoList += ",";
						oriAmbtoList += temp;
	            	}
	            }
	            
	            PactString oriAmbassadortoList = new PactString();
	            oriAmbassadortoList.setValue(oriAmbtoList);
	            element.setField(0, fromVertex);
				element.setField(1, oriAmbassadortoList);
				out.collect(element);
				
				setOri.clear();
				selectedAmbs.clear();
				
				
			}
            
		}
		
	}
	
	private GeometricalMeanUtil gmu = new GeometricalMeanUtil();
	public int calculateOutLinks(float per) {
        return gmu.getGeoDev(1.0 - per);
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Plan getPlan(String... args) {

		// parse job parameters
		int noSubTasks   = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
		String input     = (args.length > 1 ? args[1] : "");
		String output    = (args.length > 2 ? args[2] : "");
		String addVertexNum    = (args.length > 3 ? args[3] : "");
		String firstIteration    = (args.length > 4 ? args[4] : "");
		String pPos    = (args.length > 5 ? args[5] : "");
		//LOG.info("addVertexNum: "+ addVertexNum);
		float flopPos = Float.parseFloat(pPos);
		int iter = Integer.parseInt(firstIteration);
		
		
		int argpNumber = 2;
		if (iter >= 2)
		{
			for (int i = 2;i<=iter;i++)
			{
				argpNumber = this.calculateOutLinks(flopPos);
			}
		}
		

		FileDataSink result = new FileDataSink(GraphOutFormat.class, output, "Write records");
		result.setDegreeOfParallelism(noSubTasks);

		if(firstIteration.equals("1"))
		{
			FileDataSource pathsInput = new FileDataSource(GraphInFormatUndirect.class, input, "Read records");
			pathsInput.setDegreeOfParallelism(noSubTasks);
			
			MapContract generateAllVertices = MapContract.builder(GenerateAllVertices.class)
					.input(pathsInput)
					.name("Generate all vertices")
					.build();
			generateAllVertices.setDegreeOfParallelism(noSubTasks);
	
			
			ReduceContract generateAmbassadorFirst = new ReduceContract.Builder(GenerateAmbassadorFirst.class, PactString.class, 0)
					.input(generateAllVertices)
					.name("Generate Ambassador for the first iteration")
					.build();
			generateAmbassadorFirst.setParameter(newVerticesNum, addVertexNum);
			generateAmbassadorFirst.setDegreeOfParallelism(noSubTasks);
			
			ReduceContract setAmbassador = new ReduceContract.Builder(SetAmbassador.class, PactString.class, 0)
					.input(generateAmbassadorFirst)
					.name("Set next iteration ambassador")
					.build();
			setAmbassador.setDegreeOfParallelism(noSubTasks);
	
			
			result.addInput(setAmbassador);	
			
		}
		else
		{
			FileDataSource pathsInput = new FileDataSource(GraphInFormatOther.class, input, "Read records >1");
			pathsInput.setDegreeOfParallelism(noSubTasks);
			
			MapContract generatePossibleAmbassador = MapContract.builder(GeneratePossibleAmbassador.class)
					.input(pathsInput)
					.name("Generate Possible Ambassador")
					.build();
			generatePossibleAmbassador.setDegreeOfParallelism(noSubTasks);
			
			ReduceContract generateAmbassadorNotFirst = new ReduceContract.Builder(GenerateAmbassadorNotFirst.class, PactString.class, 0)
					.input(generatePossibleAmbassador)
					.name("Generate Ambassador > 1")
					.build();
			generateAmbassadorNotFirst.setParameter(pNumber, argpNumber);
			generateAmbassadorNotFirst.setDegreeOfParallelism(noSubTasks);

			ReduceContract setAmbassador = new ReduceContract.Builder(SetAmbassador.class, PactString.class, 0)
					.input(generateAmbassadorNotFirst)
					.name("Set next iteration ambassador")
					.build();
			setAmbassador.setDegreeOfParallelism(noSubTasks);

	
			result.addInput(setAmbassador);	
			
		}
		return new Plan(result, "Evolution first iteration");
		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Parameters: [noSubStasks] [input] [output] [addVertexNum] [firstIteration] [pPos]";
	}
	
	
	
	

}
