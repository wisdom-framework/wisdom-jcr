package org.wisdom.jcrom.runtime;

import com.dooapp.cloud.common.model.AbstractEntity;
import org.jcrom.JcrMappingException;
import org.jcrom.annotations.JcrNode;
import org.jcrom.dao.AbstractJcrDAO;
import org.jcrom.util.NodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.model.EntityFilter;
import org.wisdom.api.model.FluentTransaction;
import org.wisdom.api.model.Repository;
import org.wisdom.api.model.TransactionManager;
import org.wisdom.jcrom.crud.JcromCrud;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by antoine on 14/07/2014.
 */
public class JcromCrudService<T> implements JcromCrud<T, String> {

    private Logger logger = LoggerFactory.getLogger(JcromCrudService.class);

    protected final JcrRepository repository;

    protected Class<T> entityClass;

    protected AbstractJcrDAO<T> dao;


    /**
     * Flag used in order to know if the instance is used during a transaction in the current thread.
     */
    private static final ThreadLocal<Boolean> transaction = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    protected JcromCrudService(JcrRepository repository, Class<T> entityClass) throws RepositoryException {
        this(repository);
        this.entityClass = entityClass;
        dao = new AbstractJcrDAO<T>(entityClass, repository.getSession(), repository.getJcrom()) {
        };
    }

    protected JcromCrudService(JcrRepository repository) {
        this.repository = repository;
        transaction.set(false);
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public Class<String> getIdClass() {
        return String.class;
    }

    @Override
    public T delete(T t) {
        dao.remove(repository.getJcrom().getPath(t));
        return t;
    }

    @Override
    public void delete(String s) {
        dao.remove(s);
    }

    @Override
    public Iterable<T> delete(Iterable<T> ts) {
        return null;
    }

    @Override
    public T save(T t) {
        String path = repository.getJcrom().getPath(t);
        if (path != null) {
            if (exists(path)) {
                return dao.update(t);
            }
        }
        return dao.create(t);
    }

    @Override
    public Iterable<T> save(Iterable<T> ts) {
        return null;
    }

    @Override
    public T findOne(String name) {
        String nodeType = entityClass.getAnnotation(JcrNode.class).nodeType();
        QueryResult r = executeQuery(findOneQuery(nodeType, name));
        try {
            return readResult(r.getRows(), null).get(0);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return null;
    }

    @Override
    public T findOne(EntityFilter<T> tEntityFilter) {
        return null;
    }

    @Override
    public boolean exists(String s) {
        return dao.exists(s);
    }

    @Override
    public Iterable<T> findAll() {
        String nodeType = entityClass.getAnnotation(JcrNode.class).nodeType();
        QueryResult r = executeQuery(findAllQuery(nodeType));
        try {
            return readResult(r.getRows(), null);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    protected T getEntity(String path, NodeFilter filter) {
        return (T) dao.get(path, filter);
    }

    @Override
    public Iterable<T> findAll(Iterable<String> strings) {
        return null;
    }

    @Override
    public Iterable<T> findAll(EntityFilter<T> tEntityFilter) {
        return null;
    }

    @Override
    public long count() {
        return dao.findAll(entityClass.getSimpleName()).size();
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public void executeTransactionalBlock(Runnable runnable) {

    }

    @Override
    public <A> A executeTransactionalBlock(Callable<A> aCallable) {
        return null;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return null;
    }

    @Override
    public <R> FluentTransaction<R> transaction() {
        return null;
    }

    @Override
    public <R> FluentTransaction<R>.Intermediate transaction(Callable<R> callable) {
        return null;
    }

    @Override
    public T findOneByQuery(String query) {
        QueryResult r = executeQuery(query);
        try {
            return readResult(r.getRows(), null).get(0);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return null;
    }

    @Override
    public List<T> findByQuery(String query) {
        QueryResult r = executeQuery(query);
        try {
            return readResult(r.getRows(), null);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return null;
    }

    public static final String findAllQuery(String nodeType) {
        return "SELECT [jcr:path] FROM [" + nodeType + "]";
    }

    public static final String findOneQuery(String nodeType, String id) {
        return "SELECT * FROM [" + nodeType + "] WHERE [jcr:name] =" + "'" + id + "'";
    }

    protected QueryResult executeQuery(String querry) {
        try {
            javax.jcr.query.QueryManager queryManager = repository.getSession().getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(querry, Query.JCR_SQL2);
            return query.execute();
        } catch (RepositoryException e) {
            throw new JcrMappingException("Could not find nodes by SQL", e);
        }
    }

    protected List<T> readResult(RowIterator rowIterator, NodeFilter filter) throws RepositoryException {
        List<T> list = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            String path = row.getValue("jcr:path").getString();
            T newInstance = getEntity(path, filter);
            list.add(newInstance);
        }
        return list;
    }
}
