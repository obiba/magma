/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.limesurvey;

import java.util.List;
import java.util.Map;

public interface LimesurveyElementProvider {

  Map<Integer, LimeQuestion> queryQuestions();

  Map<Integer, List<LimeAnswer>> queryExplicitAnswers();

  Map<Integer, LimeAttributes> queryAttributes();

}
