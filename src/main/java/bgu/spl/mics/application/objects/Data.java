package bgu.spl.mics.application.objects;

import org.junit.Test;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;
    private GPU gpu; //the specific gpu which handles the data

    public Data(Type type, int size){
        this.type = type;
        this.size = size;
        processed = 0;
        this.gpu = null;
    }
    public Type getType(){
        return type;
    }
    public void setType(Type type){
        this.type = type;
    }
    public int getProcessed(){
        return processed;
    }
    public int getSize(){
        return size;
    }
    public void incrementProcessedData(){
        processed = processed + 1000;
    }
    public boolean isDone(){
        return (processed == size);
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }
    public String toString(){
        String toStr = "";
        toStr += "\t\t\t\t\t\t\"type\": \"" + type + "\",\n";
        toStr += "\t\t\t\t\t\t\"size\": \"" + size + "\",\n";
        return toStr;
    }
}
