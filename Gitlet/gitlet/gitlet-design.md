# Gitlet Design Document

## **Repository.Java**:

This is the class that will handle all of the operations that will be called in Main.java, and call other classes or 
methods to perform required actions. Has all of the relevant files needed in .gitlet as its instance variables
and creates them when its constructor is called during init. 


***INIT Method***:

- Creates all files in .gitlet, calls an initial commit and adds it to a 'master' branch. 
- Checks if CWD already has a .gitlet dir

***ADD Method***:

- Reads the staging area from the stage file
- Calls the Stage.add() method, passing in the filename

***COMMIT Method***:

- Reads the Stage and Branch from their respective files
- Creates a new commit object (which should automatically add and subtract from their respective collections)
and clears the stage after. Add new commit to front of current branch

***CHECKOUT (1/2) Method***:

- Pulls out the relevant commit from the COMMITS dir based on its SHA1 id
- Extract the wanted blob, read its data as a string, and write that string into the file that needs to be updated
/ might also need to create the file if it does not exist.

***CHECKOUT 3 Method***:
- C
- 

***LOG Method***:

- Prints log

## Classes and Data Structures
***Repo.java***:

very vague at the moment, but the repo() constructor should be in charge of creating 
and ensuring that any repos created by the user is correct according to project specs, 
such as not being a duplicate. 

***add.Java***:

the add() command will add a file to the staging area and prepare it for commits, and will
also check that the input is in its correct format and structure. 

***Commits.Java***:


commits a file from the staging area into the repo folder, and also calls persistence methods
on it to ensure that it is saved in the correct commit format. adds pointers to all blobs and 
former commits.

***Persistence.Java***:

in charge of saving files and ensuring that files. Will also probably need to implement a 
update() method that changes former commits and updates committed files into its newer versions. 

## Algorithms
Alright admittedly this portion will be just to fluff the word count, but
there are several key methods that will need intellgent implementation.

I suspect that merging an old file into a new version when we call 
check out will be a difficult method, so that will be one of the key methods
that I know will take a long time for me personally. Good luck to us all. 


## Persistence
Honestly this section will probably suffer from my lack of understanding of persistence in general, 
but once I'm better at this this file will definitely be updated. 

committed files that are already in repo will need to be updated each time a new commit is called
when it refers back to a file that is already in repo. Relevant things to keep in mind
is adding new files to repo, updating old files, deleting files ion a new commit, and

peristence will be key in this project so I will be changing this document as time goes on

