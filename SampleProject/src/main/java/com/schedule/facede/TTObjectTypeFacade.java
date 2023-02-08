package com.schedule.facede;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.schedule.base.facade.AbstractFacade;
import com.schedule.model.TTObjectType;

/**
 *
 * @author Jia Li
 */
@Stateless
public class TTObjectTypeFacade extends AbstractFacade<TTObjectType> {
    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TTObjectTypeFacade() {
        super(TTObjectType.class);
    }
    
}
