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
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class CollectionPanel extends Panel {

  private static final long serialVersionUID = 8820125143513457684L;

  public CollectionPanel(String id, IModel<Collection> collectionModel) {
    super(id, collectionModel);

    // TODO: Make one view per entityType
    String entityType = getModel().getObject().getEntityTypes().iterator().next();
    add(new DataView<Variable>("variables", new VariableDataProvider(entityType)) {

      private static final long serialVersionUID = -8601452436304391332L;

      @Override
      protected void populateItem(Item<Variable> item) {
        item.add(new Label("name", new PropertyModel<String>(item.getModel(), "name")));
        item.add(new Label("type", new PropertyModel<String>(item.getModel(), "valueType.name")));
        item.add(new DataView<Category>("categories", new CategoryDataProvider(item.getModel())) {

          private static final long serialVersionUID = 4615644513438302396L;

          @Override
          protected void populateItem(Item<Category> item) {
            item.add(new Label("name", new PropertyModel<String>(item.getModel(), "name")));
            item.add(new Label("code", new PropertyModel<String>(item.getModel(), "code")));
          }
        });
      }
    });
  }

  @SuppressWarnings("unchecked")
  public IModel<Collection> getModel() {
    return (IModel<Collection>) super.getDefaultModel();
  }

  private static class CategoryDataProvider implements IDataProvider<Category> {

    private static final long serialVersionUID = -4740198039337060239L;

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

        private static final long serialVersionUID = 3599901956227418900L;

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

    private static final long serialVersionUID = -6929361212788032632L;

    private String entityType;

    public VariableDataProvider(String entityType) {
      this.entityType = entityType;
    }

    @Override
    public void detach() {
    }

    @Override
    public Iterator<? extends Variable> iterator(int arg0, int arg1) {
      return ImmutableList.copyOf(getModel().getObject().getVariables(entityType)).subList(arg0, arg1).iterator();
    }

    @Override
    public IModel<Variable> model(Variable variable) {
      final String type = variable.getEntityType();
      final String name = variable.getCollection() + ':' + variable.getName();
      return new LoadableDetachableModel<Variable>(variable) {

        private static final long serialVersionUID = -5709197229495004502L;

        @Override
        protected Variable load() {
          return MagmaEngine.get().lookupVariable(type, name).getVariable();
        }
      };
    }

    @Override
    public int size() {
      return getModel().getObject().getVariables(entityType).size();
    }

  }
}
