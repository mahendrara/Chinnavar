package com.schedule.converter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.persistence.Converter;
import org.postgresql.util.PGobject;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Jia Li
 */
@Converter
public class JsonConverter implements javax.persistence.AttributeConverter<JsonObject, Object> {

    
    @Override
    public PGobject convertToDatabaseColumn(JsonObject objectValue) {
        PGobject out = null;
        if(objectValue!=null) {
            out = new PGobject();
            out.setType("jsonb");
            try {
                //out.setValue(mapper.writerWithType( new TypeReference<JsonObject>() {}).writeValueAsString(objectValue));
                //mapper.writerWithType( new TypeReference<JsonObject>() {}).writeValueAsString(objectValue);
                out.setValue(objectValue.toString());
            } catch (SQLException ex) {
                Logger.getLogger(JsonConverter.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
        return out;
    }

    @Override
    public JsonObject convertToEntityAttribute(Object dataValue) {
        JsonObject object = null;
        if (dataValue instanceof PGobject && ((PGobject) dataValue).getType().equals("jsonb")) {

            try (JsonReader jsonReader = Json.createReader(new StringReader(((PGobject) dataValue).getValue()))) {
                object = jsonReader.readObject();
            }
        }
        return object;
    }
}

