package py.com.cds.framework.jpa;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jinq.jpa.JinqJPAStreamProvider;

public class EntityManagerProvider {

	@Produces
	@PersistenceContext
	private EntityManager manager;

	@Produces
	private JinqJPAStreamProvider streams() {
		return new JinqJPAStreamProvider(manager.getMetamodel());
	}
}
