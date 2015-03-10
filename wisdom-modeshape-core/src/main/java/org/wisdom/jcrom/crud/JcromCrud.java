package org.wisdom.jcrom.crud;

import org.wisdom.api.model.Crud;

import java.io.Serializable;
import java.util.List;

/**
 * Created by antoine on 14/07/2014.
 */
public interface JcromCrud<T, I extends Serializable> extends Crud<T, I> {

    public T findOneByQuery(String query);

    public List<T> findByQuery(String query);

}
