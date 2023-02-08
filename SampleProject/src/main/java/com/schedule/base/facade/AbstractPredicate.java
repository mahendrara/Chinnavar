package com.schedule.base.facade;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.CriteriaQuery;
/**
 *
 * @author EBIScreen
 */

public abstract class AbstractPredicate<T> {
    abstract public void addPredicate(CriteriaBuilder qb, CriteriaQuery cq, Root<T> p, boolean useOrdering);
}
