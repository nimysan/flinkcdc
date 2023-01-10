git submodule update --merge --remote
# if you changed the source code after build, you can uncomment below line to force rebuild the image
# docker-compose build --no-cache data-generator
docker-compose -p cuteflink -f docker-compose.yml up -d
