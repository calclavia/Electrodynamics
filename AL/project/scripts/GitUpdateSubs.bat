cd ..\
git submodule foreach git pull origin master
git commit -am "Updating Submodules"
git push origin master