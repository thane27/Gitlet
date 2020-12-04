
# Gitlet Design Document

**Name**: Ethan Brown

## Classes and Data Structures
### Repository

This class holds all the information for a given repo and contains all methods used to modify or update it.

**Fields**
1. 'CommitTree tree': keeps track of all commits for given repo.
2. 'Path dir': keeps track of current files in given repo
3. 'String name': name of given repo
4. 'Path stagingDir': keeps track of current files to be committed 

### Commits
This class holds all the information for a given commit and contains the methods used to initialize and store it.

**Fields**
1. 'String author': author of given commit
2. 'Date date': Date instance marking when commit was made
3. 'CommitTree tree': CommitTree that given commit is a part of
4. 'String message': log message for given commit
5. 'Integer id': unique integer id for given commit

### Main

This class reads user input and runs the entire Gitlet program

**Fields**
1. 'String input': optional string of arguments for Main to read and execute.

### Blob

This class keeps track of the contents of files.

**Fields**
1. 'File file': file that blob is keeping track of
2. 'Integer id': unique integer id for blob.


## Algorithms
### Main
1. 'main(String[] args)': reads user arguments and executes commands. Returns error messages of arguments are invalid.
### Repository
1. 'initialize()': initializes given repo in the current directory and executes the initial commit with an accurate timestamp.
2. 'add(File f)': adds file into stagingDir of given repo. Checks if file already exists in given stagingDir. If it does and fs contents differ from the original file, it overwrites the previous file in the staging area. Checks if file is present in current commit of given repo. If it is and fs contents do not differ at all from current version, file is not staged for commit.
3. 'commit()': creates an instance of commit class for files in current stagingDir. Adds commit to tree of repo and clears stagingDir.
4. 'remove(File f)': if f is in stagedDir, remove it. If f is in the current commit for given repo, stage it for removal.
5. 'log()': Returns commit history for given repo starting at current head of CommitTree in repo including timestamps, commit messages, and the commits unique integer id.
6. 'globalLog()': Returns complete commit history for given repo.
7. 'find(String name)': prints all ids for a commit that contain the message name.
8. 'status()': displays all branches of current repo and marks current branch. In addition, displays which files have been staged for removal or addition.
9. 'checkout1(File f)': takes f from head of CommitTree and adds it to working directory, replacing its current version contained there. If file does not exist or does not exist in working directory, it will return an error.
10. 'checkout2(File f, int id)': Does the same as checkout1 except for the fact that it takes f from the commit in CommitTree with int id and not from the head of the CommitTree of the given repo. Errors if no commit exists with given id.
11. 'checkout3(String name)': Same as checkout1 except takes all files from the commit at the head of the branch given by name, resulting in the given branch now being considered as the current branch.
12. 'branch(String name)': creates new branch and points to head of CommitTree. Before this is called, code is running on default branch 'master'.
13. 'rm-branch(String name)': deletes pointer associated with branch with given name.
14. 'reset(int id)': Checks out all files tracked by commit with id id. Moves current branch head to commit with id id. Staging directory is cleared (stagingDir).
15. 'merge(String name)': merges files from branch with given name to current branch abiding by all rules listed in the spec.  
### Commit
1. 'initialize(Path p, Date d)': intializes instance of Commit class containing given files in p and using timestamp d.
### Blob
1. 'initialize(File f)': initializes instance of blob class containing f and its contents.
## Persistence
1. For keeping track of commits and their various files and states, we will have a directory which contains folders for every commit logged under their unique id. 
2. For keeping track of staging and committed files, the repo class has a folder for staged files which is used when making commits and then is cleared. When commits are made they will make their own specific folder with instances of the files in their specific commit instance.

