/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.EntityComparator;

/**
 * @author Decebal Suiu
 */
public class EntityDataProvider implements IDataProvider<Entity> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(EntityDataProvider.class);
	
	protected IModel<Entity> entityModel;
	private transient List<Entity> children;
	
    @SpringBean
    protected StorageService storageService;
	
    public EntityDataProvider(IModel<Entity> entityModel) {
    	this.entityModel = entityModel;
    	
    	Injector.get().inject(this);
    }
    
    @Override
	public Iterator<? extends Entity> iterator(long first, long count) {
		return getChildren().iterator();
	}

	@Override
	public IModel<Entity> model(Entity entity) {
		return new EntityModel(entity.getId());
	}

	@Override
	public long size() {
		return getChildren().size();
	}

	@Override
	public void detach() {
		children = null;
	}

    private List<Entity> getChildren() {
        if (children == null) {
        	try {
				children = loadChildren();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO
				throw new RuntimeException(e);
			}
        }
        
        return children;
    }

    private List<Entity> loadChildren() throws Exception {
    	long time = System.currentTimeMillis();
    	String id = entityModel.getObject().getId();
    	String path = entityModel.getObject().getPath();
        Entity[] entities = storageService.getEntityChildrenById(id);
        time = System.currentTimeMillis() - time;
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Load " + entities.length + " entities for '" + path + "' in " + time + " ms");
        }
        List<Entity> result = Arrays.asList(entities);
        Collections.sort(result, new EntityComparator());
        
        return result;
    }

}
