This segment of code was used to test the ability of the collection sorter used by the drain block to correctly sort blocks.

30 tests per setting group were used and found to generate correct results
Input data was randomized on start and was not recorded.

To test the the method yourself load up the main.class and change some of the varables. You will 
need to load this up with minecraft and UE api as the vector3 class is from UE api that needs minecraft

Changable vars
int listSize   - changes the number of elements used for testing. Higher this number the more 
                    vectors will be generated to read threw
int maxChangeY - changes the max amount that the y value can change from the origin
int maxChangeD - changes the max distance that the x&z can change from the origin

Start vector can be changed but is coded in the main method to randomly generate in a range of 10