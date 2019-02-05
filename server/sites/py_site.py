#!/usr/local/bin/python
import random

if __name__ == "__main__":
    rint = random.randint(1, 2**32)
    print("<html><body>This is a randomly generated python site!")
    print("<p>" + str(rint) + "</p></body></html>")
