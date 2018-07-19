JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Parser.java \
        connection.java \
        HttpServer.java \
        worker.java \
        server.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class