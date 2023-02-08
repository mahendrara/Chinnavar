package com.schedule.controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.schedule.base.facade.AbstractPredicate;
import com.schedule.base.model.Locale;
import com.schedule.model.Locale_;

/**
 *
 * @author Jia Li
 */
public class LocaleFilter extends AbstractPredicate<Locale> {
    
    private String localeCode;
    private boolean localeCodeValid = false;

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate(CriteriaBuilder qb,CriteriaQuery cq,Root<Locale> p,boolean useOrdering) {

        List<Predicate> predicates = new ArrayList<>();
        if(localeCodeValid)
        {
             Path<String> path = p.get(Locale_.localeCode);
            
            predicates.add(qb.equal(path, this.localeCode));
         }
        
        cq.where(qb.and(predicates.toArray(new Predicate[predicates.size()])));
        
        if(useOrdering)
            cq.orderBy(qb.asc(p.get(Locale_.description)));
    }
    public void SetLocaleCodeFilter(String localeCode) {
        this.localeCode = localeCode;
        this.localeCodeValid = true;
    }
    
}
