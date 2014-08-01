/***********************************************************************************************************************
 *
 * CD: Community Detection
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


public class CommunityDetectionCamb implements PlanAssembler, PlanAssemblerDescription{
	
	private static final Logger LOG = Logger.getLogger(CommunityDetectionCamb.class);
	public static final String curDir = System.getProperty("user.home");
	public static final String subDir = "/done/commcamb";
	public static final String doneFile = curDir + subDir +  "/tobeprocess";
	public static final String iniScore = "1.0";
	public static final double delta = 0.1;
	public static final double mvalue = 0.1;
	
	
	/**
	 * Read undirected graph
	 */
	public static class GraphInFormatUndirect extends DelimitedInputFormat
	{
		private final PactString fromNode = new PactString();
		private final PactString toNodeList = new PactString();
		private final PactString label = new PactString();
		private final PactString degree = new PactString();
		private final PactString score = new PactString();
		
		

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			lineStr = lineStr.replace("\t", " ").trim();
			StringTokenizer st = new StringTokenizer(lineStr, " ");
			
			this.fromNode.setValue(st.nextToken());
			String line_toNodeList = st.nextToken();
			this.toNodeList.setValue(line_toNodeList);
			this.label.setValue(this.fromNode);
			StringTokenizer st_toNodeList = new StringTokenizer(line_toNodeList, ",");
			this.degree.setValue(Integer.toString(st_toNodeList.countTokens()));
			this.score.setValue(iniScore);
			

			target.setField(0, this.fromNode);
			target.setField(1, this.toNodeList);
			target.setField(2, this.label);
			target.setField(3, this.degree);
			target.setField(4, this.score);
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
		private final PactString degree = new PactString();
		private final PactString score = new PactString();

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
				HashSet<String> set=new HashSet<String>();
				StringTokenizer tokenFrom = new StringTokenizer(fromList, ",");
				StringTokenizer tokenTo = new StringTokenizer(toList, ",");
				while(tokenFrom.hasMoreTokens())
				{
					set.add(tokenFrom.nextToken());
				}
				while(tokenTo.hasMoreTokens())
				{
					set.add(tokenTo.nextToken());
				}
				
				Iterator ite = set.iterator();
				String allList = new String("");
				while(ite.hasNext())
				{
					allList += ite.next().toString()+",";
				}
				allList = allList.substring(0, allList.length()-1);
				this.toNodeList.setValue(allList);
				set.clear();
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
			
			String line_toNodeList = this.toNodeList.getValue().toString();
			int int_degree = 0;
			if(line_toNodeList.equals("NULL"))
			{
				int_degree = 0;
			}
			else
			{
				StringTokenizer st_toNodeList = new StringTokenizer(line_toNodeList, ",");
				int_degree = st_toNodeList.countTokens();
			}
			
			
			this.degree.setValue(Integer.toString(int_degree));
			this.score.setValue(iniScore);		

			target.setField(0, this.fromNode);
			target.setField(1, this.toNodeList);
			target.setField(2, this.fromNode);
			target.setField(3, this.degree);
			target.setField(4, this.score);
	
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
		private final PactString degree = new PactString();
		private final PactString score = new PactString();
		

		@Override
		public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes)
		{
			String lineStr = new String(bytes, offset, numBytes);
			StringTokenizer st = new StringTokenizer(lineStr, "|");
			
			this.fromNode.setValue(st.nextToken());
			this.toNodeList.setValue(st.nextToken());
			this.label.setValue(st.nextToken());
			this.degree.setValue(st.nextToken());
			this.score.setValue(st.nextToken());
			
			

			target.setField(0, fromNode);
			target.setField(1, toNodeList);
			target.setField(2, label);
			target.setField(3, this.degree);
			target.setField(4, this.score);
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

			
			line.append(record.getField(0, PactString.class).toString());
			line.append("|");
			
			line.append(record.getField(1, PactString.class).toString());
			line.append("|");
			
			line.append(record.getField(2, PactString.class).toString());
			line.append("|");
			
			line.append(record.getField(3, PactString.class).toString());
			line.append("|");
			
			line.append(record.getField(4, PactString.class).toString());
			
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
			String oriLabel = record.getField(2, PactString.class).toString();
			String getDegree = record.getField(3, PactString.class).toString();
			String getScore = record.getField(4, PactString.class).toString();
			
			this.list.setValue("$"+line+"$"+oriLabel+"$"+getDegree+"$"+getScore);
			
			this.outputRecord.setField(0, this.vertex);
			this.outputRecord.setField(1, this.list);
			collector.collect(this.outputRecord);
			
			
			StringTokenizer st = new StringTokenizer(line, ",");
			while (st.hasMoreTokens())
			{
				String nVertex = st.nextToken();
				if(!nVertex.equals("NULL"))
				{
					this.newVertex.setValue(nVertex);
					this.outputRecord.setField(0, this.newVertex);
					this.label.setValue(oriLabel+"@"+getDegree+"@"+getScore+"@"+this.vertex.getValue().toString());
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
		private final PactString label = new PactString();
		private final PactString degree = new PactString();
		private final PactString score = new PactString();
		
		
		@SuppressWarnings("null")
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception
		{
			PactRecord element = null;
			String oldLabel = new String("");
			String oldScore = new String("");
			
			HashMap<String,Double> labelNum = new HashMap<String,Double>();
			HashMap<String,Double> labelMax = new HashMap<String,Double>();
			double maxLabelNum = 0;
			List<String> maxLabelList = new ArrayList<String>();
			
			while (records.hasNext())
			{
				element = records.next();
				this.vertex.setValue(element.getField(0, PactString.class));
				String listorlabelValue = element.getField(1, PactString.class).toString();
				if(listorlabelValue.startsWith("$"))
				{
					String getlist = listorlabelValue.substring(1);
					StringTokenizer stList = new StringTokenizer(getlist, "$");
					this.list.setValue(stList.nextToken());
					oldLabel = stList.nextToken();
					this.degree.setValue(stList.nextToken());
					oldScore = stList.nextToken();
				}
				else
				{
					StringTokenizer stLabelDegreeScore = new StringTokenizer(listorlabelValue, "@");
					String getLabel = stLabelDegreeScore.nextToken();
					String getDegree = stLabelDegreeScore.nextToken();
					String getScore = stLabelDegreeScore.nextToken();
					String getFrom = stLabelDegreeScore.nextToken();
					double douDegree = Double.parseDouble(getDegree);
					double douScore = Double.parseDouble(getScore);
					double increase = douScore * Math.pow(douDegree,mvalue);
					if(labelNum.containsKey(getLabel))
					{	
						
						double oldLabelNum = labelNum.get(getLabel);
						labelNum.put(getLabel, oldLabelNum+increase);
					}
					else
					{
						labelNum.put(getLabel, increase);
					}
					if(labelMax.containsKey(getLabel))
					{
						if(douScore > labelMax.get(getLabel))
							labelMax.put(getLabel, douScore);
					}
					else
					{
						labelMax.put(getLabel, douScore);
					}
				}
			}
			
			
			Iterator iter = labelNum.entrySet().iterator(); 
			while (iter.hasNext()) 
			{ 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    String labelKey = entry.getKey().toString(); 
			    double labelVal = Double.parseDouble(entry.getValue().toString());
			    if(labelVal > maxLabelNum)
			    {
			    	maxLabelNum = labelVal;
			    	maxLabelList.clear();
			    	maxLabelList.add(labelKey);
			    }
			    else if(labelVal == maxLabelNum)
			    {
			    	maxLabelList.add(labelKey);
			    }
			}
			
			
			if(maxLabelList.size() > 0)
			{
				Random r = new Random();
				int random = r.nextInt(maxLabelList.size());
				String selectedLabel = maxLabelList.get(random);
				
				this.label.setValue(selectedLabel);
				
				if(selectedLabel.equals(oldLabel))
				{
					this.score.setValue(Double.toString(labelMax.get(selectedLabel)));
				}
				else
				{
					this.score.setValue(Double.toString(labelMax.get(selectedLabel)-delta));
				}
				
				if(!selectedLabel.equals(oldLabel) && lastFlag == 1)
				{
					lastFlag = 0;
				}
			}
			else
			{
				this.label.setValue(oldLabel);
				this.score.setValue(oldScore);
			}
			
			
			element.setField(0, this.vertex);
			element.setField(1, this.list);
			element.setField(2, this.label);
			element.setField(3, this.degree);
			element.setField(4, this.score);
			
			out.collect(element);
			labelNum.clear();
			labelMax.clear();
			maxLabelList.clear();
			
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
		return new Plan(result, "Community Detection");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Parameters: [noSubStasks] [input] [output] [firstIteration] [directivity]";
	}
	
}
