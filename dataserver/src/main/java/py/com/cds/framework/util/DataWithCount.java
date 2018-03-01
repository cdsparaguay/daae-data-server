package py.com.cds.framework.util;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import py.com.cds.framework.jpa.BaseEntity;

@Data
@AllArgsConstructor
public class DataWithCount<T extends BaseEntity> {

	private List<T> data;
	private long recordsFiltered;
	private long recordsTotal;
	private long draw;

}
