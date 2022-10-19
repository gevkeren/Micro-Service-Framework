package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static CountDownLatch CDL;

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new FileReader(args[0]);){
//        try (Reader reader = new FileReader("example_input.json");){
//        try (Reader reader = new FileReader("jasonJunior.json");){
            JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);
            String jsonInputString = gson.toJson(jsonElement);
            Input input = new Gson().fromJson(jsonInputString, Input.class);
//            System.out.println(jsonInputString);

            LinkedList<GPU> gpuToInitialize = new LinkedList<>();
            LinkedList<CPU> cpuToInitialize = new LinkedList<>();
            for (Student student : input.getStudents()){
                for (Model model : student.getModels()){
                    Data data;
                    if (model.getType() == "images")
                        data = new Data(Data.Type.Images, model.getSize());
                    else if (model.getType() == "Tabular")
                        data = new Data(Data.Type.Tabular, model.getSize());
                    else{//Text
                        data = new Data(Data.Type.Text, model.getSize());
                    }
                    model.initBolleans();
                    model.setData(data);
                    model.setStudent(student);
                    model.setStatus(Model.Status.PreTrained);
                    model.setResult(Model.Result.None);
                }
                student.initConfs();
                student.setTrainedModels(new LinkedBlockingQueue<>());
                student.setCurrentModel(student.getModels().poll());
            }
            for (String type : input.getGpus()){//creating a list of GPUS
                GPU gpu;
                if (type.equals("GTX1080")) {
                    gpu = new GPU(GPU.Type.GTX1080);
                }
                else if (type.equals("RTX2080")) {
                    gpu = new GPU(GPU.Type.RTX2080);
                }
                else {//RTX3090
                    gpu = new GPU(GPU.Type.RTX3090);
                }
                gpuToInitialize.add(gpu);
            }
            for (int numOfCores : input.getCpus()){//creating a list of CPUS
                CPU cpu = new CPU(numOfCores);
                cpuToInitialize.add(cpu);
            }
            for (ConfrenceInformation conf : input.getConf()){
                conf.createNewPublishResultsEvents();
                conf.setModels(new LinkedBlockingQueue<>());
            }
            ConfrenceInformation[] newConfs = new ConfrenceInformation[input.getConf().size()];
            int i = 0;
            for (ConfrenceInformation conf : input.getConf()){
                newConfs[i] = conf;
                i++;
            }
            for (int a = 0; a < newConfs.length; a++){
                for (int b = a + 1; b < newConfs.length; b++){
                    if (newConfs[b].getDate() < newConfs[a].getDate()){
                        ConfrenceInformation temp = newConfs[a];
                        newConfs[a] = newConfs[b];
                        newConfs[b] = temp;
                    }
                }
            }
//            for (ConfrenceInformation conf : newConfs){
//                System.out.println(conf.getDate());
//            }
            int tickTime = input.getTickTime();
            int duration = input.getDuration();
            LinkedBlockingQueue<ConfrenceInformation> confs = new LinkedBlockingQueue<>();
            for (int j = 0; j < newConfs.length; j++){
                confs.add(newConfs[j]);
            }
            for (Student s : input.getStudents()){
                for (ConfrenceInformation c : confs) {
                    s.addConf(c);
                }
            }
            LinkedList<StudentService> studentServices = new LinkedList<>();
            LinkedList<GPUService> gpuServices = new LinkedList<>();
            LinkedList<CPUService> cpuServices = new LinkedList<>();
            LinkedList<ConferenceService> conferenceServices = new LinkedList<>();

            for (Student student : input.getStudents()){
                StudentService studentService = new StudentService(student);
                studentServices.add(studentService);
            }
            int gpuCounter = 0;
            for (GPU gpu : gpuToInitialize){
                String name = "GPUService" + gpuCounter;
                GPUService gpuService = new GPUService(name, gpu);
                gpu.setGpuService(gpuService);
                gpuServices.add(gpuService);
                gpuCounter++;
            }
            int cpuCounter = 0;
            for (CPU cpu : cpuToInitialize){
                String name = "CPUService" + cpuCounter;
                CPUService cpuService = new CPUService(name, cpu);
                cpuServices.add(cpuService);
                cpuCounter++;
            }
            int confCounter = 0;
            for (ConfrenceInformation confrenceInformation : confs){
                String name = "ConferenceService" + confCounter;
                ConferenceService conferenceService = new ConferenceService(name, confrenceInformation);
                conferenceServices.add(conferenceService);
                confrenceInformation.setService(conferenceService);
                confCounter++;
            }

            LinkedBlockingQueue<Thread> SSThreads = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<Thread> GPUSThreads = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<Thread> CPUSThreads = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<Thread> CSThreads = new LinkedBlockingQueue<>();

            TimeService timeService = new TimeService(duration, tickTime);
            int gpuConfCount = 0;
            Thread timeThread = new Thread(timeService);
