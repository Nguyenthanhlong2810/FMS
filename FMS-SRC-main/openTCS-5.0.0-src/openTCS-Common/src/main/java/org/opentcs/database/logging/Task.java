package org.opentcs.database.logging;

import java.util.List;
import java.util.concurrent.Callable;

//quan li danh sach doi tuong log can save vao Db
public abstract class Task<E> implements Callable<Integer> {
    private List<E> items;
    public void setItems(List<E> items){
        this.items = items;
    }
    public List<E> getItems(){
        return items;
    }
}
