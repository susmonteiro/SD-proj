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
    public static final double NANOSECONDS_PER_MILLISECONDS = 1000000;

    public int startRead() {
        int idx = readIdx.incrementAndGet();
        readTimes.put(idx, System.nanoTime());
        return idx;
    }

    public void stopRead(int idx) {
        long time = System.nanoTime();
        long start = readTimes.get(idx);
        long diff = time - start;
        readTimes.put(idx, diff);
    }

    public int startWrite() {
        int idx = writeIdx.incrementAndGet();
        writeTimes.put(idx, System.nanoTime());
        return idx;
    }

    public void stopWrite(int idx) {
        long time = System.nanoTime();
        long start = writeTimes.get(idx);
        long diff = time - start;
        writeTimes.put(idx, diff);
    }

    public String computeResults() {
        String results = "\n$$$\n\tPerformance Logger Results:\n$$$\n\n";
        
        /* First Removal - Uniform testing (remove init weight) */
        readTimes.remove(1);
        writeTimes.remove(1);

        /* Compute Read */
        String readLog = "\t**Reads**\n" + "Number of reads: " + readTimes.size() + "\t" + "(" + ((double)readTimes.size()/((double)readTimes.size() + (double)writeTimes.size()))*100 + "% of all Ops)\n";
        String readValues = "Values: [";
        double sumAllReads = 0;
        for (Map.Entry<Integer, Long> entry : readTimes.entrySet()) {
            // double time = entry.getValue()/(1000);
            double time = toMilliseconds(entry.getValue());
            sumAllReads += time;
            readValues += time + ", ";
        }
        readValues = readValues.substring(0, readValues.length() - 2) + "]";   // remove ", " from end
        double readAvg = (sumAllReads/readTimes.size());
        readLog += "Average time taken: " + readAvg + "\n";
        readLog += readValues;
        
        /* Compute Write */
        String writeLog = "\t**Writes**\n" + "Number of writes: " + writeTimes.size() + "\t" + "(" + ((double)writeTimes.size()/((double)readTimes.size() + (double)writeTimes.size()))*100 + "% of all Ops)\n";
        String writeValues = "Values: [";
        double sumAllWrites = 0;
        for (Map.Entry<Integer, Long> entry : writeTimes.entrySet()) {
            // double time = entry.getValue()/(1000);
            double time = toMilliseconds(entry.getValue());
            sumAllWrites += time;
            writeValues += time + ", ";
        }
        writeValues = writeValues.substring(0, writeValues.length() - 2) + "]";   // remove ", " from end
        double writeAvg = (sumAllWrites/writeTimes.size());
        writeLog += "Average time taken: " + writeAvg + "\n";
        writeLog += writeValues;
        
        results = results + readLog + "\n" + writeLog + "\n" ;
        results += "Total time taken in Ops: " + (sumAllReads + sumAllWrites) + "\n";
        results += "\n$=$\n\tPerformance Logger Results.\n$=$\n";
        return results;
    }

    public static double toMilliseconds(double nanoseconds) {
        return nanoseconds / NANOSECONDS_PER_MILLISECONDS;
    }
}
