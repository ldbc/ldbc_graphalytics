/***********************************************************************************************************************
 *
 * BFS: Breadth First Search
 *
 **********************************************************************************************************************/
package eu.stratosphere.pact.example.biggraph;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.apache.log4j.Logger;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.FileDataSource;
import eu.stratosphere.pact.common.contract.MapContract;
import eu.stratosphere.pact.common.contract.ReduceContract;
import eu.stratosphere.pact.common.contract.ReduceContract.Combinable;
import eu.stratosphere.pact.common.io.DelimitedInputFormat;
import eu.stratosphere.pact.common.io.FileOutputFormat;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.stubs.ReduceStub;
import eu.stratosphere.pact.common.stubs.StubAnnotation.ConstantFields;
import eu.stratosphere.pact.common.stubs.StubAnnotation.OutCardBounds;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;



public class BreadthFirstSearch implements PlanAssembler, PlanAssemblerDescription{
	

	private static final Logger LOG = Logger.getLogger(BreadthFirstSearch.class);
	public static final int iterationCount = 1;
	public static final String starter = "firstnode";
	// flag file for detecting the converge of algorithm
	public static final String lastIteration = "no";
	public static final String curDir = System.getProperty("user.home");
	public static final String subDir = "/done/bfs";
	public static final String doneFile = curDir + subDir +  "/tobeprocess";
	
