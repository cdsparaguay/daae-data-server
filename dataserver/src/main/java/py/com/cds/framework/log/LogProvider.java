package py.com.cds.framework.log;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class LogProvider {

	@Produces
	public Logger getLogger(InjectionPoint point) {
		return LogManager.getLogger(point.getMember().getDeclaringClass().getName());
	}
}
