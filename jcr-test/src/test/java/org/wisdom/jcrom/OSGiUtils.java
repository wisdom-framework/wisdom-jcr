package org.wisdom.jcrom;

import org.junit.Assert;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.model.Crud;

import java.util.List;

/**
 * User: Antoine Mischler <antoine@dooapp.com>
 * Date: 19/06/15
 * Time: 17:31
 */
public class OSGiUtils {

    private final OSGiHelper osgi;

    public OSGiUtils(OSGiHelper osgi) {
        this.osgi = osgi;
    }

    public <T> Crud<T, String> getCrud(Class<T> clazz) {
        osgi.waitForService(Crud.class, null, 5000);
        final List<Crud> cruds = osgi.getServiceObjects(Crud.class);
        Crud<T, String> helloCrud = null;
        for (Crud crud : cruds) {
            if (crud.getEntityClass().equals(clazz)) {
                helloCrud = crud;
                break;
            }
        }
        Assert.assertNotNull(helloCrud);
        return helloCrud;
    }

}
