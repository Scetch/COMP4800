#include <stdio.h>
int countTransaction()
{
static int count;
++count;
return count;
}
int main()
{
for (int i = 0; i < 9; i++)
countTransaction();
int final_count = countTransaction();
printf("Called %d times\n", final_count);
return 0;
}
