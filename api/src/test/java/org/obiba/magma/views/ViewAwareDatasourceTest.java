package org.obiba.magma.views;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public class ViewAwareDatasourceTest {

  private ViewAwareDatasource sut;

  @Test
  public void testGetViewsOnlyReturnsViews() throws Exception {
    Set<ValueTable> views = new HashSet<ValueTable>();
    // Add a ValueTable.
    ValueTable valueTableOne = createMock(ValueTable.class);
    views.add(valueTableOne);
    // Add two Views.
    views.add(new View());
    views.add(new View());

    Datasource datasource = createMock(Datasource.class);

    sut = new ViewAwareDatasource(datasource, views);
    assertThat(sut.getViews().size(), is(2));
  }
}
