/***********************************************************************************************************************
 *
 * CONN: Connected Component
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



public class ConnectedComponent implements PlanAssembler, PlanAssemblerDescription{
	

	private static final Logger LOG = Logger.getLogger(ConnectedComponent.class);
	public static final int iterationCount = 1;
	public static final String starter = "firstnode";	
	public static final String curDir = System.getProperty("user.home");
	public static final String subDir = "/done/conn";
	public static final String doneFile = curDir + subDir +  "/tobeprocess";
	
	/**
	 * Read undirected graph
	 */
	public static class GraphInFormatUndirect extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();
		private final PactString label = new PactString();
		

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			lineStr = lineStr.replace("\t", " ").trim();
			StringTokenizer st = new StringTokenizer(lineStr, " ");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());
			this.label.setValue(this.fromNode);
			

			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, label);
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
			
			String fromList = "";
			String toList = "";
			
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
				String allList = fromList + "," + toList;
				this.toNodeList.setValue(allList);
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
			
			

			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, fromNode);
			
	
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
		private final PactString label = new PactString();
		

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			StringTokenizer st = new StringTokenizer(lineStr, "|");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());
			this.label.setValue(st.nextToken());
			

			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, label);
			//LOG.info("read records other");
		
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

			// append vertex
			line.append(record.getField(0, PactString.class).toString());
			line.append("|");
			// append 
			line.append(record.getField(1, PactString.class).toString());
			line.append("|");
			
			line.append(record.getField(2, PactInteger.class).toString());
			
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
				
		
		// initialize reusable mutable objects
		private final PactRecord outputRecord = new PactRecord();
		private final PactString vertex = new PactString();
		private final PactString newVertex = new PactString();
		private final PactString list = new PactString();
		private final PactString label = new PactString();
		
		@Override
		public void map(PactRecord record, Collector<PactRecord> collector)
		{

			this.vertex.setValue(record.getField(0, PactString.class).toString());
			
			String line = record.getField(1, PactString.class).toString();
			this.list.setValue("$"+line);
			
			this.label.setValue(record.getField(2, PactString.class).toString());
			
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.list);
			collector.collect(this.outputRecord);
			
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.label);
			collector.collect(this.outputRecord);
			
			
			StringTokenizer st = new StringTokenizer(line, ",");
			while (st.hasMoreTokens())
			{
				String nVertex = st.nextToken();
				if(!nVertex.equals("NULL"))
				{
					this.newVertex.setValue(nVertex);
					this.outputRecord.setField(0, this.newVertex);
					this.outputRecord.setField(1, this.label);
					collector.collect(this.outputRecord);
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
	public static class MergeRecords extends ReduceStub
	{		
		public static int lastFlag = 1;
		private final PactString vertex = new PactString();
		private final PactString list = new PactString();
		
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
			PactInteger newLabel = new PactInteger(Integer.MAX_VALUE);
			int valueDiff = 0;
			
			while (records.hasNext())
			{
				element = records.next();
				this.vertex.setValue(element.getField(0, PactString.class));
				String listorlabelValue = element.getField(1, PactString.class).toString();
				if(listorlabelValue.startsWith("$"))
				{
					this.list.setValue(listorlabelValue.substring(1));
				}
				else
				{
					if(Integer.parseInt(listorlabelValue) < newLabel.getValue())
					{						

						if (MergeRecords.lastFlag == 1 && valueDiff > 0)
						{
							MergeRecords.lastFlag = 0;
						}
						valueDiff++;
						newLabel.setValue(Integer.parseInt(listorlabelValue));			
					}
					if(Integer.parseInt(listorlabelValue) > newLabel.getValue())
					{
						if (MergeRecords.lastFlag == 1)
						{
							MergeRecords.lastFlag = 0;
						}
					}
				}
			}
			
			element.setField(0, this.vertex);
			element.setField(1, this.list);
			element.setField(2, newLabel);
			
			out.collect(element);
		}
		
		@Override
		public void close()
		{
			if (MergeRecords.lastFlag == 0)
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
	 * {@inheritDoc}
	 */
	@Override
	public Plan getPlan(String... args) {

		// parse job parameters
		int noSubTasks   = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
		String input     = (args.length > 1 ? args[1] : "");
		String output    = (args.length > 2 ? args[2] : "");
		String firstIteration    = (args.length > 3 ? args[3] : "");
		String directivity    = (args.length > 4 ? args[4] : "");
		

		

		FileDataSource pathsInput;
		
		pathsInput= new FileDataSource(GraphInFormatUndirect.class, input, "Paths");
		
		if(firstIteration.equals("1"))
		{
			if(directivity.equals("d"))
				pathsInput= new FileDataSource(GraphInFormatDirect.class, input, "Paths");
			if(directivity.equals("u"))
				pathsInput= new FileDataSource(GraphInFormatUndirect.class, input, "Paths");
		}
		else
		{
			pathsInput= new FileDataSource(GraphInFormatOther.class, input, "Paths");
		}
		pathsInput.setDegreeOfParallelism(noSubTasks);
		
		MapContract mapper = MapContract.builder(GenerateNewRecords.class)
				.input(pathsInput)
				.name("Generate new records")
				.build();
		mapper.setDegreeOfParallelism(noSubTasks);
		
		ReduceContract reducer = new ReduceContract.Builder(MergeRecords.class, PactString.class, 0)
				.input(mapper)
				.name("Merge records")
				.build();
		reducer.setDegreeOfParallelism(noSubTasks);

		FileDataSink result = new FileDataSink(GraphOutFormat.class, output, "Records with new label");
		result.setDegreeOfParallelism(noSubTasks);

		result.addInput(reducer);
		return new Plan(result, "Connected Component");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Parameters: [noSubStasks] [input] [output] [firstIteration] [directivity]";
	}

	
	

}
