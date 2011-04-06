package com.breezecapsule.scalarest.example

import com.breezecapsule.scalarest.{Lifecycle, HTTP}
import collection.mutable.ConcurrentMap

/**
 * Sample server that provides an in-memory key-value store.
 */
class KeyValueServer(val port:Int) extends Lifecycle {

    private val httpServer = HTTP.createServer(port);
    private val keyValuePairs = new collection.mutable.Map() with ConcurrentMap[String, String];
    override def start = httpServer.start;
    override def stop = httpServer.stop;



}