package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    private String name;
    private String type;
    private int size;
    private Data data;
    private Student student;
    public enum Status {PreTrained, Training, Trained, Tested};
    private Status status;
    public enum Result {None, Good, Bad};
    private Result result;
    private boolean trained;
    private boolean tested;
    private boolean published;

    public Model(String name, Student student){
        this.name = name;
        this.type = type;
        this.size = size;
        this.data = new Data(Data.Type.Images, size);
        if (type == "Tabular"){
            data.setType(Data.Type.Tabular);
        }
        else if (type == "Text"){
            data.setType(Data.Type.Text);
        }
        else{//the initialization is for images, so no need to change.

        }
        this.student = student;
        status = Status.PreTrained;
        result = Result.None;
        trained = false;
        tested = false;
        published = false;
    }
    public void initBolleans(){
        this.trained = false;
        this.tested = false;
        this.published = false;
    }

    public boolean isTrained() {
        return trained;
    }

    public boolean isTested() {
        return tested;
    }

    public boolean isPublished() {
        return published;
    }

    public void changeToTrained() {
        this.trained = true;
    }

    public void changeToTested() {
        this.tested = true;
    }

    public void changeToPublished() {
        this.published = true;
    }

    public void setStudent(Student student){
        this.student = student;
    }
    public void setData(Data data) {
        this.data = data;
    }
    public int getSize(){
        return size;
    }
    public String getType(){
        return type;
    }
    public Student getStudent(){
        return student;
    }
    public Data getData(){
        return data;
    }
    public Status getStatus(){
        return status;
    }
    public void setStatus(Status newStatus){
        this.status = newStatus;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result newResult) {
        this.result = newResult;
    }

    public void incrementModelsProcessedData(){
        data.incrementProcessedData();
        if (data.isDone()){
            status = Status.Trained;
        }
    }

    public String getName() {
        return name;
    }
    public String toString(){
        String toStr = "";

        toStr += "\"name\": \"" + name + "\",\n";
        toStr += "\t\t\t\t\t\"data\": {\n" + data.toString() + "\t\t\t\t\t},\n";
        toStr += "\t\t\t\t\t\"status\": \"" + status + "\",\n";
        toStr += "\t\t\t\t\t\"results\": \"" + result + "\"";

        return toStr;
    }
}
