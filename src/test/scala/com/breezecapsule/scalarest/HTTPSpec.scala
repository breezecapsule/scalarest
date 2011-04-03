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
import java.net.URL
import java.io.{IOException, FileNotFoundException}
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

            server.react {
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
            server.react {
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
            server.react {
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

            server.react {
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
            server.react {
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
            server.react {
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
            server.react {
                case Get("/a", _) => {
                    Text("a")
                }
            }
            server.react {
                case Get("/b", _) => {
                    Text("b")
                }
            }

            server.start;

            Source.fromURL("http://localhost:8080/b").getLines.next should equal ("b");

        } finally {
            server.stop;
        }
    }

    "Multiple reactors composed " should " be matched sequentially" in {
        val server = HTTP.createServer(8080);
        try {

            val reactor1:PartialFunction[ResourceRequest, ResourceRepresentation] = {case Get("/a", _) => Text("a")};
            val reactor2:PartialFunction[ResourceRequest, ResourceRepresentation] = {case Get("/b", _) => Text("b")};

            server.react(reactor1 orElse reactor2);

            server.start;

            Source.fromURL("http://localhost:8080/b").getLines.next should equal ("b");

        } finally {
            server.stop;
        }
    }

    "Multiple reactor with regular expression on path " should " match " in {
        val server = HTTP.createServer(8080);
        try {

            val WithinFolder = "/folder/(\\d+)".r


            server.react {
                case Get(WithinFolder(id), _) => {
                    Text(id);
                }
            };

            server.start;

            Source.fromURL("http://localhost:8080/folder/1234").getLines.next should equal ("1234");

        } finally {
            server.stop;
        }
    }

}