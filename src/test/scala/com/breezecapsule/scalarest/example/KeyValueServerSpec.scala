/*
 * Created by IntelliJ IDEA.
 * User: williamlee
 * Date: 06/04/2011
 * Time: 17:15
 */
package com.breezecapsule.scalarest.example

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import scala.io.Source
import xml.Utility._;
import xml.{Node, XML}
import java.net.{HttpURLConnection, URL}
;
;
class KeyValueServerSpec extends FlatSpec with ShouldMatchers {

    "Get /keys on a new server" should " returns an XML containing no keys" in {
        val server = new KeyValueServer(8080);
        try {
            server.start
            val keys = XML.load(new URL("http://localhost:8080/keys").openStream);
            trim(keys) should equal(<keys></keys>);
        } finally {
            server.stop
        }
    }

    "Get /keys on a server with existing mappins" should " returns an XML containing all mapped keys" in {
        val server = new KeyValueServer(8080);
        try {
            server.start
            server.keyValuePairs += ("a" -> "1");
            server.keyValuePairs += ("b" -> "2");

            val keys = XML.load(new URL("http://localhost:8080/keys").openStream);

            val allKeys = for (val keyElement <- (keys \ "key")) yield keyElement.text
            allKeys should have length (2);
            allKeys should contain ("a");
            allKeys should contain ("b");
        } finally {
            server.stop
        }
    }


    "Putting an object" should " put the object into the underlying map" in {
        val server = new KeyValueServer(8080);
        try {
            server.start

            val connection = new URL("http://localhost:8080/keys/a?value=hello").openConnection.asInstanceOf[HttpURLConnection];
            connection.setRequestMethod("PUT");
            Source.fromInputStream(connection.getInputStream);

            server.keyValuePairs should contain key ("a");
            server.keyValuePairs("a") should equal ("hello");
        } finally {
            server.stop
        }

    }

}