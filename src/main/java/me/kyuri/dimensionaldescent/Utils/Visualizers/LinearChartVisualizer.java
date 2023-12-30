package me.kyuri.dimensionaldescent.Utils.Visualizers;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class LinearChartVisualizer extends JFrame {

    public LinearChartVisualizer() {
        super("Continentalness Chart");

        XYSeries series = new XYSeries("Continentalness");
        double[] ContinentalValues = new double[]{-1, 0.3, 0.4, 1.0};
        double[] ContinentalHeight = new double[]{50, 100, 150, 150};
        for (int i = 0; i < ContinentalValues.length; i++) {
            series.add(ContinentalValues[i], ContinentalHeight[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Continentalness Chart",
                "Noise Value",
                "Height",
                dataset
        );

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    public static void main(String[] args) {
        LinearChartVisualizer example = new LinearChartVisualizer();
        example.setSize(800, 400);
        example.setLocationRelativeTo(null);
        example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        example.setVisible(true);
    }
}