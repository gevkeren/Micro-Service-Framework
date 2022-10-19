package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.services.ConferenceService;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private LinkedBlockingQueue<PublishResultsEvent> publishResultsEvents;
    private LinkedBlockingQueue<Model> models;
    private ConferenceService service;
    public ConfrenceInformation(String name, int date){
        this.name = name;
        this.date = date;
        this.publishResultsEvents = new LinkedBlockingQueue<>();
        models = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<Model> getModels() {
        return models;
    }

    public void setModels(LinkedBlockingQueue<Model> models) {
        this.models = models;
    }

    public LinkedBlockingQueue<PublishResultsEvent> getPublishResultsEvents() {
        return publishResultsEvents;
    }
    public void addPublishResultEvent(PublishResultsEvent publishEvent){
        publishResultsEvents.add(publishEvent);
    }

    public String getName() {
        return name;
    }

    public ConferenceService getService() {
        return service;
    }

    public void setService(ConferenceService service) {
        this.service = service;
    }

    public int getDate() {
        return date;
    }
    public void createNewPublishResultsEvents(){
        this.publishResultsEvents = new LinkedBlockingQueue<>();
    }
    public String toString(){
        String toStr = "\n";
        toStr += "\t\t\t\"name\": \"" + name + "\",\n";
        toStr += "\t\t\t\"date\": \"" + date + "\",\n";
        toStr += "\t\t\t\"publications\": [\n\t\t\t\t";
        if(models.size() > 0){
            Iterator<Model> conferator = models.iterator();
            while(conferator.hasNext()){
                Model model = conferator.next();
                toStr += "{\n\t\t\t";
                toStr += "\t\t" + model.toString() + "\n\t\t\t\t}";
                toStr += ",\n\t\t\t\t";
            }
            toStr = toStr.substring(0, toStr.length() -6);
            toStr += "\n";
        }
        toStr += "\t\t\t]";
        return toStr;
    }
}
