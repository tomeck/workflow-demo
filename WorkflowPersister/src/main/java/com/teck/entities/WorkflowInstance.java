package com.teck.entities;

import com.couchbase.client.java.repository.annotation.Field;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WorkflowInstance {

    @NotNull
    @Id
    private String id;

    @NotNull
    @Field
    private String correlationId;

    //@Field
    //private List<String> phoneNumbers = new ArrayList<>();

}
