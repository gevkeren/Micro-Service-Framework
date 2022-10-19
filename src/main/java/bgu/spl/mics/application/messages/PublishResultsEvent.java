package bgu.spl.mics.application.messages;


import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {
    private Model model;
    private Future<Model> future;


    public PublishResultsEvent(Model model, Future<Model> future){
        this.model = model;
        this.future = future;
    }

    public Model getModel(){
        return model;
    }

    public Future<Model> getFuture() {
        return future;
    }
}
