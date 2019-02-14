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

void getRequest() {
    char *header = read_file("sites/teacher_header.html");
    printf("%s", header);

    // Load name and marks
    char *classList = read_file("marks/classlist.txt");
    char *marksList = read_file("marks/marks.txt");

    // for getting each line of the string(file)
    char *saveptrName, *saveptrMark;
    char *nameTok = strtok_r(classList, "\n", &saveptrName);
    char *markTok = strtok_r(marksList, "\n", &saveptrMark);
    int id = 0;

    while(nameTok != NULL && markTok != NULL) {
        printf("<tr><td>%s</td>", nameTok);
        printf("<td><a href=\"/downloads/source%d.c\" download>Source</a></td>", id);
        printf("<td><a href=\"/downloads/binary%d.out\" download>Binary</a></td>", id);
        printf("<td><input type=\"number\" name=\"%d\" value=\"%s\" style=\"width: 50px\" tabindex=\"1\"></td></tr>", id, markTok);

        id++;
        nameTok = strtok_r(NULL, "\n", &saveptrName);
        markTok = strtok_r(NULL, "\n", &saveptrMark);
    }

    char *footer = read_file("sites/teacher_footer.html");
    printf("%s", footer);

    // Remember to clean up memory.
    free(saveptrMark);
    free(saveptrName);
    free(nameTok);
    free(markTok);
    free(classList);
    free(marksList);
    free(header);
    free(footer);

    return;
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

void postRequest(char *line) {
    // write to marks
    FILE *fp;
    if((fp = fopen("marks/marks.txt", "w+"))==NULL) {
        fprintf(stderr, "error");
        exit(1);
    }


    char delims[] = ";&=\n";
    char *pTok = strtok_single(line, delims);
    while(pTok != NULL) {
        pTok = strtok_single(NULL, delims); // skip id
        if(pTok) {
            // printf("%s<br>", *pTok ? pTok : "sp");
            fprintf(fp, "%s\n", *pTok ? pTok : " ");
        }
        pTok = strtok_single(NULL, delims);
    }

    fclose(fp);
    return;
}

// comple: gcc teacher.c -o teacher
int main() {
    char line[257];
    fgets(line, 5, stdin);
    if(strcmp(line,"GET ")==0) {
        getRequest();
        return 0;
    } else if(strcmp(line, "POST")==0) {
        while(fgets(line, 255, stdin) != NULL) {
            // Skip until end of line
        }
        strcat(line, ";");
        postRequest(line);
        getRequest();
        return 0;
    }

    return 0;
}