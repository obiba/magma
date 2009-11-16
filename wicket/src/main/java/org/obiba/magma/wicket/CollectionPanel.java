package org.obiba.magma.wicket;

import java.util.Iterator;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.magma.Category;
import org.obiba.magma.Collection;
import org.obiba.magma.MetaEngine;
import org.obiba.magma.Variable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class CollectionPanel extends Panel {
  public CollectionPanel(String id, IModel<Collection> collectionModel) {
    super(id, collectionModel);

    add(new DataView<Variable>("variables", new VariableDataProvider()) {
      @Override
      protected void populateItem(Item<Variable> item) {
        item.add(new Label("name", new PropertyModel(item.getModel(), "name")));
        item.add(new Label("type", new PropertyModel(item.getModel(), "valueType.name")));
        item.add(new DataView<Category>("categories", new CategoryDataProvider(item.getModel())) {
          @Override
          protected void populateItem(Item<Category> item) {
            item.add(new Label("name", new PropertyModel(item.getModel(), "name")));
            item.add(new Label("code", new PropertyModel(item.getModel(), "code")));
          }
        });
      }
    });
  }

  public IModel<Collection> getModel() {
    return (IModel<Collection>) super.getDefaultModel();
  }

  private class CategoryDataProvider implements IDataProvider<Category> {

    IModel<Variable> variableModel;

    public CategoryDataProvider(IModel<Variable> model) {
      variableModel = model;
    }

    @Override
    public Iterator<? extends Category> iterator(int arg0, int arg1) {
      return variableModel.getObject().getCategories().iterator();
    }

    @Override
    public IModel<Category> model(Category c) {
      final String name = c.getName();
      return new IModel<Category>() {
        @Override
        public Category getObject() {
          return Iterables.find(variableModel.getObject().getCategories(), new Predicate<Category>() {

            @Override
            public boolean apply(Category input) {
              return input.getName().equals(name);
            }
          });
        }

        @Override
        public void setObject(Category arg0) {
        }

        @Override
        public void detach() {
        }
      };
    }

    @Override
    public int size() {
      return variableModel.getObject().getCategories().size();
    }

    @Override
    public void detach() {
      variableModel.detach();
    }

  }

  private class VariableDataProvider implements IDataProvider<Variable> {

    public VariableDataProvider() {

    }

    @Override
    public void detach() {
    }

    @Override
    public Iterator<? extends Variable> iterator(int arg0, int arg1) {
      return ImmutableList.copyOf(getModel().getObject().getVariables()).subList(arg0, arg1).iterator();
    }

    @Override
    public IModel<Variable> model(Variable variable) {
      final String type = variable.getEntityType();
      final String name = variable.getCollection() + ':' + variable.getName();
      return new LoadableDetachableModel<Variable>(variable) {
        @Override
        protected Variable load() {
          return MetaEngine.get().lookupVariable(type, name).getVariable();
        }
      };
    }

    @Override
    public int size() {
      return getModel().getObject().getVariables().size();
    }

  }
}
