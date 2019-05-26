package org.dominokit.domino.rest.shared.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

public class ArrayResponse<T> implements ResponseBean {

    private T[] items;

    public ArrayResponse() {
    }

    public ArrayResponse(T[] items) {
        this.items = items;
    }

    public T[] getItems() {
        return items;
    }

    public void setItems(T[] items) {
        this.items = items;
    }

    public List<T> asList(){
        if(nonNull(items)){
            return Arrays.asList(items);
        }else{
            return new ArrayList<>();
        }
    }
}
