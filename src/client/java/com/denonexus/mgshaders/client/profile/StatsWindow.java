package com.denonexus.mgshaders.client.profile;

public final class StatsWindow {
    private final int capacity;
    private final double[] samples;
    private int size, index;
    private volatile double current, min, max, avg;

    public StatsWindow(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.samples = new double[this.capacity];
    }

    public synchronized void push(double v) {
        if (!Double.isFinite(v)) return;
        current = v;
        samples[index] = v;
        index = (index + 1) % capacity;
        if (size < capacity) size++;

        double mn = Double.POSITIVE_INFINITY, mx = Double.NEGATIVE_INFINITY, sum = 0;
        for (int i = 0; i < size; i++) {
            double s = samples[i];
            if (s < mn) mn = s;
            if (s > mx) mx = s;
            sum += s;
        }
        min = (mn == Double.POSITIVE_INFINITY) ? 0 : mn;
        max = (mx == Double.NEGATIVE_INFINITY) ? 0 : mx;
        avg = sum / size;
    }

    public double current() { return current; }
    public double min()     { return min; }
    public double max()     { return max; }
    public double avg()     { return avg; }
    public int    samples() { return size; }

    public synchronized void reset() {
        size = index = 0;
        current = min = max = avg = 0;
    }
}
