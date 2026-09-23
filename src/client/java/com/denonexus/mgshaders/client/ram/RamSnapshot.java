package com.denonexus.mgshaders.client.ram;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Leitura precisa de memória do processo via /proc.
 * Fontes: statm (RSS), status (HWM/Swap/Threads), smaps_rollup (PSS/Shared/Private),
 *         meminfo (sistema), cgroup v1/v2 (limite do Android).
 */
public final class RamSnapshot {
    public long rssKb, hwmKb, swapKb, vmDataKb;
    public long pssKb, sharedKb, privateKb;
    public long sysMemTotalKb, sysMemFreeKb, sysMemAvailableKb;
    public long sysSwapTotalKb, sysSwapFreeKb;
    public long cgroupLimitKb, cgroupUsageKb;
    public int  threads;

    public boolean hasPss()    { return pssKb > 0; }
    public boolean hasCgroup() { return cgroupLimitKb > 0; }
    public boolean hasHwm()    { return hwmKb > 0; }

    public static RamSnapshot read() {
        RamSnapshot s = new RamSnapshot();
        readStatm(s); readStatus(s); readSmapsRollup(s); readMeminfo(s); readCgroup(s);
        return s;
    }

    private static void readStatm(RamSnapshot s) {
        try {
            String[] p = Files.readString(Path.of("/proc/self/statm")).trim().split("\\s+");
            if (p.length >= 2) s.rssKb = Long.parseLong(p[1]) * 4096L / 1024L;
        } catch (Throwable ignored) {}
    }

    private static void readStatus(RamSnapshot s) {
        try {
            for (String ln : Files.readAllLines(Path.of("/proc/self/status"))) {
                if      (ln.startsWith("VmRSS:"))   s.rssKb    = kb(ln);
                else if (ln.startsWith("VmHWM:"))   s.hwmKb    = kb(ln);
                else if (ln.startsWith("VmSwap:"))  s.swapKb   = kb(ln);
                else if (ln.startsWith("VmData:"))  s.vmDataKb = kb(ln);
                else if (ln.startsWith("Threads:")) s.threads  = (int) kb(ln);
            }
        } catch (Throwable ignored) {}
    }

    private static void readSmapsRollup(RamSnapshot s) {
        Path p = Path.of("/proc/self/smaps_rollup");
        if (!Files.exists(p)) return;
        try {
            for (String ln : Files.readAllLines(p)) {
                if      (ln.startsWith("Pss:"))            s.pssKb    = kb(ln);
                else if (ln.startsWith("Shared_Clean:"))   s.sharedKb += kb(ln);
                else if (ln.startsWith("Shared_Dirty:"))   s.sharedKb += kb(ln);
                else if (ln.startsWith("Private_Clean:"))  s.privateKb += kb(ln);
                else if (ln.startsWith("Private_Dirty:"))  s.privateKb += kb(ln);
            }
        } catch (Throwable ignored) {}
    }

    private static void readMeminfo(RamSnapshot s) {
        try {
            for (String ln : Files.readAllLines(Path.of("/proc/meminfo"))) {
                if      (ln.startsWith("MemTotal:"))     s.sysMemTotalKb     = kb(ln);
                else if (ln.startsWith("MemFree:"))      s.sysMemFreeKb      = kb(ln);
                else if (ln.startsWith("MemAvailable:")) s.sysMemAvailableKb = kb(ln);
                else if (ln.startsWith("SwapTotal:"))    s.sysSwapTotalKb    = kb(ln);
                else if (ln.startsWith("SwapFree:"))     s.sysSwapFreeKb     = kb(ln);
            }
        } catch (Throwable ignored) {}
    }

    private interface LongSetter { void set(long bytes); }

    private static void readCgroup(RamSnapshot s) {
        // cgroup v2 (Android 11+)
        tryLong("/sys/fs/cgroup/memory.max",     v -> s.cgroupLimitKb = v);
        tryLong("/sys/fs/cgroup/memory.current", v -> s.cgroupUsageKb = v);
        // cgroup v1 (Android 10-)
        if (s.cgroupLimitKb == 0) {
            tryLong("/sys/fs/cgroup/memory/memory.limit_in_bytes", v -> s.cgroupLimitKb = v);
            tryLong("/sys/fs/cgroup/memory/memory.usage_in_bytes", v -> s.cgroupUsageKb = v);
        }
    }

    private static void tryLong(String path, LongSetter set) {
        try {
            String t = Files.readString(Path.of(path)).trim();
            if (t.isEmpty() || t.equals("max")) return;
            long b = Long.parseLong(t);
            if (b > 0) set.set(b / 1024L);
        } catch (Throwable ignored) {}
    }

    private static long kb(String ln) {
        try { return Long.parseLong(ln.trim().split("\\s+")[1]); }
        catch (Throwable t) { return 0; }
    }

    public String shortSummary() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("rss=").append(rssKb / 1024).append("M");
        if (hasPss())    sb.append(" pss=").append(pssKb / 1024).append("M");
        if (hasHwm())    sb.append(" hwm=").append(hwmKb / 1024).append("M");
        if (swapKb > 0)  sb.append(" swap=").append(swapKb / 1024).append("M");
        if (hasCgroup()) sb.append(" cg=").append(cgroupUsageKb / 1024)
                            .append("/").append(cgroupLimitKb / 1024).append("M");
        if (sysMemAvailableKb > 0)
            sb.append(" sysAvail=").append(sysMemAvailableKb / 1024).append("M");
        return sb.toString();
    }
}
