package bgu.spl.mics.application.objects;

import java.util.List;

public class Input {
    private List<Student> Students;
    private List<String> GPUS;
    private List<Integer> CPUS;
    private List<ConfrenceInformation> Conferences;
    private int TickTime;
    private int Duration;

    public List<Student> getStudents() {
        return Students;
    }

    public List<ConfrenceInformation> getConf() {
        return Conferences;
    }

    public List<String> getGpus() {
        return GPUS;
    }

    public List<Integer> getCpus() {
        return CPUS;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }
}