//            System.out.println(timeThread.getName() + " is Time Thread");
            for(StudentService studentService : studentServices){
                Thread thread = new Thread(studentService);
                SSThreads.add(thread);
//                System.out.println(thread.getName() + " is Student Thread");
            }
            for(GPUService gpuService : gpuServices){
                Thread thread = new Thread(gpuService);
                GPUSThreads.add(thread);
//                System.out.println(thread.getName() + " is gpu Thread");
                gpuConfCount++;
            }
            for(CPUService cpuService : cpuServices){
                Thread thread = new Thread(cpuService);
                CPUSThreads.add(thread);
//                System.out.println(thread.getName() + " is cpu Thread");
            }
            for(ConferenceService conferenceService : conferenceServices){
                Thread thread = new Thread(conferenceService);
                CSThreads.add(thread);
//                System.out.println(thread.getName() + " is conferenceService Thread");
                gpuConfCount++;
            }
            CDL = new CountDownLatch(gpuConfCount);


            for(Thread t : GPUSThreads){
                t.start();
            }
            for(Thread t : CPUSThreads){
                t.start();
            }
            for(Thread t : CSThreads){
                t.start();
            }
            try{
                CDL.await();
            }catch (InterruptedException interrupt){

            }
            for(Thread t : SSThreads){
                t.start();
            }
            timeThread.start();
            timeThread.join();



        //Output File

        Cluster cluster = Cluster.getInstance();

//        File output = new File("/home/spl211/IdeaProjects/assignment2/output_GevIsTheKing");
        File output = new File(args[1]);
        //File output = new File(args[1]);
        FileWriter writer = null;
        try{
            writer = new FileWriter(output);

            //writing the students into the output file
            writer.write("{\n\t\"students\": [");
            int a = input.getStudents().size() - 1;
            for(Student student : input.getStudents()){
                writer.write("\n\t\t{\n\t\t");
                writer.write(student.toString());
                writer.write("\n\t\t}");
                a--;
                if (a > 0)
                    writer.write(",");
            }
            writer.write("\n\t],\n");

            //writing the conferences into the output file
            writer.write("\t\"conferences\": [\n");
            a = confs.size() - 1;
            for(ConfrenceInformation c : confs){
                writer.write("\t\t{\n\t\t");
                writer.write(c.toString());
                writer.write("\n\t\t}");
                a--;
                if( a > 0)
                    writer.write(",");
                writer.write("\n");
            }
            writer.write("\t], \n");

            writer.write("\t\"cpuTimeUsed\": ");
            writer.write(Integer.toString(cluster.getCpuTimeUsed()));
            writer.write(",\n");

            writer.write("\t\"gpuTimeUsed\": ");
            writer.write(Integer.toString(cluster.getGpuTimeUsed()));
            writer.write(",\n");

            writer.write("\t\"batchesProcessed\": ");
            writer.write(Integer.toString(cluster.getTotalBatchesProcessed()));
            writer.write(",\n");

            writer.write("}");

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        } catch (Exception e) {
            System.out.println("File Not Found");
        }
    }
}
