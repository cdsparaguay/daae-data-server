/*
 * @RestBaseService.java 1.0 Aug 19, 2014
 */
package py.com.cds.framework.rest;

import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import py.com.cds.framework.ejb.BaseLogic;
import py.com.cds.framework.jpa.BaseEntity;
import py.com.cds.framework.rest.DataTablesHelper.QueryExtracted;
import py.com.cds.framework.util.DataWithCount;
import py.com.cds.framework.security.logic.Logged;

/**
 *
 * TODO agregar soporte a loggers
 *
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Aug 19, 2014
 *
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public abstract class BaseResource<T extends BaseEntity> {

    @Context
    UriInfo uriInfo;

    @Inject
    DataTablesHelper helper;

    @GET
    @Logged(isPublic = true)
    @ApiOperation(value = "Obtiene una lista paginada de entidades", notes = "Esto respeta el API de jquery datatables, para más detalles ver https://www.datatables.net/examples/server_side/simple.html")
    public DataWithCount<T> get(
            @QueryParam("draw") @DefaultValue("0") Long draw,
            @QueryParam("start") @DefaultValue("0") Integer start,
            @QueryParam("length") @DefaultValue("0") Integer length,
            @QueryParam("search[value]") @DefaultValue("") String globalSearch) {

        QueryExtracted qe = helper.extractData(uriInfo.getQueryParameters());

        DataWithCount<T> toRet = getBaseBean().get(start, length,
                qe.getOrders(), qe.getValues(), qe.getProperties(),
                StringUtils.isEmpty(globalSearch) ? null : globalSearch);
        toRet.setDraw(draw);
        return toRet;

    }

    @GET
    @Path("/all")
    @Logged(isPublic = true)
    @ApiOperation("Obtiene todas las entidades")
    public List<T> getAll() {

        return getBaseBean().getAll();
    }

    @GET
    @Path("{id}")
    @Logged(isPublic = true)
    @ApiOperation("Recupera una entidad por su identificador")
    public T get(
            @PathParam("id") @ApiParam("Identificador del usuario") @NotNull Long id) {

        Validate.notNull(id, "No se puede buscar una entidad con id nulo");
        return getBaseBean().findById(id);
    }

    @POST
    @ApiOperation(value = "Permite crear una nueva entidad", hidden = true)
    public T add(@NotNull T entity) {

        Validate.isTrue(entity.getId() == 0l,
                "No se puede crear una entidad con un ID ya asigando");
        T added = getBaseBean().add(entity);
        return added;
    }

    @PUT
    @Path("{id}")
    @ApiOperation(value = "Actualiza una entidad dado su identificador", hidden = true)
    public T update(@PathParam("id") @NotNull Long id, @NotNull T entity) {

        if (entity.getId() == 0l && id == null) {
            throw new IllegalArgumentException("ID no puede ser nulo");
        } else {
            entity.setId(id);
            return getBaseBean().update(entity);
        }
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Elimina una entidad", hidden = true)
    public T delete(@PathParam("id") @NotNull Long id) {

        if (id == 0) {
            throw new IllegalArgumentException("ID no puede ser nulo");
        } else {
            T toDelete = getBaseBean().findById(id);
            getBaseBean().remove(toDelete);
            return toDelete;
        }
    }

    @GET
    @Path("/schema")
    @Logged(isPublic = true)
    @ApiOperation(value = "Retorna un json-schema de la entidad", notes = "Este esquema se puede utilizar para definir las validaciones.")
    public Response getSchema() {

        return Response.ok().entity(new JsonSchemaGenerator()
                .getSchema(getBaseBean().getClassOfT())).build();
    }

    protected abstract BaseLogic<T> getBaseBean();
}
