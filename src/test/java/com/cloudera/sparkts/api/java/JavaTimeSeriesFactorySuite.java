package com.cloudera.sparkts.api.java;

import com.cloudera.sparkts.*;
import org.joda.time.DateTime;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class JavaTimeSeriesFactorySuite {
    @Test
    public void testTimeSeriesFromIrregularSamples() {
        DateTime dt = new DateTime("2015-4-8");
        List<Tuple2<DateTime, double[]>> samples = new ArrayList<>();
        samples.add(new Tuple2<>(dt, new double[]{1.0, 2.0, 3.0}));
        samples.add(new Tuple2<>(dt.plusDays(1), new double[]{4.0, 5.0, 6.0}));
        samples.add(new Tuple2<>(dt.plusDays(2), new double[]{7.0, 8.0, 9.0}));
        samples.add(new Tuple2<>(dt.plusDays(4), new double[]{10.0, 11.0, 12.0}));

        String[] labels = new String[]{"a", "b", "c", "d"};
        JavaTimeSeries<String> ts = JavaTimeSeriesFactory.timeSeriesFromIrregularSamples(
                samples, labels, String.class);
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d},
                ts.dataAsArray(), 0);
    }

    @Test
    public void testLagsIncludingOriginals() {
        UniformDateTimeIndex originalIndex = new UniformDateTimeIndex(0, 5, new DayFrequency(1));

        List<double[]> samples = new ArrayList<>();
        samples.add(new double[] { 1.0, 6.0 });
        samples.add(new double[] { 2.0, 7.0 });
        samples.add(new double[] { 3.0, 8.0 });
        samples.add(new double[] { 4.0, 9.0 });
        samples.add(new double[] { 5.0, 10.0 });

        JavaTimeSeries<String> originalTimeSeries = JavaTimeSeriesFactory.timeSeriesFromUniformSamples(
                samples, originalIndex, new String[] { "a", "b" }, String.class);

        JavaTimeSeries<String> laggedTimeSeries = originalTimeSeries.lags(2, true,
                (k, i) -> "lag" + i + "(" + k + ")");

        assertArrayEquals(new String[] { "a", "lag1(a)", "lag2(a)", "b", "lag1(b)", "lag2(b)" },
                (String[]) laggedTimeSeries.keys());
        assertEquals(3, laggedTimeSeries.index().size());

        assertArrayEquals(new double[] { 3.0, 2.0, 1.0, 8.0, 7.0, 6.0,
                        4.0, 3.0, 2.0, 9.0, 8.0, 7.0,
                        5.0, 4.0, 3.0, 10.0, 9.0, 8.0},
                laggedTimeSeries.dataAsArray(), 0);
    }

    @Test
    public void testLagsExcludingOriginals() {
        UniformDateTimeIndex originalIndex = new UniformDateTimeIndex(0, 5, new DayFrequency(1));

        List<double[]> samples = new ArrayList<>();
        samples.add(new double[] { 1.0, 6.0 });
        samples.add(new double[] { 2.0, 7.0 });
        samples.add(new double[] { 3.0, 8.0 });
        samples.add(new double[] { 4.0, 9.0 });
        samples.add(new double[] { 5.0, 10.0 });

        JavaTimeSeries<String> originalTimeSeries = JavaTimeSeriesFactory.timeSeriesFromUniformSamples(
                samples, originalIndex, new String[] { "a", "b" }, String.class);

        JavaTimeSeries<String> laggedTimeSeries = originalTimeSeries.lags(2, false,
                (k, i) -> "lag" + i + "(" + k + ")");

        assertArrayEquals(new String[] { "lag1(a)", "lag2(a)", "lag1(b)", "lag2(b)" },
                (String[]) laggedTimeSeries.keys());
        assertEquals(3, laggedTimeSeries.index().size());

        assertArrayEquals(new double[]{2.0, 1.0, 7.0, 6.0,
                        3.0, 2.0, 8.0, 7.0,
                        4.0, 3.0, 9.0, 8.0},
                laggedTimeSeries.dataAsArray(), 0);
    }
}
