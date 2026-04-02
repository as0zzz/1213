package dev.mdk2.client.render;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class WatermarkSystemSampler {
    private static final long SYSTEM_SAMPLE_INTERVAL_MS = 1000L;
    private static final long GPU_SAMPLE_INTERVAL_MS = 2500L;

    private final OperatingSystemMXBean operatingSystemBean;
    private final ExecutorService gpuExecutor;

    private volatile int gpuLoad;
    private volatile boolean gpuSampling;
    private long lastSystemSampleAt;
    private long lastGpuSampleAt;
    private int cpuLoad;
    private int memoryLoad;

    WatermarkSystemSampler() {
        final java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        this.operatingSystemBean = bean instanceof OperatingSystemMXBean ? (OperatingSystemMXBean) bean : null;
        this.gpuExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                final Thread thread = new Thread(runnable, "mdk2-watermark-gpu");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    Snapshot snapshot(final boolean includeGpu) {
        final long now = System.currentTimeMillis();
        if (now - this.lastSystemSampleAt >= SYSTEM_SAMPLE_INTERVAL_MS) {
            sampleCpuAndMemory();
            this.lastSystemSampleAt = now;
        }
        if (includeGpu && isWindows() && now - this.lastGpuSampleAt >= GPU_SAMPLE_INTERVAL_MS && !this.gpuSampling) {
            this.lastGpuSampleAt = now;
            this.gpuSampling = true;
            this.gpuExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        gpuLoad = queryGpuLoad();
                    } finally {
                        gpuSampling = false;
                    }
                }
            });
        }
        return new Snapshot(this.cpuLoad, this.memoryLoad, this.gpuLoad);
    }

    private void sampleCpuAndMemory() {
        if (this.operatingSystemBean == null) {
            return;
        }

        final double cpu = this.operatingSystemBean.getSystemCpuLoad();
        if (cpu >= 0.0D) {
            this.cpuLoad = clampPercent((int) Math.round(cpu * 100.0D));
        }

        final long totalMemory = this.operatingSystemBean.getTotalPhysicalMemorySize();
        final long freeMemory = this.operatingSystemBean.getFreePhysicalMemorySize();
        if (totalMemory > 0L) {
            final double usedRatio = 1.0D - (double) freeMemory / (double) totalMemory;
            this.memoryLoad = clampPercent((int) Math.round(usedRatio * 100.0D));
        }
    }

    private int queryGpuLoad() {
        Process process = null;
        try {
            process = new ProcessBuilder(
                "powershell",
                "-NoProfile",
                "-Command",
                "$value = (Get-CimInstance Win32_PerfFormattedData_GPUPerformanceCounters_GPUEngine | " +
                    "Measure-Object -Property UtilizationPercentage -Sum).Sum; " +
                    "if ($null -eq $value) { 0 } else { [int][math]::Round([double]$value) }"
            ).redirectErrorStream(true).start();
            if (!process.waitFor(1200L, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return this.gpuLoad;
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            final StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line.trim());
            }
            final String text = output.toString().replace(',', '.');
            if (text.isEmpty()) {
                return this.gpuLoad;
            }
            return clampPercent((int) Math.round(Double.parseDouble(text)));
        } catch (final Exception ignored) {
            return this.gpuLoad;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static int clampPercent(final int value) {
        return Math.max(0, Math.min(100, value));
    }

    static final class Snapshot {
        final int cpuLoad;
        final int memoryLoad;
        final int gpuLoad;

        Snapshot(final int cpuLoad, final int memoryLoad, final int gpuLoad) {
            this.cpuLoad = cpuLoad;
            this.memoryLoad = memoryLoad;
            this.gpuLoad = gpuLoad;
        }
    }
}
