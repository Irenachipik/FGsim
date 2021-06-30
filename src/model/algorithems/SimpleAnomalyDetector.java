package ptm1stuff;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ptm1stuff.Commands.*;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
	
	ArrayList<CorrelatedFeatures> cf;
	private float correlationThreshold;
	public SimpleAnomalyDetector() {
		cf=new ArrayList<>();
	}

	@Override
	public void learnNormal(TimeSeries ts) {
		ArrayList<String> atts=ts.getAttributes();
		int len=ts.getRowSize();

		float vals[][]=new float[atts.size()][len];
		for(int i=0;i<atts.size();i++){
			for(int j=0;j<ts.getRowSize();j++){
				vals[i][j]=ts.getAttributeData(atts.get(i)).get(j);
			}
		}


		for(int i=0;i<atts.size();i++){
			for(int j=i+1;j<atts.size();j++){
				float p=StatLib.pearson(vals[i],vals[j]);
				if(Math.abs(p)>0.9){

					Point ps[]=toPoints(ts.getAttributeData(atts.get(i)),ts.getAttributeData(atts.get(j)));
					Line lin_reg=StatLib.linear_reg(ps);
					float threshold=findThreshold(ps,lin_reg)*1.1f; // 10% increase

					CorrelatedFeatures c=new CorrelatedFeatures(atts.get(i), atts.get(j), p, lin_reg, threshold);

					cf.add(c);
				}
			}
		}
	}

	private Point[] toPoints(ArrayList<Float> x, ArrayList<Float> y) {
		Point[] ps=new Point[x.size()];
		for(int i=0;i<ps.length;i++)
			ps[i]=new Point(x.get(i),y.get(i));
		return ps;
	}
	
	private float findThreshold(Point ps[],Line rl){
		float max=0;
		for(int i=0;i<ps.length;i++){
			float d=Math.abs(ps[i].y - rl.f(ps[i].x));
			if(d>max)
				max=d;
		}
		return max;
	}
	
	public void modLearnNormal(TimeSeries ts, float trsh) {
		ArrayList<String> atts=ts.getAttributes();
		int len=ts.getRowSize();

		float vals[][]=new float[atts.size()][len];
		for(int i=0;i<atts.size();i++){
			for(int j=0;j<ts.getRowSize();j++){
				vals[i][j]=ts.getAttributeData(atts.get(i)).get(j);
			}
		}


		for(int i=0;i<atts.size();i++){
			for(int j=i+1;j<atts.size();j++){
				float p=StatLib.pearson(vals[i],vals[j]);
				if(Math.abs(p)>trsh){

					Point ps[]=toPoints(ts.getAttributeData(atts.get(i)),ts.getAttributeData(atts.get(j)));
					Line lin_reg=StatLib.linear_reg(ps);
					float threshold=findThreshold(ps,lin_reg)*1.1f; // 10% increase

					CorrelatedFeatures c=new CorrelatedFeatures(atts.get(i), atts.get(j), p, lin_reg, threshold);

					cf.add(c);
				}
			}
		}
	}
	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		ArrayList<AnomalyReport> v=new ArrayList<>();
		
		for(CorrelatedFeatures c : cf) {
			ArrayList<Float> x=ts.getAttributeData(c.feature1);
			ArrayList<Float> y=ts.getAttributeData(c.feature2);
			for(int i=0;i<x.size();i++){
				if(Math.abs(y.get(i) - c.lin_reg.f(x.get(i)))>c.threshold){
					String d=c.feature1 + "-" + c.feature2;
					v.add(new AnomalyReport(d,(i+1)));
				}
			}			
		}
		return v;
	}
	public void results(List<AnomalyReport> ar,DefaultIO dio){
		for(AnomalyReport anmly : ar) {
			dio.write(anmly.timeStep +"\t" + anmly.description + "\n" );
		}
		dio.write("Done.\n");
	}
	
	public void rcmp(List<AnomalyReport> ar,DefaultIO dio,int lts){
		String line;
		HashMap<Long, String> map = new HashMap<Long,String>();
		for(AnomalyReport anmly : ar) {
			map.put(anmly.timeStep, anmly.description);
		}
		int flag =0,sum=0;
		long anml[]=new long[2];
		while(!(line=dio.readText()).equalsIgnoreCase("done")) {
			int i = 0;
			for(String val : line.split(",")) {
				anml[i]=Long.parseLong(val);
				i++;
			}
			for(long a = anml[0];a<=anml[1];a++) {
				if(map.containsKey(a))
					flag++;
			}
			sum+=anml[1]-anml[0];
		
	}
		System.out.println("True Positive Rate: " + (float)flag/ar.size() );
		System.out.println("False Positive Rate: " + (float)(ar.size()-flag)/(lts-sum) );
		DecimalFormat df = new DecimalFormat("#.###");
		dio.write("True Positive Rate: " + df.format((float)flag/(float)ar.size()) + "\n" );
		dio.write("False Positive Rate: " + (float)(ar.size()-flag)/(float)(lts-sum) + "\n" );
}
	public void setCorrelationThreshold(float threshold) {
		correlationThreshold=threshold;
	}
	public List<CorrelatedFeatures> getNormalModel(){
		return cf;
	}
}
