package org.dominokit.domino.rest;

import org.dominokit.jacksonapt.*;
import org.dominokit.jacksonapt.annotation.JSONReader;
import org.dominokit.jacksonapt.deser.BaseNumberJsonDeserializer;
import org.dominokit.jacksonapt.deser.array.ArrayJsonDeserializer;
import org.dominokit.jacksonapt.ser.BaseNumberJsonSerializer;

public class TestApp {

//    @JSONReader
    public static class Person{
        private Integer[] ids;

        public Integer[] getIds() {
            return ids;
        }

        public void setIds(Integer[] ids) {
            this.ids = ids;
        }
    }



    public ObjectReader<Integer> getReader(){

        new AbstractObjectReader<Integer[]>("Integer[]") {
            @Override
            protected JsonDeserializer<Integer[]> newDeserializer() {
                return ArrayJsonDeserializer.newInstance(BaseNumberJsonDeserializer.IntegerJsonDeserializer.getInstance(), (ArrayJsonDeserializer.ArrayCreator<Integer>) Integer[]::new);
            }
        }.read("");

        new AbstractObjectWriter<Integer>("Integer") {
            @Override
            protected JsonSerializer<Integer> newSerializer() {
                return BaseNumberJsonSerializer.IntegerJsonSerializer.getInstance();
            }
        }.write(10);

        return new AbstractObjectWriter<Integer>("Integer") {
            @Override
            protected JsonSerializer<Integer> newSerializer() {
                return BaseNumberJsonSerializer.IntegerJsonSerializer.getInstance();
            }
        };
    }




}
