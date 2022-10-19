package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.StudentService;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }
    //microServiseStudent -> TrainModel to MB -> gpu.... -> MB -> Student
    private String name;
    private String department;
    private Degree status;
    private LinkedBlockingQueue<Model> models;
    private int publications;
    private int papersRead;
    private Model currentModel;
    private StudentService studentService;
    private MessageBusImpl MB = MessageBusImpl.getInstance();
    private LinkedBlockingQueue <ConfrenceInformation> confs;
    private LinkedBlockingQueue <String> trainedModels;

    public Student(String name, String department, Degree status){
        this.name = name;
        this.department = department;
        this.status = status;
        this.publications = 0;
        this.papersRead = 0;
        models = new LinkedBlockingQueue<Model>();
        currentModel = null;
        studentService = new StudentService(this);
        MB = MessageBusImpl.getInstance();
    }
    public void initConfs(){
        confs = new LinkedBlockingQueue<>();
    }
    public void addConf(ConfrenceInformation c){
        if (c != null) {
            confs.add(c);
        }
    }
    public LinkedBlockingQueue <ConfrenceInformation> getConfs(){
        return confs;
    }
    public MessageBusImpl getMB() {
        return MB;
    }

    public void addModel(Model model){

    }
    public LinkedBlockingQueue<Model> getModels(){
        return models;
    }

    public Model getFirstUntrainedModel() throws InterruptedException {
        Model model = models.poll();
        return model;
    }
    public void start(){

    };
    public String getName() {
        return name;
    }
    public Degree getStatus(){
        return status;
    }

    //it will create a train model event, and will send it to the messageBus
    public void sendEvent(){

    }
    public void createEvent(Model model){

    }
    public void subscribeTrainModelEvent() throws InterruptedException {

    }

    public Model getCurrentModel() {
        return currentModel;
    }

    public void setNextCurrentModel() throws InterruptedException {
        this.currentModel = getFirstUntrainedModel();
    }
    public void incrementPublications(){
        publications++;
    }
    public void incrementPapersRead(){
        papersRead++;
    }
    public void setCurrentModel(Model model){
        this.currentModel = model;
    }

    public void setTrainedModels(LinkedBlockingQueue<String> trainedModels) {
        this.trainedModels = trainedModels;
    }

    public LinkedBlockingQueue<String> getTrainedModels() {
        return trainedModels;
    }

    public String toString(){
        String toStr = "";

        toStr += "\t\"name\": \"" + name + "\",\n";
        toStr += "\t\t\t\"department\": \"" + department + "\",\n";
        toStr += "\t\t\t\"status\": \"" +status + "\",\n";
        toStr += "\t\t\t\"publications\": " +publications + ",\n";
        toStr += "\t\t\t\"papersRead\": " + papersRead + ",\n";
        toStr += "\t\t\t\"trainedModels\": [\n\t\t\t\t";


        if(trainedModels != null && trainedModels.size() > 0){
            Iterator<String> moderator = trainedModels.iterator();

            while (moderator.hasNext()){
                String modelName = moderator.next();
                toStr += "{\n\t\t\t";
                toStr += "\t\t" + modelName.toString() + "\n\t\t\t\t}";
                toStr += ",\n\t\t\t\t";
            }
            toStr = toStr.substring(0, toStr.length() -6);
            toStr += "\n";
        }
        toStr += "\t\t\t]\n\t\t";

        return toStr;
    }
}
