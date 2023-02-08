package com.schedule.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.util.PGobject;

import com.schedule.model.TrainType.DriveTimeEstEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;



/**
 *
 * @author Jia Li
 */
@Converter
public class DriveTimeEstimationConverter implements AttributeConverter<DriveTimeEstEnum, Object>{

    @Override
    public PGobject convertToDatabaseColumn(DriveTimeEstEnum x) {
        PGobject object = new PGobject();
        object.setType("drivetimeestimation");
        
        try {
            object.setValue(x.toString());
        } catch (SQLException ex) {
            Logger.getLogger(DriveTimeEstimationConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }


    @Override
    public DriveTimeEstEnum convertToEntityAttribute(Object y) {
        switch (y.toString()) {
            case "nostatistics":
                return DriveTimeEstEnum.nostatistics;
            case "onlystatistics":
                return DriveTimeEstEnum.onlystatistics;
            case "validstatistics":
                return DriveTimeEstEnum.validstatistics;
            default:
                return null;
        }
    }    
}

