package me.kyuri.dimensionaldescent.Utils.Visualizers;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

public class LinearGraph extends JFrame {
    private XYSeries series;
    private XYSeriesCollection dataset;
    private XYDataItem selectedPoint;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private boolean isDragging = false;

    public LinearGraph() {
        super("Line Point Graph");


        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exportPoints();
            }
        });
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        series = new XYSeries("Data");
        dataset = new XYSeriesCollection(series);

        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPopupMenu(null);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        chartPanel.setMouseZoomable(false, false);

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (event.getEntity() instanceof XYItemEntity) {
                    XYItemEntity entity = (XYItemEntity) event.getEntity();
                    int seriesIndex = entity.getSeriesIndex();
                    int item = entity.getItem();
                    selectedPoint = series.getDataItem(item);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
            }
        });

        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int x = e.getX();
                    int y = e.getY();
                    Rectangle2D dataArea = chartPanel.getScreenDataArea();
                    XYPlot plot = (XYPlot) chart.getPlot();
                    double chartX = plot.getDomainAxis().java2DToValue(x, dataArea, plot.getDomainAxisEdge());
                    double chartY = plot.getRangeAxis().java2DToValue(y, dataArea, plot.getRangeAxisEdge());

                    // Limit the noise values between -1 and 1
                    chartX = Math.max(-1, Math.min(1, chartX));
                    // Limit the height values between 0 and 319
                    chartY = Math.max(0, Math.min(319, chartY));

                    series.add(chartX, chartY);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                Rectangle2D dataArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) chart.getPlot();
                double chartX = plot.getDomainAxis().java2DToValue(x, dataArea, plot.getDomainAxisEdge());
                double chartY = plot.getRangeAxis().java2DToValue(y, dataArea, plot.getRangeAxisEdge());

                selectedPoint = findPointNear(chartX, chartY);
                if (SwingUtilities.isRightMouseButton(e) && selectedPoint != null) {
                    int indexToRemove = -1;
                    for (int i = 0; i < series.getItemCount(); i++) {
                        XYDataItem item = series.getDataItem(i);
                        if (item.equals(selectedPoint)) {
                            indexToRemove = i;
                            break;
                        }
                    }

                    if (indexToRemove != -1) {
                        series.remove(indexToRemove);
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = false;
                }
            }
        });

        chartPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && isDragging && selectedPoint != null) {
                    int x = e.getX();
                    int y = e.getY();
                    Rectangle2D dataArea = chartPanel.getScreenDataArea();
                    XYPlot plot = (XYPlot) chart.getPlot();
                    double chartX = plot.getDomainAxis().java2DToValue(x, dataArea, plot.getDomainAxisEdge());
                    double chartY = plot.getRangeAxis().java2DToValue(y, dataArea, plot.getRangeAxisEdge());

                    // Limit the noise values between -1 and 1
                    chartX = Math.max(-1, Math.min(1, chartX));
                    // Limit the height values between 0 and 319
                    chartY = Math.max(0, Math.min(319, chartY));

                    // Find the index of the selected point and remove it from the series
                    int index = -1;
                    for (int i = 0; i < series.getItemCount(); i++) {
                        if (series.getDataItem(i).equals(selectedPoint)) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        series.remove(index);
                    }

                    // Add a new point with the updated X and Y values
                    selectedPoint = new XYDataItem(chartX, chartY);
                    series.add(selectedPoint);

                    chartPanel.repaint();
                }
            }
        });

        setContentPane(chartPanel);
    }

    private JFreeChart createChart(XYSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Line Point Graph",
                "Noise Value",
                "Height",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);

        // Set the range of the x axis (noise values) to -1 to 1
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(-1, 1);

        // Set the range of the y axis (height values) to 0 to 319
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0, 319);

        return chart;
    }

    private XYDataItem findPointNear(double chartX, double chartY) {
        XYPlot plot = chart.getXYPlot();
        Rectangle2D dataArea = chartPanel.getScreenDataArea();

        for (int i = 0; i < series.getItemCount(); i++) {
            XYDataItem item = series.getDataItem(i);
            double itemChartX = item.getXValue();
            double itemChartY = item.getYValue();

            // Convert chart coordinates to pixel coordinates
            double itemPixelX = plot.getDomainAxis().valueToJava2D(itemChartX, dataArea, plot.getDomainAxisEdge());
            double itemPixelY = plot.getRangeAxis().valueToJava2D(itemChartY, dataArea, plot.getRangeAxisEdge());

            // Convert input chart coordinates to pixel coordinates
            double pixelX = plot.getDomainAxis().valueToJava2D(chartX, dataArea, plot.getDomainAxisEdge());
            double pixelY = plot.getRangeAxis().valueToJava2D(chartY, dataArea, plot.getRangeAxisEdge());

            // Check if the point is within 10 pixels of the cursor
            if (Math.abs(pixelX - itemPixelX) < 10 && Math.abs(pixelY - itemPixelY) < 10) {
                return item;
            }
        }
        return null;
    }

    public void exportPoints() {
        try (PrintWriter writer = new PrintWriter("linear-points.txt")) {
            writer.print("double[] ContinentalValues = new double[]{");
            for (int i = 0; i < series.getItemCount(); i++) {
                XYDataItem item = series.getDataItem(i);
                double x = item.getXValue();
                writer.print(String.format("%.2f", x));
                if (i < series.getItemCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println("};");

            writer.print("double[] ContinentalHeight = new double[]{");
            for (int i = 0; i < series.getItemCount(); i++) {
                XYDataItem item = series.getDataItem(i);
                double y = item.getYValue();
                writer.print(Math.round(y)); // Round the y value to the nearest integer
                if (i < series.getItemCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println("};");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LinearGraph demo = new LinearGraph();
            demo.pack();
            demo.setLocationRelativeTo(null);
            demo.setVisible(true);
        });
    }
}
