#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// Read an entire file to string.
// This makes writing the HTML easier.
char* read_file(char* name) {
    char* buf = NULL;
    int len = 0;
    FILE* f = fopen(name, "r");
    
    if(!f) {
        return NULL;
    }

    // Get the size of the file
    fseek(f, 0, SEEK_END);
    len = ftell(f);
    fseek(f, 0, SEEK_SET);

    buf = (char*) malloc(len + 2);
    fread(buf, 1, len, f);
    buf[len + 1] = '\0';

    fclose(f);

    return buf;
}

void print_person() {
    char line[256];
    while(fgets(line, 256, stdin) != NULL) {
        // Skip until end of line
    }
    printf("%s", line);
}

int main() {
    char* header = read_file("sites/teacher_header.html");
    printf("%s", header);

    print_person();

    char* footer = read_file("sites/teacher_footer.html");
    printf("%s", footer);

    // Remember to clean up memory.
    free(header);
    free(footer);

    return 0;
}