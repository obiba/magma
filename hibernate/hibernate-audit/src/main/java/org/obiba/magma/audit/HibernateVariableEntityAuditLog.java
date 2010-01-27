package org.obiba.magma.audit;
import java.util.List;
import java.util.Map;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.audit.VariableEntityAuditEvent;
import org.obiba.magma.audit.VariableEntityAuditLog;


public class HibernateVariableEntityAuditLog implements VariableEntityAuditLog {

	@Override
	public VariableEntityAuditEvent createAuditEvent(Datasource datasource,	String type, Map<String, Value> details) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VariableEntityAuditEvent> getAuditEvents(Datasource datasource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VariableEntityAuditEvent> getAuditEvents(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VariableEntityAuditEvent> getAuditEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VariableEntity getVariableEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}
