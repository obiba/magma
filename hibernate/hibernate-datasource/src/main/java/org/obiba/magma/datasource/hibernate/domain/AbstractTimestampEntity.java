package org.obiba.magma.datasource.hibernate.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.obiba.core.domain.AbstractEntity;

/**
 * Persistent entities that extend this class are provided with read-only 'created' and 'updated' timestamp fields.
 * These fields indicate the time that the entity was created and the last time the entity was updated. The 'updated'
 * member uses {@link @Version} and can be used for optimistic locking. Additionally the subclass will also receive the
 * generated 'id' field from the parent {@link AbstractEntity} class.
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractTimestampEntity extends AbstractEntity implements Timestamp {

  @Temporal(TemporalType.TIMESTAMP)
  @Column(insertable = true, updatable = false, nullable = false)
  private Date created = new Date();

  @Version
  @Column(nullable = false)
  private Date updated;

  @Override
  public Date getCreated() {
    return new Date(created.getTime());
  }

  @Override
  public Date getUpdated() {
    return new Date(updated.getTime());
  }

}
