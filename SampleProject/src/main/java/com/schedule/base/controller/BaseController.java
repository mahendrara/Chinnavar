package com.schedule.base.controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Iterator;
import java.util.List;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.schedule.base.facade.AbstractFacade;
import com.schedule.base.model.EditableEntity;
import com.schedule.util.PaginationHelper;

/**
 *
 * @author spirttin
 * @param <T>
 */
public abstract class BaseController<T> {

    // Data
    private DataModel<T> items = null; // All items to be shown in 'List'
    private T selected = null; // This is to be used only in 'View'

    // Variables for paging
    protected PaginationHelper<T> pagination = null;
    protected int itemsPerPage = 50;

    public BaseController() {
    }

    public BaseController(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Inherited class facade for pagination use
     *
     * @return
     */
    protected abstract AbstractFacade<T> getFacade();

    /**
     * Creates a new, empty item
     *
     * @return Empty item
     */
    public abstract T constructNewItem();

    /**
     * Creates or returns pagination item
     *
     * @return
     */
    public PaginationHelper<T> getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper<T>(this.itemsPerPage) {
                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel<T> createPageDataModel() {
                    return new ListDataModel<>(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public T getSelected() {
        return selected;
    }

    public String prepareList() {
        selected = null;
        resetPagination();
        return "List";
    }

    public String prepareView(T item) {
        selected = item;
        return "View";
    }

    public String activateEdit(EditableEntity e) {
        e.setEditing(true);
        return null;
    }

    private void addToDataModel(T item) {
        @SuppressWarnings("unchecked")
        List<T> oldArray = (List<T>) getItems().getWrappedData();
        oldArray.add(0, item);
        getItems().setWrappedData(oldArray);
    }

    /**
     * Create empty 'T' and add it to data model, by calling addToDataModel();
     *
     * @return
     */
    public String addNew() {
        addToDataModel(constructNewItem());
        return null;
    }

    public abstract String save(T item);

    public abstract String destroy(T item);

    public String cancel(EditableEntity e) {
        recreateModel();
        return null;
    }

    public DataModel<T> getItems() {
        if (items == null) {
            this.getFacade().evictAll();//Otherwise duty is not shown on webpages
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    /**
     * Sets page selection to first page and clears items
     */
    public void resetPagination() {
        getPagination().setFirstPage();
        recreateModel();
    }

    /**
     * Clears items, that pagination will get new items accordingly to selected
     * page
     */
    protected void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public boolean isAddAllowed() {
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) getItems().getWrappedData();
        if (list == null || list.isEmpty()) {
            return true;
        } else {

            EditableEntity e = (EditableEntity) list.get(0);
            //return (e == null || !e.isCreating());
            if (e != null && e.isCreating()) {
                return false;
            } else {
                Iterator<T> iterator = list.iterator();
                while (iterator.hasNext()) {
                    EditableEntity item = (EditableEntity) iterator.next();
                    if (item.isEditing()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}
