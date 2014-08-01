/***********************************************************************************************************************
 *
 * STATS: calculate the number of vertices, the number of edges, and the average local clustering coefficient
 *
 **********************************************************************************************************************/
package eu.stratosphere.pact.example.biggraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

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
import eu.stratosphere.pact.common.type.base.PactDouble;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;
import eu.stratosphere.pact.example.biggraph.BreadthFirstSearch.GenerateNewRecords;
import eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb.MergeRecords;
import eu.stratosphere.pact.example.biggraph.EvolutionUndirected.GraphOutFormat;



public class NumberLCC implements PlanAssembler, PlanAssemblerDescription{
	
	private static final Logger LOG = Logger.getLogger(EvolutionUndirected.class);
	
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

			//LOG.info("read records");
		
			return true;
		}
	}
	
	
	/**
	 * Read directed graph
	 */	
	public static class GraphInFormatDirect extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			StringTokenizer st = new StringTokenizer(lineStr, "#");
			
			this.fromNode.setValue(st.nextToken().trim());
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "@");
			
			String fromList = new String("");
			String toList = new String("");
			
			if(st2.hasMoreTokens())
			{	
				fromList = st2.nextToken().trim();	
				if(st2.hasMoreTokens())
				{
					toList = st2.nextToken().trim();
				}
			}
						
			if(fromList.length() > 0 && toList.length() > 0)
			{
				this.toNodeList.setValue(fromList+","+toList);
			}
			else if(fromList.length() == 0 && toList.length() > 0)
			{
				this.toNodeList.setValue(toList);
			}
			else if(fromList.length() > 0 && toList.length() == 0)
			{
				this.toNodeList.setValue(fromList);
			}
			else if(fromList.length() == 0 && toList.length() == 0)
			{
				this.toNodeList.setValue("NULL");
			}
			
			
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
	 * Mapper directed graph
	 *
	 */
	@OutCardBounds(lowerBound=0, upperBound=OutCardBounds.UNBOUNDED)
	public static class GenerateRecordDirect extends MapStub
	{
		
		
		private final PactRecord outputRecord = new PactRecord();
		private final PactString vertex = new PactString();
		private final PactString toList = new PactString();
		private final PactString VMark = new PactString("V");
		private final PactString VEdge = new PactString("");
		private final PactString possEdge = new PactString("");

		
		
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector)
		{
			String source = record.getField(0, PactString.class).toString();
			this.vertex.setValue(source);
			String des = record.getField(1, PactString.class).toString();
			this.toList.setValue(des);
			
			//generate number of edges
			StringTokenizer st = new StringTokenizer(des, ",");
			
			String edgeNum =  Integer.toString(st.countTokens());
			this.VEdge.setValue(source+"E"+edgeNum);
			this.outputRecord.setField(0, this.VMark);
			this.outputRecord.setField(1, this.VEdge);
			collector.collect(this.outputRecord);
			
			//calculate the possible edge number of neighbors
			List<String> uniqueVertex = new ArrayList<String>();
			
			while(st.hasMoreElements())
			{
				String getV = st.nextToken();
				if (!uniqueVertex.contains(getV))
					uniqueVertex.add(getV);
			}
			
			String uniqueNei = "";
			
			Iterator<String> it = uniqueVertex.iterator();
            while (it.hasNext())
            {
            	String uniV = it.next();
            	uniqueNei += uniV;
            	uniqueNei += ",";
            }
			String modiUniNei = uniqueNei.substring(0, uniqueNei.length()-1);
			int neiNum = uniqueVertex.size();
			String possNum = Integer.toString((neiNum)*(neiNum-1));
			this.possEdge.setValue(possNum+"$"+modiUniNei);
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.possEdge);
			collector.collect(this.outputRecord);
			
			
			StringTokenizer st2 = new StringTokenizer(modiUniNei, ",");
			PactString newVertex = new PactString();
			while (st2.hasMoreTokens())
			{
				String nVertex = st2.nextToken();
				if(!nVertex.equals("NULL"))
				{
					newVertex.setValue(nVertex);
					this.outputRecord.setField(0, newVertex);
					this.outputRecord.setField(1, this.toList);
					collector.collect(this.outputRecord);
				}
			}
			
			uniqueVertex.clear();
			
			
		}

	}
	
	/**
	 *
	 * Mapper undirected graph
	 *
	 */
	@OutCardBounds(lowerBound=0, upperBound=OutCardBounds.UNBOUNDED)
	public static class GenerateRecordUndirect extends MapStub
	{
		
		private final PactRecord outputRecord = new PactRecord();
		private final PactString vertex = new PactString();
		private final PactString toList = new PactString();
		private final PactString VMark = new PactString("V");
		private final PactString VEdge = new PactString("");
		private final PactString possEdge = new PactString("");

		
		
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector)
		{
			String source = record.getField(0, PactString.class).toString();
			this.vertex.setValue(source);
			String des = record.getField(1, PactString.class).toString();
			this.toList.setValue(des);
			
			//generate number of edges
			StringTokenizer st = new StringTokenizer(des, ",");
			
			String edgeNum =  Integer.toString(st.countTokens());
			this.VEdge.setValue(source+"E"+edgeNum);
			this.outputRecord.setField(0, this.VMark);
			this.outputRecord.setField(1, this.VEdge);
			collector.collect(this.outputRecord);
			
			//calculate the possible edge number of neighbors
			int neiNum = st.countTokens();
			String possNum = Integer.toString((neiNum)*(neiNum-1)/2);
			this.possEdge.setValue(possNum+"$"+des);
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.possEdge);
			collector.collect(this.outputRecord);
			
			PactString newVertex = new PactString();
			while (st.hasMoreTokens())
			{
				String nVertex = st.nextToken();
				if(!nVertex.equals("NULL"))
				{
					newVertex.setValue(nVertex);
					this.outputRecord.setField(0, newVertex);
					this.outputRecord.setField(1, this.toList);
					collector.collect(this.outputRecord);
				}
			}	
			
			
		}

	}
	
	/**
	 *
	 * Reducer calculate LCC for each vertex
	 *
	 */
	public static class CalculateEach extends ReduceStub
	{		
		private final PactString vertex = new PactString();
		private final PactString list = new PactString();
		private final PactString verticeNum = new PactString("#Vertices");
		private final PactString edgesNum = new PactString("#Edges");
		private final PactString LCC = new PactString("LCC");
		private final PactString LCCValue = new PactString("");
		private final PactString cnt = new PactString();
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			
			PactRecord element = null;
			
			//for number
			
			int vSum = 0;
			int eSum = 0;
			
			//for LCC
			List<String> allNei = new ArrayList<String>();
			String possNum = ""; 
			String neighborList = "";
			List<String> neighborArray = new ArrayList<String>();
			while (records.hasNext())
			{
				element = records.next();
				this.vertex.setValue(element.getField(0, PactString.class));
				String list = element.getField(1, PactString.class).toString();
				if(this.vertex.getValue().equals("V"))
				{
					vSum +=1;
					StringTokenizer st = new StringTokenizer(list, "E");
					st.nextToken();
					int eNum = Integer.parseInt(st.nextToken());
					eSum += eNum;
				}
				else
				{
					if(list.contains("$"))
					{
						StringTokenizer st2 = new StringTokenizer(list, "$");
						possNum = st2.nextToken();
						neighborList = st2.nextToken();
						StringTokenizer stt = new StringTokenizer(neighborList, ",");
						while(stt.hasMoreElements())
						{
							neighborArray.add(stt.nextToken());
						}
					}
					else
					{
						allNei.add(list);
					}
				}
			}
			
			double existNum = 0.0;
			if(this.vertex.getValue().equals("V"))
			{
				element.setField(0, this.verticeNum);
				this.cnt.setValue(Integer.toString(vSum));
				element.setField(1, this.cnt);
				out.collect(element);
				
				element.setField(0, this.edgesNum);
				this.cnt.setValue(Integer.toString(eSum/2));
				element.setField(1, this.cnt);
				out.collect(element);
			}
			else
			{
				
				Iterator<String> it = allNei.iterator();
	            while (it.hasNext())
	            {
	            	String neiList = it.next();
	            	StringTokenizer st3 = new StringTokenizer(neiList, ",");
	            	while(st3.hasMoreTokens())
	            	{
	            		String existVertex = st3.nextToken();
	            		if(neighborArray.contains(existVertex))
	            		{
	            			existNum+=1;
	            		}
	            	}
	            }
	            double eachLCC = 0.0;
				if (!possNum.equals("0"))
				{
					eachLCC = existNum/Double.parseDouble(possNum)/2;
				}
				element.setField(0, this.LCC);
				this.LCCValue.setValue(this.vertex.getValue()+"L"+Double.toString(eachLCC));
				element.setField(1, this.LCCValue);
				out.collect(element);
				allNei.clear();
			}
			
		}
		
		
	}
	
	
	
	/**
	 *
	 * Reducer calculate the average of LCC of each vertex
	 *
	 */
	public static class AverageLCC extends ReduceStub
	{		
		
		private final PactString vertex = new PactString();
		private final PactString LCC = new PactString("LCC");
		private final PactString cnt = new PactString();
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
			int vSum = 0;
			double LCCSum = 0.0;
			while (records.hasNext())
			{
				element = records.next();
				this.vertex.setValue(element.getField(0, PactString.class));
				String list = element.getField(1, PactString.class).toString();
				if(this.vertex.getValue().equals("LCC"))
				{
					vSum += 1;
					StringTokenizer st = new StringTokenizer(list, "L");
					st.nextToken();
					double LCCValue = Double.parseDouble(st.nextToken());
					LCCSum += LCCValue;	
				}

			}

			if(this.vertex.getValue().equals("LCC"))
			{
				element.setField(0, this.LCC);
				this.cnt.setValue(Double.toString(LCCSum/(double)vSum));
				element.setField(1, this.cnt);
				out.collect(element);
			}
			
			else
			{
				out.collect(element);
			}	

		}		
		
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
		String directivity = (args.length > 3 ? args[3] : "");
		

		
		
		MapContract mapper = MapContract.builder(GenerateRecordDirect.class).build();

		// plan for different types of graphs
		if(directivity.equals("d"))
		{
			FileDataSource pathsInput= new FileDataSource(GraphInFormatDirect.class, input, "Paths");
			pathsInput.setDegreeOfParallelism(noSubTasks);
			mapper = MapContract.builder(GenerateRecordDirect.class)
					.input(pathsInput)
					.name("Generate directed records")
					.build();
			mapper.setDegreeOfParallelism(noSubTasks);
			
		}
		if(directivity.equals("u"))
		{
			FileDataSource pathsInput= new FileDataSource(GraphInFormatUndirect.class, input, "Paths");
			pathsInput.setDegreeOfParallelism(noSubTasks);
			mapper = MapContract.builder(GenerateRecordUndirect.class)
					.input(pathsInput)
					.name("Generate undirected records")
					.build();
			mapper.setDegreeOfParallelism(noSubTasks);
			
		}

		
		ReduceContract reducerEach = new ReduceContract.Builder(CalculateEach.class, PactString.class, 0)
				.input(mapper)
				.name("Calculate LCC for each node and get Numbers")
				.build();		
		reducerEach.setDegreeOfParallelism(noSubTasks);
		
		ReduceContract reducerAvg = new ReduceContract.Builder(AverageLCC.class, PactString.class, 0)
				.input(reducerEach)
				.name("Average LCC")
				.build();
		reducerEach.setDegreeOfParallelism(noSubTasks); 

		
		FileDataSink result = new FileDataSink(GraphOutFormat.class, output, "Write records");
		result.setDegreeOfParallelism(noSubTasks);
		result.addInput(reducerAvg);
		return new Plan(result, "NumberLCC");
		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Parameters: [noSubStasks] [input] [output] [directivity]";
	}

}
