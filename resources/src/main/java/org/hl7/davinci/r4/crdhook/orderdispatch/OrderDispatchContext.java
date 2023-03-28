package org.hl7.davinci.r4.crdhook.orderdispatch;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.r4.JacksonHapiSerializer;
import org.hl7.davinci.r4.JacksonIBaseResourceDeserializer;
import org.hl7.davinci.r4.crdhook.ServiceContext;
import org.hl7.fhir.r4.model.*;

import javax.validation.constraints.NotNull;
public class OrderDispatchContext extends ServiceContext {
    @NotNull
    private String order;

    @NotNull
    private String performer;

    @JsonSerialize(using = JacksonHapiSerializer.class)
    @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
    private Task task;

    @Override
    public Bundle getDraftOrders() {
        return null;
    }

    public String getOrder() {
        return order;
    }
    public void setOrder(String order) { this.order = order; }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) { this.performer = performer; }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {this.task = task;}
}
