/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.support;

import javax.annotation.Nullable;

import org.obiba.magma.ValueTable;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;

/**
 *
 */
public class Orderings {

  public static final Ordering<ValueTable> VALUE_TABLE_NAME_ORDERING = new Ordering<ValueTable>() {
    @Override
    public int compare(@Nullable ValueTable left, @Nullable ValueTable right) {
      return Strings.nullToEmpty(left == null ? "" : left.getName())
          .compareTo(Strings.nullToEmpty(right == null ? "" : right.getName()));
    }
  };

  private Orderings() {}
}
