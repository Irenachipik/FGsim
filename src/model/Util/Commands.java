package ptm1stuff;
/*
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import Commands.DefaultIO;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);

		// you may add default methods here
	}
	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	// you may add other helper classes here
	
	
	
	// the shared state of all commands
	private class SharedState{
		// implement here whatever you need
		public TimeSeries tsTrain,tsTest;
		public SimpleAnomalyDetector ad;
		public List<AnomalyReport> aR;
		
	}
	
	private  SharedState sharedState=new SharedState();

	
	// Command abstract class
	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute();
	}
	
	// Command class for example:
	public class ExampleCommand extends Command{

		public ExampleCommand() {
			super("this is an example of command");
		}

		@Override
		public void execute() {
			dio.write(description);
		}		
	}
	
	// implement here all other commands
	
	public class tsCommand extends Command{

		
		public tsCommand() {
			super("1. upload a time series csv file\n");
			
		}
		
		public void execute() {
			dio.write("Please upload your local train CSV file.\n");
			sharedState.tsTrain = new TimeSeries(dio);
			dio.write("Upload complete.\n"+"Please upload your local test CSV file.\n");
			sharedState.tsTest = new TimeSeries(dio);
			dio.write("Upload complete.\n");
		}

	}
	
	public class asCommand extends Command{
		float defThreshold = 0.9f;
		public asCommand() {
			super("2. algorithm settings\n");
		}

		@Override
		public void execute() {
			sharedState.ad = new SimpleAnomalyDetector();
			dio.write("The current correlation threshold is "+ defThreshold + "\n");
			dio.write("Type a new threshold\n");
			float trsh = Float.parseFloat(dio.readText());
			if(trsh>1||trsh<0)
				{dio.write("please choose a value between 0 and 1.");}
			else defThreshold=trsh;
			sharedState.ad.modLearnNormal(sharedState.tsTrain,defThreshold);
		}		
	}
	
	public class arCommand extends Command{
		public arCommand() {
			super("3. detect anomalies\n");
		}

		@Override
		public void execute() {
			sharedState.aR = sharedState.ad.detect(sharedState.tsTest);
			dio.write("anomaly detection complete.\n");
		}		
	}
	
	public class aResultCommand extends Command{
		public aResultCommand() {
			super("4. display results\n");
		}

		@Override
		public void execute() {
			sharedState.ad.results(sharedState.aR,dio);
		}		
	}
	
	public class rcmpCommand extends Command{
		public rcmpCommand() {
			super("5. upload anomalies and analyze results\n");
		}

		@Override
		public void execute() {
			//int tst = (sharedState.tsTest.ts.get(0).size()-1);
			//String lastkey;
			//for(Map.Entry<String,ArrayList<Float>> t : sharedState.tsTest.ts.get(0).size())
			//	lastkey=t.getKey();
			dio.write("Upload complete. \n");
			dio.write("Analyzing... \n");
			sharedState.ad.rcmp(sharedState.aR,dio,sharedState.tsTest.getRowSize());
		}		
	}
	
	public class exitCommand extends Command{
		public exitCommand() {
			super("6. exit\n");
		}

		@Override
		public void execute() {
			return;
		}		
	}
	
}
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Commands {

	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);

		// you may add default methods here
		public default void readAndFile(String fileName, String doneStr) {
			try {
				PrintWriter out = new PrintWriter(new FileWriter(fileName));
				String line;
				while(!(line=readText()).equals(doneStr)) {
					out.println(line);
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}

	// you may add other helper classes here

	private class FixdReport{
		public long start,end;
		public String description;
		public boolean tp;
	};


	// the shared state of all commands
	private class SharedState{
		// implement here whatever you need
		public float threshold;
		public int testFileSize;
		List<AnomalyReport> reports;
		ArrayList<FixdReport> fixdRports=new ArrayList<>();

		public SharedState(){
			threshold=0.9f;
			testFileSize=0;
		}

	}

	private  SharedState sharedState=new SharedState();


	// Command abstract class
	public abstract class Command{
		protected String description;

		public Command(String description) {
			this.description=description;
		}

		public abstract void execute();
	}

	// Command class for example:
	public class ExampleCommand extends Command{

		public ExampleCommand() {
			super("this is an example of command");
		}

		@Override
		public void execute() {
			dio.write(description);
		}
	}

	// implement here all other commands

	public class UploadCommand extends Command{

		public UploadCommand() {
			super("upload a time series csv file");
		}

		@Override
		public void execute() {
			dio.write("Please upload your local train CSV file.\n");
			dio.readAndFile("anomalyTrain.csv", "done");
			dio.write("Upload complete.\n");
			dio.write("Please upload your local test CSV file.\n");
			dio.readAndFile("anomalyTest.csv", "done");
			dio.write("Upload complete.\n");
		}
	}

	public class Settings extends Command{
		public Settings() {
			super("algorithm settings");
		}

		@Override
		public void execute() {
			boolean ok=false;
			while(!ok){
				dio.write("The current correlation threshold is "+sharedState.threshold+"\n");
				dio.write("Type a new threshold\n");
				float f=dio.readVal();
				dio.readText(); //enter
				if(f>0 && f<=1){
					sharedState.threshold=f;
					ok=true;
				}
				else
					dio.write("please choose a value between 0 and 1.\n");
			}
		}
	}

	public class Detect extends Command{
		public Detect() {
			super("detect anomalies");
		}

		@Override
		public void execute() {
			TimeSeries train=new TimeSeries("anomalyTrain.csv");
			TimeSeries test=new TimeSeries("anomalyTest.csv");
			sharedState.testFileSize = test.getRowSize();
			SimpleAnomalyDetector ad=new SimpleAnomalyDetector();
			ad.setCorrelationThreshold(sharedState.threshold);
			ad.learnNormal(train);
			sharedState.reports = ad.detect(test);

			FixdReport fr=new FixdReport();
			fr.start=0;
			fr.end=0;
			fr.description="";
			fr.tp=false;
			for(AnomalyReport ar : sharedState.reports) {
				if(ar.timeStep==fr.end+1 && ar.description.equals(fr.description))
					fr.end++;
				else{
					sharedState.fixdRports.add(fr);
					fr=new FixdReport();
					fr.start=ar.timeStep;
					fr.end=fr.start;
					fr.description=ar.description;
				}

			};
			sharedState.fixdRports.add(fr);
			sharedState.fixdRports.remove(0);

			dio.write("anomaly detection complete.\n");
		}
	}


	public class Results extends Command{
		public Results() {
			super("display results");
		}
		@Override
		public void execute() {
			sharedState.reports.forEach(ar->{
				dio.write(ar.timeStep);
				dio.write("\t "+ar.description+"\n");
			});
			dio.write("Done.\n");
		}

	}

	public class UploadAnom extends Command{
		public UploadAnom() {
			super("upload anomalies and analyze results");
		}

		boolean crossSection(long as,long ae,long bs, long be){
			return (ae>=bs && be>=as);
		}

		boolean isTP(long start, long end){
			for( FixdReport fr : sharedState.fixdRports){
				if(crossSection(start,end,fr.start,fr.end)){
					fr.tp=true;
					return true;
				}
			}
			return false;
		}


		@Override
		public void execute() {
			dio.write("Please upload your local anomalies file.\n");
			String s;
			float TP=0,sum=0,P=0;
			while(!(s=dio.readText()).equals("done")){
				long start = Long.parseLong(s.split(",")[0]);
				long end = Long.parseLong(s.split(",")[1]);
				if(isTP(start,end))
					TP++;
				sum+=end+1-start;
				P++;
			}
			dio.write("Upload complete.\n");
			float FP=0;
			for(FixdReport fr : sharedState.fixdRports)
				if(!fr.tp)
					FP++;

			float N=sharedState.testFileSize - sum;
			float tpr=((int)(1000.0*TP/P))/1000.0f;
			float fpr=((int)(1000.0*FP/N))/1000.0f;
			dio.write("True Positive Rate: ");
			dio.write(tpr);
			dio.write("\nFalse Positive Rate: ");
			dio.write(fpr);
			dio.write("\n");
		}
	}

	public class ExitCommand extends Command{
		public ExitCommand() {
			super("exit");
		}
		@Override
		public void execute() {
			// TODO Auto-generated method stub
		}
	}
}
