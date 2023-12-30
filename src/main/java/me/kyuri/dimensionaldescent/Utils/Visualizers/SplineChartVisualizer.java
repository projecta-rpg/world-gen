package me.kyuri.dimensionaldescent.Utils.Visualizers;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class SplineChartVisualizer extends JFrame {

    public SplineChartVisualizer() {
        super("Continentalness Chart");
        this.setResizable(true);
        XYSeries series = new XYSeries("Continentalness");

        double[] ContinentalValues = new double[]{-1, -0.5, 0, 0.4, 0.6, 0.8, 1.0};
        double[] ContinentalHeight = new double[]{20, 30, 40, 60, 80, 100, 120};
        for (int i = 0; i < ContinentalValues.length; i++) {
            series.add(ContinentalValues[i], ContinentalHeight[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Continentalness Chart",
                "Noise Value",
                "Height",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        NumberAxis xAxis = new NumberAxis("Noise Value");
        NumberAxis yAxis = new NumberAxis("Height");

        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        // Set explicit range for x-axis
        xAxis.setRange(-1.0, 1.0);

        // Set explicit range for y-axis
        yAxis.setRange(0.0, 300.0); // Adjust this range based on your data

        XYSplineRenderer renderer = new XYSplineRenderer();
        plot.setRenderer(renderer);

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    public static void main(String[] args) {
        SplineChartVisualizer example = new SplineChartVisualizer();
        example.setSize(800, 600);
        example.setLocationRelativeTo(null);
        example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        example.setVisible(true);
    }
}
