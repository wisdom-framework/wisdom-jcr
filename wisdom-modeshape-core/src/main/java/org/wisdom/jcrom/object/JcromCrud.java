package org.wisdom.jcrom.object;

import org.wisdom.api.model.Crud;

import java.io.Serializable;
import java.util.List;

/**
 * A crud which add some functionnality to the basic crud
 */
public interface JcromCrud<T, I extends Serializable> extends Crud<T, I> {

    public T findOneByQuery(String query);

    public List<T> findByQuery(String query);

}
