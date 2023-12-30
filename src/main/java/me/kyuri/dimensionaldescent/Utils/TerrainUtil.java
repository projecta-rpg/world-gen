package me.kyuri.dimensionaldescent.Utils;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

public class TerrainUtil {
    private UnivariateFunction heightInterpolator;

    public void setControlPoints(double[] noiseValues, double[] heightValues, boolean isLinear) {
        UnivariateInterpolator interpolator;
        if (isLinear) {
            interpolator = new LinearInterpolator();
        } else {
            interpolator = new SplineInterpolator();
        }
        heightInterpolator = interpolator.interpolate(noiseValues, heightValues);
    }


    public double getTerrainHeight(double noiseValue) {
        return heightInterpolator.value(noiseValue);
    }

    public double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    public double getSmoothTerrainHeight(double noiseValueCurrent, double noiseValueNext, double t) {
        double noiseValueSmooth = lerp(noiseValueCurrent, noiseValueNext, t);
        return getTerrainHeight(noiseValueSmooth);
    }
}
