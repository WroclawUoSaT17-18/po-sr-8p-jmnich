package kernel;

import java.util.LinkedList;

import javafx.scene.chart.XYChart;

public class ImpedanceCurveMeasurement {
	
	private LinkedList<String> frequencyAxis;
	private LinkedList<String> impRealAxis;
	private LinkedList<String> impImagAxis;
	private LinkedList<String> impMagAxis;
	private LinkedList<String> impPhaseAxis;
	
	private String measurementComment;
	
	public ImpedanceCurveMeasurement() {
		frequencyAxis = new LinkedList<>();
		impRealAxis = new LinkedList<>();
		impImagAxis = new LinkedList<>();
		impMagAxis = new LinkedList<>();
		impPhaseAxis = new LinkedList<>();
		measurementComment = "";
	}
	
	public void addPoint(String f, String Z_re, String Z_im, String Z_mag, String Z_ph) {
		frequencyAxis.add(f);
		impRealAxis.add(Z_re);
		impImagAxis.add(Z_im);
		impMagAxis.add(Z_mag);
		impPhaseAxis.add(Z_ph);
	}
	
	public void setComment(String comment) {
		measurementComment = comment;
	}
	
	public String getComment() {
		return measurementComment;
	}
	
	public LinkedList<String> getFrequencyAxis() {
		return frequencyAxis;
	}
	
	public LinkedList<String> getImpedanceRealAxis() {
		return impRealAxis;
	}
	
	public LinkedList<String> getImpedanceImaginaryAxis() {
		return impImagAxis;
	}
	
	public LinkedList<String> getImpedanceMagnitudeAxis() {
		return impMagAxis;
	}
	
	public LinkedList<String> getImpedancePhaseAxis() {
		return impPhaseAxis;
	}

	public XYChart.Series<Number, Number> getNyquistChartSeries(String seriesName) {
		
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(seriesName);
		
		for(int i = 0; i < frequencyAxis.size(); i++) {
			series.getData().add(new XYChart.Data<Number, Number>(Double.valueOf(impRealAxis.get(i)), 
					Double.valueOf(impImagAxis.get(i))));
		}
		
		return series;
	}
	
	public XYChart.Series<Number, Number> getBodePhaseChartSeries(String seriesName) {
		
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(seriesName);
		
		for(int i = 0; i < frequencyAxis.size(); i++) {
			series.getData().add(new XYChart.Data<Number, Number>(Double.valueOf(frequencyAxis.get(i)), 
					Double.valueOf(impPhaseAxis.get(i))));
		}
		
		return series;
	}
	
	public XYChart.Series<Number, Number> getBodeMagnitudeChartSeries(String seriesName) {
		
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(seriesName);
		
		for(int i = 0; i < frequencyAxis.size(); i++) {
			series.getData().add(new XYChart.Data<Number, Number>(Double.valueOf(frequencyAxis.get(i)), 
					Double.valueOf(impMagAxis.get(i))));
		}
		
		return series;
	}
}
