package org.obiba.meta.wicket;

import java.util.Iterator;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.meta.Collection;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Variable;

import com.google.common.collect.ImmutableList;

public class CollectionPanel extends Panel {
	public CollectionPanel(String id, IModel<Collection> collectionModel) {
		super(id, collectionModel);

		add(new DataView<Variable>("variables", new VariableDataProvider()) {
			@Override
			protected void populateItem(Item<Variable> item) {
				item.add(new Label("name", new PropertyModel(item.getModel(), "name")));
				item.add(new Label("type", new PropertyModel(item.getModel(), "valueType.name")));
			}
		});
	}

	public IModel<Collection> getModel() {
		return (IModel<Collection>) super.getDefaultModel();
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
