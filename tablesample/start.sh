git submodule update --merge --remote
# if you changed the source code after build, you can uncomment below line to force rebuild the image
docker-compose build --no-cache data-generator

# start the stack, you can check the docker container by 'docker ps'
docker-compose -p cuteflink -f docker-compose.yml up -d


