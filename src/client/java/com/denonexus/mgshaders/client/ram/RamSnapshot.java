package com.denonexus.mgshaders.client.ram;

/** Leitura de /proc/self — sem chamada nativa, funciona em bionic. */
public final class RamSnapshot {
    public long rssKb;
    public long pssKb;
    public long sharedKb;
    public long privateKb;
    public long swapKb;
    public long vmSizeKb;
    public long vmDataKb;
    public long threads;

    public long rssKb()   { return rssKb; }
    public long pssKb()   { return pssKb; }
    public long swapKb()  { return swapKb; }

    public static RamSnapshot read() {
        RamSnapshot s = new RamSnapshot();
        readStatm(s);
        readStatus(s);
        return s;
    }

    /** /proc/self/statm — 7 campos em páginas. */
    private static void readStatm(RamSnapshot s) {
        try (var br = java.nio.file.Files.newBufferedReader(
                java.nio.file.Path.of("/proc/self/statm"))) {
            String line = br.readLine();
            if (line == null) return;
            String[] p = line.trim().split("\\s+");
            if (p.length < 2) return;
            long pageSize = 4096L;   // aarch64 Android
            s.vmSizeKb = Long.parseLong(p[0]) * pageSize / 1024;
            s.rssKb    = Long.parseLong(p[1]) * pageSize / 1024;
        } catch (Throwable ignored) {}
    }

    /** /proc/self/status — linhas VmRSS, VmSwap, VmData, Threads. */
    private static void readStatus(RamSnapshot s) {
        try (var br = java.nio.file.Files.newBufferedReader(
                java.nio.file.Path.of("/proc/self/status"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("VmRSS:"))       s.rssKb    = kbValue(line);
                else if (line.startsWith("VmSwap:")) s.swapKb   = kbValue(line);
                else if (line.startsWith("VmData:")) s.vmDataKb = kbValue(line);
                else if (line.startsWith("Threads:")) s.threads = kbValue(line);
            }
        } catch (Throwable ignored) {}
    }

    private static long kbValue(String line) {
        try {
            String[] p = line.trim().split("\\s+");
            return Long.parseLong(p[1]);
        } catch (Throwable t) { return 0; }
    }
}
