package com.breezecapsule.scalarest.example

import com.breezecapsule.scalarest._;
import collection.JavaConversions._
import java.util.concurrent.ConcurrentHashMap
import collection.mutable.ConcurrentMap
;

/**
 * Sample server that provides an in-memory key-value store.
 */
class KeyValueServer(val port:Int) extends Lifecycle {


    // The server instance.
    private val httpServer = HTTP.createServer(port);

    // mapping the lifecycle methods to the server's
    override def start = httpServer.start;
    override def stop = httpServer.stop;

    // ConcurrentMap for storing key-value pairs.
    val keyValuePairs:ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]();
    val InKeys = "/keys/(.+)".r

    // set up reaction rules
    httpServer.reactsTo {
        case Get("/keys", _) => Xml(
            <keys>
                {
                for (val key <- keyValuePairs.keys) yield
                    <key>{key}</key>
                }
            </keys>
        )
        case Put(InKeys(name), params) => {
            keyValuePairs += (name -> params("value"));
            Text("Succeeded");
        }
    }

}