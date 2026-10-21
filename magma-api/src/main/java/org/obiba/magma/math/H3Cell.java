/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.math;

import org.obiba.magma.Coordinate;

import java.io.Serializable;
import java.util.List;

/**
 * Count of the points in an H3 cell.
 *
 * @param index H3 index, as a hexadecimal string
 * @param count number of points in the cell
 * @param boundary vertices of the cell
 */
public record H3Cell(String index, long count, List<Coordinate> boundary) implements Serializable {
}
