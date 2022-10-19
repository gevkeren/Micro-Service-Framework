package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {
    private Model model;
    private Future<Model> future;

    public TrainModelEvent(Model model, Future<Model> future){

        this.model = model;
        this.future = future;
    }
    public Model getModel(){
        return model;
    }
    public Model getResult(){
        return future.get();
    }

    public void setFuture(Future<Model> future) {
        this.future = future;
    }

}
