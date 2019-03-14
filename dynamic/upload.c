#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// TODO: There are a lot of potential errors in this file, clean them up

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

void getRequest() {
    char* upload_template = read_file("data/upload.html");
    printf("%s", upload_template);

	free(upload_template);
}

char *strtok_single(char *str, char const * delims) {
    static char *src = NULL;
    char *p, *ret=0;
    
    if(str != NULL)
        src = str;

    if(src == NULL) 
        return NULL;
    
    if((p = strpbrk(src, delims)) != NULL) {
        *p = 0;
        ret = src;
        src = ++p;
    } else if (*src) {
        ret = src;
        src = NULL;
    }

    return ret;
}

// comple: gcc teacher.c -o teacher
int main() {

    /*
    char line[1024];

    // Get the first line, this is the status line
    fgets(line, 1022, stdin);

    // All we care about is the method so we'll slice off the rest after it
    char* first_space = strchr(line, ' ');
    if(first_space != NULL) {
        *first_space = '\0';
    }

    // Check the method and handle accordingly
    if(strcmp(line, "GET") == 0) {
        getRequest();
    }

    return 0;
    */

    getRequest();

    return 0;
}
