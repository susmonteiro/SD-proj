package pt.tecnico.rec.frontend;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceLogger {
    /* Maps can be non-concorrent because they are only accessed with a key atomicly calculated */
    private Map<Integer, Long> readTimes = new HashMap<Integer, Long>();
    private Map<Integer, Long> writeTimes = new HashMap<Integer, Long>();    
    private AtomicInteger readIdx = new AtomicInteger();
    private AtomicInteger writeIdx = new AtomicInteger();

    public int startRead() {
        int idx = readIdx.incrementAndGet();
        readTimes.put(idx, System.currentTimeMillis());
        return idx;
    }

    public void stopRead(int idx) {
        long start = readTimes.get(idx);
        long diff = System.currentTimeMillis() - start;
        readTimes.put(idx, diff);
    }

    public int startWrite() {
        int idx = writeIdx.incrementAndGet();
        writeTimes.put(idx, System.currentTimeMillis());
        return idx;
    }

    public void stopWrite(int idx) {
        long start = writeTimes.get(idx);
        long diff = System.currentTimeMillis() - start;
        writeTimes.put(idx, diff);
    }

    public String computeResults() {
        String results = "\n$$$\n\tPerformance Logger Results:\n$$$\n\n";
        
        /* First Removal - Uniform testing (remove init weight) */
        readTimes.remove(1);
        writeTimes.remove(1);

        /* Compute Read */
        String readLog = "\t**Reads**\n" + "Number of reads: " + readTimes.size() + "\n";
        String readValues = "Values: [";
        double sumAllReads = 0;
        for (Map.Entry<Integer, Long> entry : readTimes.entrySet()) {
            // double time = entry.getValue()/(1000);
            long time = entry.getValue();
            sumAllReads += time;
            readValues += time + ", ";
        }
        readValues.substring(0, readValues.length() - 2);   // remove ", " from end
        readLog += "Average time taken: " + (sumAllReads/readTimes.size()) + "\n";
        readLog += readValues;

        /* Compute Write */
        String writeLog = "\t**Writes**\n" + "Number of writes: " + writeTimes.size() + "\n";
        String writeValues = "Values: [";
        double sumAllWrites = 0;
        for (Map.Entry<Integer, Long> entry : writeTimes.entrySet()) {
            // double time = entry.getValue()/(1000);
            long time = entry.getValue();
            sumAllWrites += time;
            writeValues += time + ", ";
        }
        writeValues.substring(0, writeValues.length() - 2);   // remove ", " from end
        writeLog += "Average time taken: " + (sumAllWrites/writeTimes.size()) + "\n";
        writeLog += writeValues;

        results = results + readLog + "\n" + writeLog + "\n$=$\n\tPerformance Logger Results.\n$=$\n";
        return results;
    }
}
