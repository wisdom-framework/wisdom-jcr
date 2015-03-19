/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.jcrom.runtime;

import org.jcrom.JcrMappingException;
import org.jcrom.annotations.JcrNode;
import org.jcrom.dao.AbstractJcrDAO;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.model.EntityFilter;
import org.wisdom.api.model.FluentTransaction;
import org.wisdom.api.model.Repository;
import org.wisdom.api.model.TransactionManager;
import org.wisdom.jcrom.object.JcrCrud;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.wisdom.jcrom.runtime.JcrQueryFactory.findAllQuery;
import static org.wisdom.jcrom.runtime.JcrQueryFactory.findOneQuery;

/**
 * CRUD Service Implementation using Jcrom
 */
public class JcrCrudService<T> implements JcrCrud<T, String> {

    private Logger logger = LoggerFactory.getLogger(JcrCrudService.class);

    protected final JcrRepository repository;

    protected Class<T> entityClass;

    protected String nodeType;

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

    protected JcrCrudService(JcrRepository repository, Class<T> entityClass) throws RepositoryException {
        this(repository);
        this.entityClass = entityClass;
        dao = new AbstractJcrDAO<T>(entityClass, repository.getSession(), repository.getJcrom()) {
        };
        JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(entityClass);
        if (jcrNode != null) {
            nodeType = jcrNode.nodeType();
        }
        if (nodeType == null) {
            throw new JcrMappingException("Can not use JcrCrudService on a class with no node type, please annotate the class " + entityClass + " with the JcrNode annotations and specify its nodeType");
        }
    }

    protected JcrCrudService(JcrRepository repository) {
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
        List<T> deleted = new ArrayList<>();

        for (T toDelete : ts) {
            deleted.add(delete(toDelete));
        }
        return deleted;
    }

    @Override
    public T save(T t) {
        String path = repository.getJcrom().getPath(t);
        String name = repository.getJcrom().getName(t);
        if (path != null) {
            if (exists(name)) {
                return dao.update(t);
            }
        }
        return dao.create(t);
    }

    @Override
    public Iterable<T> save(Iterable<T> ts) {
        List<T> saved = new ArrayList<>();

        for (T tosave : ts) {
            saved.add(save(tosave));
        }
        return saved;
    }

    @Override
    public T findOne(String name) {
        QueryResult r = executeJcrSql2Query(findOneQuery(nodeType, name));
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
        for (T entity : findAll()) {
            if (tEntityFilter.accept(entity)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public boolean exists(String name) {
        QueryResult r = executeJcrSql2Query(findOneQuery(nodeType, name));
        try {
            return r.getRows().getSize() > 0;
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Iterable<T> findAll() {
        QueryResult r = executeJcrSql2Query(findAllQuery(nodeType));
        try {
            return readResult(r.getRows(), null);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    protected T getEntity(String path, NodeFilter filter) {
        return dao.get(path, filter);
    }

    @Override
    public Iterable<T> findAll(Iterable<String> strings) {
        List<T> entities = new ArrayList<>();

        for (String ids : strings) {
            entities.add(findOne(ids));
        }
        return entities;
    }

    @Override
    public Iterable<T> findAll(EntityFilter<T> tEntityFilter) {
        List<T> entities = new ArrayList<>();

        for (T entity : findAll()) {
            if (tEntityFilter.accept(entity)) {
                entities.add(entity);
            }
        }

        return entities;
    }

    @Override
    public long count() {
        QueryResult r = executeJcrSql2Query(findAllQuery(nodeType));
        try {
            return r.getRows().getSize();
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public void executeTransactionalBlock(Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> A executeTransactionalBlock(Callable<A> aCallable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionManager getTransactionManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> FluentTransaction<R> transaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> FluentTransaction<R>.Intermediate transaction(Callable<R> callable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T findOneByQuery(String query, String language) {
        QueryResult r = executeQuery(query, language);
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
    public List<T> findByQuery(String query, String language) {
        QueryResult r = executeQuery(query, language);
        try {
            return readResult(r.getRows(), null);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return null;
    }

    protected QueryResult executeQuery(String statement, String language) {
        try {
            javax.jcr.query.QueryManager queryManager = repository.getSession().getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(statement, language);
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

    protected QueryResult executeJcrSql2Query(String statement) {
        return executeQuery(statement, Query.JCR_SQL2);
    }

}
