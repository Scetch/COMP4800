# Create the out directory out/
mkdir -p out

# Compile the server to out/
javac -sourcepath src/ -d out/ src/Main.java

# Run the server
java -cp out/ Main