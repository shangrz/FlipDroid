package it.tika;

import it.tika.mongodb.logger.Log;
import it.tika.mongodb.logger.LogDBMongoDB;
import org.restlet.resource.ServerResource;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ITS
 * Date: 11-7-14
 * Time: 下午4:59
 * To change this template use File | Settings | File Templates.
 */
public class Logger implements OnRequestListener {
    public void onRequest(ServerResource resource) {
        Log log = new Log();
        log.setCreatedDate(new Date());
        log.setUrl(resource.getQuery().getQueryString());
        LogDBMongoDB.getInstance().addLog(log);
    }
}
