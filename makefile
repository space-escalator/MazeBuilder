JC = javac
JFLAGS = -g

default: MazeBuilder.class Cell.class

MazeBuilder.class : MazeBuilder.java
	$(JC) $(JFLAGS) MazeBuilder.java

Cell.class : Cell.java
	$(JC) $(JFLAGS) Cell.java

clean:
	rm *.class

jar: MazeBuilder.class Cell.class
	jar cfe MazeBuilder.jar MazeBuilder *.class
	make clean

cleanjar:
	rm *.jar