	/**
	 * Read undirected graph
	 */	
	public static class GraphInFormatUndirect extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();
		private final PactInteger distance = new PactInteger();
		private final PactInteger status = new PactInteger();

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			lineStr = lineStr.replace("\t", " ").trim();
			StringTokenizer st = new StringTokenizer(lineStr, " ");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());
			this.distance.setValue(Integer.MAX_VALUE);
			this.status.setValue(0);

			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, distance);
			target.setField(3, status);
			
		
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
		private final PactInteger distance = new PactInteger();
		private final PactInteger status = new PactInteger();

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			StringTokenizer st = new StringTokenizer(lineStr, "#");
			
			this.fromNode.setValue(st.nextToken().trim());
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "@");
			st2.nextToken();
			if(st2.hasMoreTokens())
			{	
				String toList = st2.nextToken().trim();
				if(toList.length() > 0)
				{
					this.toNodeList.setValue(toList);
				}
				else
				{
					this.toNodeList.setValue("NULL");
				}
			}
			else
				this.toNodeList.setValue("NULL");
			
			
			this.distance.setValue(Integer.MAX_VALUE);
			this.status.setValue(0);
			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, distance);
			target.setField(3, status);
	
			return true;
		}
	}
	
	
	/**
	 * Read graphs during iteration
	 */
	public static class GraphInFormatOther extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();
		private final PactInteger distance = new PactInteger();
		private final PactInteger status = new PactInteger();

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			StringTokenizer st = new StringTokenizer(lineStr, "|");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());
			this.distance.setValue(Integer.parseInt(st.nextToken()));
			this.status.setValue(Integer.parseInt(st.nextToken()));

			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, distance);
			target.setField(3, status);
		
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
			line.append("|");
			line.append(record.getField(2, PactInteger.class).toString());
			line.append("|");
			line.append(record.getField(3, PactInteger.class).toString());
			line.append("\n");
			
			stream.write(line.toString().getBytes());
			
		}

	}
	
	/**
	 *
	 *Mapper
	 *
	 */
	@ConstantFields(fields={0})
	@OutCardBounds(lowerBound=0, upperBound=OutCardBounds.UNBOUNDED)
	public static class GenerateNewRecords extends MapStub
	{
		
		private String fromFirst;		
		public static int lastFlag = 1;
		

		@Override
		// get the root vertex of BFS
		public void open(Configuration parameters) {
			this.fromFirst = parameters.getString(starter, "9999");
		}
		
		
		// initialize reusable mutable objects
		private final PactRecord outputRecord = new PactRecord();
		private final PactString vertex = new PactString();
		private final PactString newVertex = new PactString();
		private final PactString list = new PactString();
		private final PactString newList = new PactString();
		private final PactInteger dis = new PactInteger();
		private final PactInteger newDis = new PactInteger();
		private final PactInteger sta = new PactInteger();
		private final PactInteger newSta = new PactInteger();
		
		
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector)
		{
			// get the first field (as type PactString) from the record and emit a record
			String source = record.getField(0, PactString.class).toString();
			this.vertex.setValue(source);
			
			// get the second field and emit all records
			String line = record.getField(1, PactString.class).toString();
			this.list.setValue(line);
			
			
			
			int distance = record.getField(2, PactInteger.class).getValue();
			this.dis.setValue(distance);
			
			int status = record.getField(3, PactInteger.class).getValue();
			this.sta.setValue(status);
			
			if(this.vertex.getValue().equals(this.fromFirst) && this.sta.getValue() < 1)
			{
				this.dis.setValue(0);
				this.sta.setValue(1);
			}
			
			
			if (this.sta.getValue() == 1)
			{
				if (GenerateNewRecords.lastFlag == 1)
				{
					GenerateNewRecords.lastFlag = 0;
				}
				
				if(!line.equals("NULL"))
				{		
					StringTokenizer st = new StringTokenizer(line, ",");
					while (st.hasMoreTokens())
					{
						this.newVertex.setValue(st.nextToken());
						this.newList.setValue("NULL");
						this.newDis.setValue(this.dis.getValue()+1);
						this.newSta.setValue(1);
						this.outputRecord.setField(0, this.newVertex);
						this.outputRecord.setField(1, this.newList);
						this.outputRecord.setField(2, this.newDis);
						this.outputRecord.setField(3, this.newSta);
						collector.collect(this.outputRecord);
					}
				}
				
				this.sta.setValue(2);
			}
			
			
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.list);
			this.outputRecord.setField(2, this.dis);
			this.outputRecord.setField(3, this.sta);
			collector.collect(this.outputRecord);	
			
		}
		
		
		@Override
		public void close()
		{
			if (GenerateNewRecords.lastFlag == 0)
			{
				File dir = new File(curDir + subDir);
				if (!dir.exists())
				{
					dir.mkdirs();
				}
				File filename = new File(doneFile); 
				if (!filename.exists()) 
				{ 
					try {
						filename.createNewFile();
						FileWriter filewriter = new FileWriter(filename, true);
					    filewriter.write("The process is to be done");
					    filewriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
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
	@Combinable
	public static class MergeRecords extends ReduceStub
	{		
		int distance = 0;
		int status = 0;
		private final PactString vertex = new PactString();
		private final PactString list = new PactString();
		private final PactInteger dis = new PactInteger();
		private final PactInteger sta = new PactInteger();
		
		
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
		
			PactString newList = new PactString("NULL");
			PactInteger newDis = new PactInteger(Integer.MAX_VALUE);
			PactInteger newSta = new PactInteger(0);
			
			while (records.hasNext())
			{
				element = records.next();
				this.vertex.setValue(element.getField(0, PactString.class));
				this.list.setValue(element.getField(1, PactString.class));
				this.dis.setValue(element.getField(2, PactInteger.class).getValue());
				this.sta.setValue(element.getField(3, PactInteger.class).getValue());
				if(!this.list.getValue().equals("NULL"))
				{
					newList.setValue(this.list);
				}
				if(this.dis.getValue() < newDis.getValue())
				{
					newDis.setValue(this.dis.getValue());
				}
				if(this.sta.getValue() > newSta.getValue())
				{
					newSta.setValue(this.sta.getValue());
				}
			}
			
			element.setField(0, this.vertex);
			element.setField(1, newList);
			element.setField(2, newDis);
			element.setField(3, newSta);
			out.collect(element);
			
		}
		
		@Override
		public void combine(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			// the logic is the same as in the reduce function, so simply call the reduce method
			this.reduce(records, out);
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
		String firstNode    = (args.length > 3 ? args[3] : "");
		String firstIteration    = (args.length > 4 ? args[4] : "");
		String directivity = (args.length > 5 ? args[5] : "");
		

		FileDataSource pathsInput;
		
		pathsInput= new FileDataSource(GraphInFormatUndirect.class, input, "Read input 1 iteration");
		
		if(firstIteration.equals("1"))
		{
			if(directivity.equals("d"))
				pathsInput= new FileDataSource(GraphInFormatDirect.class, input, "Read input 1 iteration");
			if(directivity.equals("u"))
				pathsInput= new FileDataSource(GraphInFormatUndirect.class, input, "Read input 1 iteration");
		}
		else
		{
			pathsInput= new FileDataSource(GraphInFormatOther.class, input, "Read input >1 iteration");
		}
		
		
		pathsInput.setDegreeOfParallelism(noSubTasks);
		
		MapContract mapper = MapContract.builder(GenerateNewRecords.class)
				.input(pathsInput)
				.name("Map tasks")
				.build();
		mapper.setParameter(starter, firstNode);
		mapper.setParameter(lastIteration, "yes");
		mapper.setDegreeOfParallelism(noSubTasks);

		
		ReduceContract reducer = new ReduceContract.Builder(MergeRecords.class, PactString.class, 0)
				.input(mapper)
				.name("Reduce tasks")
				.build();
		
		reducer.setDegreeOfParallelism(noSubTasks);

		FileDataSink result = new FileDataSink(GraphOutFormat.class, output, "Write output");
		result.setDegreeOfParallelism(noSubTasks);

		result.addInput(reducer);
		return new Plan(result, "Breadth First Search");
		

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Parameters: [noSubStasks] [input] [output] [firstNode] [firstIteration] [directivity]";
	}

	
	

}
