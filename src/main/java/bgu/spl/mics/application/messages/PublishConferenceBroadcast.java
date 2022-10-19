package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class PublishConferenceBroadcast implements Broadcast {
    private Model model;
    private Future<Model> future;

    public PublishConferenceBroadcast(Model model, Future<Model> future){
        this.model = model;
        this.future = future;
    }

    public Model getModel(){
        return model;
    }
}
