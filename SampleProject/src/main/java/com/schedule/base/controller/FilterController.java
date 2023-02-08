package com.schedule.base.controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.schedule.base.facade.AbstractPredicate;
import com.schedule.util.PaginationHelper;

/**
 *
 * @author spirttin
 * @param <T> Entity type which is base object for this controller
 * @param <F> Filter type which match to AbstractFacade<T>
 */
public abstract class FilterController<T, F extends AbstractPredicate<T> > extends BaseController<T> {
    // Data
    private F filter = null;
    
    public FilterController() {
        super();
    }

    public FilterController(F filter) {
        super();
        this.filter = filter;
    }
    public FilterController(F filter, int itemsPerPage) {
        super(itemsPerPage);
        this.filter = filter;
    }
    
    public F getFilter() {
        return filter;
    }
    public void setFilter(F filter) {
        this.filter = filter;
    }
    
    /**
     * @return 
     */
    @Override
    public PaginationHelper<T> getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper<T>(this.itemsPerPage) {
                @Override
                public int getItemsCount() {
                    return getFacade().count(filter);
                }

                @Override
                public DataModel<T> createPageDataModel() {
                    return new ListDataModel<>(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}, filter));
                }
            };
        }
        return pagination;
    }

    @Override
    public String prepareList() {
        clearFilters();
        return super.prepareList();
    }
    
    @Override
    public String prepareView(T item) {
        clearFilters();
        return super.prepareView(item);
    }

    /**
     * Called from prepareList/PrepareView
     * This can be overloaded and used to clear filters within controller, that
     * a new page will have a clear selection independent of old selections
     */
    protected void clearFilters() {
    }
    
}
