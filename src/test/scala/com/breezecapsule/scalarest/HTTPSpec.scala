/*
 * Created by IntelliJ IDEA.
 * User: williamlee
 * Date: 31/03/2011
 * Time: 17:18
 */
package com.breezecapsule.scalarest

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import io.Source
import java.io.{IOException, FileNotFoundException}
import java.net.{HttpURLConnection, URL}
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.ConcurrentHashMap
;

class HTTPSpec extends FlatSpec with ShouldMatchers {


    "A HTTP server" should "start and stop" in {
        val server = HTTP.createServer(8080);
        try {
            server.start;
        } finally {
            server.stop;
        }
    }

    "A reactor that handles GET request with text/plain response" should "produce correct output" in {
        val server = HTTP.createServer(8080);
        try {

            server reactsTo {
                case Get("/a", _) => {
                    Text("Hello world!");
                }
            }

            server.start;

            Source.fromURL("http://localhost:8080/a").getLines.next should equal("Hello world!")

            evaluating {
                Source.fromURL("http://localhost:8080/unknown")
            } should produce[FileNotFoundException];
        } finally {
            server.stop;
        }
    }

    "A reactor that handles GET request with text/xml response" should "produce correct output" in {
        val server = HTTP.createServer(8080);
        try {

            val testXml = <a>
                <b>test</b>
            </a>;
            server reactsTo {
                case Get("/a", _) => {
                    Xml(testXml);
                }
            }


            server.start;

            val element = scala.xml.XML.load(new URL("http://localhost:8080/a"));
            element should equal(testXml);

        } finally {
            server.stop;
        }
    }



    "A reactor that handles GET request with image/gif response" should "produce correct output" in {
        val server = HTTP.createServer(8080);
        try {
            server reactsTo {
                case Get("/a", _) => {
                    ByteStream("image/gif", Source.fromString("Hello world!").toStream.map(_.toByte));
                }
            }

            server.start;

            Source.fromURL("http://localhost:8080/a").getLines.next should equal("Hello world!")

        } finally {
            server.stop;
        }
    }

    "A reactor that expects parameters" should "be able to patterm match parameters" in {
        val server = HTTP.createServer(8080);
        try {

            server reactsTo {
                case Get("/a", params) => params.get("b") match {
                    case Some(b) => Text(b);
                    case None => Text("");
                }
            }

            server.start;

            Source.fromURL("http://localhost:8080/a?b=hello").getLines.next should equal("hello")

        } finally {
            server.stop;
        }
    }



    "A reactor that returns not found" should "produce HTTP 404" in {
        val server = HTTP.createServer(8080);
        try {
            server reactsTo {
                case Get("/a", _) => {
                    ResourceNotFound()
                }
            }

            server.start;

            evaluating {
                Source.fromURL("http://localhost:8080/a")
            } should produce[FileNotFoundException];

        } finally {
            server.stop;
        }
    }

    "A reactor that returns not authorized" should "produce HTTP 401" in {
        val server = HTTP.createServer(8080);
        try {
            server reactsTo {
                case Get("/a", _) => {
                    ResourceUnauthorized()
                }
            }

            server.start;

            evaluating {
                Source.fromURL("http://localhost:8080/a")
            } should produce[IOException];

        } finally {
            server.stop;
        }
    }

    "Multiple reactors " should " be matched sequentially" in {
        val server = HTTP.createServer(8080);
        try {
            server reactsTo {
                case Get("/a", _) => {
                    Text("a")
                }
            } and {
                case Get("/b", _) => {
                    Text("b")
                }
            }

            server.start;

            Source.fromURL("http://localhost:8080/b").getLines.next should equal("b");

        } finally {
            server.stop;
        }
    }

    "Multiple reactors composed " should " be matched sequentially" in {
        val server = HTTP.createServer(8080);
        try {

            val reactor1: PartialFunction[ResourceRequest, ResourceRepresentation] = {
                case Get("/a", _) => Text("a")
            };
            val reactor2: PartialFunction[ResourceRequest, ResourceRepresentation] = {
                case Get("/b", _) => Text("b")
            };

            server reactsTo (reactor1 orElse reactor2);

            server.start;

            Source.fromURL("http://localhost:8080/b").getLines.next should equal("b");

        } finally {
            server.stop;
        }
    }

    "Multiple reactor with regular expression on path " should " match " in {
        val server = HTTP.createServer(8080);
        try {

            val WithinFolder = "/folder/(\\d+)".r


            server reactsTo {
                case Get(WithinFolder(id), _) => {
                    Text(id);
                }
            };

            server.start;

            Source.fromURL("http://localhost:8080/folder/1234").getLines.next should equal("1234");

        } finally {
            server.stop;
        }
    }

    "Reactor sending permanent redirect " should " redirect " in {
        val server = HTTP.createServer(8080);
        try {

            server reactsTo {
                case Get("/a", _) => {
                    ResourcePermanentlyRedirected("/b");
                }
            };

            server.start;

            val url = new URL("http://localhost:8080/a")
            val connection = url.openConnection.asInstanceOf[HttpURLConnection];
            connection.setInstanceFollowRedirects(false);
            Source.fromInputStream(connection.getInputStream);
            connection.getResponseCode should equal(HttpServletResponse.SC_MOVED_PERMANENTLY);
            connection.getHeaderField("Location") should equal("/b");
        } finally {
            server.stop;
        }
    }

    "Reactor sending permanent redirect " should " redirect and serve alternative content " in {
        val server = HTTP.createServer(8080);
        try {

            server reactsTo {
                case Get("/a", _) => {
                    ResourcePermanentlyRedirected("/b");
                }
                case Get("/b", _) => {
                    Text("b");
                }
            };

            server.start;

            val url = new URL("http://localhost:8080/a")
            val connection = url.openConnection.asInstanceOf[HttpURLConnection];
            connection.setInstanceFollowRedirects(true);
            Source.fromInputStream(connection.getInputStream).getLines.next should equal("b");
        } finally {
            server.stop;
        }
    }

    "Reactor sending temporary redirect " should " redirect " in {
        val server = HTTP.createServer(8080);
        try {

            server reactsTo {
                case Get("/c", _) => {
                    ResourceTemporarilyRedirected("/d");
                }
            };

            server.start;

            val url = new URL("http://localhost:8080/c")
            val connection = url.openConnection.asInstanceOf[HttpURLConnection];
            connection.setInstanceFollowRedirects(false);
            Source.fromInputStream(connection.getInputStream);
            connection.getResponseCode should equal(HttpServletResponse.SC_MOVED_TEMPORARILY);
        } finally {
            server.stop;
        }
    }

}